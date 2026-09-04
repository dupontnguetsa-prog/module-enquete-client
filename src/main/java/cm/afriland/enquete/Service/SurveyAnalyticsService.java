package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyDeliveryEvent;
import cm.afriland.enquete.model.SurveyQuestion;
import cm.afriland.enquete.model.SurveyResponse;
import cm.afriland.enquete.repository.SurveyDeliveryEventRepository;
import cm.afriland.enquete.repository.SurveyRepository;
import cm.afriland.enquete.repository.SurveyResponseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class SurveyAnalyticsService {
    private final SurveyRepository surveyRepository; private final SurveyResponseRepository responseRepository; private final SurveyDeliveryEventRepository eventRepository; private final ObjectMapper mapper;
    public SurveyAnalyticsService(SurveyRepository surveyRepository, SurveyResponseRepository responseRepository, SurveyDeliveryEventRepository eventRepository, ObjectMapper mapper){this.surveyRepository=surveyRepository;this.responseRepository=responseRepository;this.eventRepository=eventRepository;this.mapper=mapper;}

    @Transactional(readOnly=true)
    public Analytics analytics(Long userId, Long surveyId, SurveyService service){
        return analytics(userId, surveyId, service, null, null, null);
    }

    @Transactional(readOnly=true)
    public Analytics analytics(Long userId, Long surveyId, SurveyService service, Integer days, String audienceField, String audienceValue){
        Survey survey = surveyRepository.findByIdAndOwner(surveyId, service.requireUser(userId)).orElseThrow();
        LocalDateTime from = days == null || days <= 0 ? null : LocalDateTime.now().minusDays(days);
        LocalDateTime to = LocalDateTime.now();
        long views=from == null ? eventRepository.countBySurveyAndEventType(survey,"VIEWED") : eventRepository.countBySurveyAndEventTypeAndOccurredAtBetween(survey,"VIEWED",from,to);
        List<SurveyResponse> responseList=from == null ? responseRepository.findAllBySurveyOrderByCompletedAtDesc(survey) : responseRepository.findAllBySurveyAndCompletedAtBetweenOrderByCompletedAtDesc(survey,from,to);
        if (audienceField != null && !audienceField.isBlank() && audienceValue != null && !audienceValue.isBlank()) {
            responseList = responseList.stream().filter(r -> matchesAudience(r, audienceField, audienceValue)).toList();
        }
        long responses=responseList.size(); double rate=views==0?0:(responses*100.0/views);
        List<ResponsePoint> series = series(survey, responseList, days);
        List<QuestionAnalytics> questionAnalytics = new ArrayList<>();
        for(int i=0;i<survey.getQuestions().size();i++){ SurveyQuestion q=survey.getQuestions().get(i); questionAnalytics.add(analyzeQuestion(i,q,responseList)); }
        List<SurveyDeliveryEvent> viewedEvents = eventRepository.findAllBySurveyOrderByOccurredAtDesc(survey).stream()
                .filter(e -> "VIEWED".equals(e.getEventType()))
                .filter(e -> from == null || (!e.getOccurredAt().isBefore(from) && !e.getOccurredAt().isAfter(to)))
                .toList();
        List<SegmentAnalytics> segmentAnalytics = segmentAnalytics(responseList, responses);
        List<ChannelAnalytics> channelAnalytics = channelAnalytics(viewedEvents, responseList);
        long abandoned = Math.max(0, views - responses);
        double completionRate = views == 0 ? 0 : responses * 100.0 / views;
        return new Analytics(views,responses,round(rate),round(completionRate),abandoned,survey.getQuestions().size(),survey.getLogicRules().size(),series,questionAnalytics,segmentAnalytics,channelAnalytics);
    }

    private boolean matchesAudience(SurveyResponse response, String field, String value) {
        if (response.getCustomer() == null) return false;
        Object actual = switch (field) {
            case "customerType" -> response.getCustomer().getCustomerType();
            case "agency" -> response.getCustomer().getAgency();
            case "city" -> response.getCustomer().getCity();
            case "relationshipStatus" -> response.getCustomer().getRelationshipStatus();
            case "product" -> response.getCustomer().getProduct();
            default -> null;
        };
        return actual != null && value.equalsIgnoreCase(String.valueOf(actual));
    }

    private List<ResponsePoint> series(Survey s,List<SurveyResponse> responses,Integer days){ Map<LocalDate,Integer> counts=new TreeMap<>(); responses.forEach(r->counts.merge(r.getCompletedAt().toLocalDate(),1,Integer::sum)); LocalDate today=LocalDate.now(); int span=days==null||days<=0?14:Math.min(days,90); List<ResponsePoint> result=new ArrayList<>(); for(int i=span-1;i>=0;i--){LocalDate d=today.minusDays(i); result.add(new ResponsePoint(d.format(DateTimeFormatter.ISO_LOCAL_DATE),counts.getOrDefault(d,0)));} return result; }
    private QuestionAnalytics analyzeQuestion(int index,SurveyQuestion q,List<SurveyResponse> responses){ Map<String,Integer> distribution=new LinkedHashMap<>(); double sum=0; int numeric=0; int answered=0; for(SurveyResponse r:responses){try{Map<String,Object> answers=mapper.readValue(r.getAnswers(),Map.class); Object value=answers.get(String.valueOf(index)); if(value!=null){answered++; String key=String.valueOf(value); distribution.merge(key,1,Integer::sum); try{sum+=Double.parseDouble(key);numeric++;}catch(Exception ignored){}}}catch(Exception ignored){}} return new QuestionAnalytics(index,q.getTitle(),answered,numeric==0?null:round(sum/numeric),distribution); }
    private List<SegmentAnalytics> segmentAnalytics(List<SurveyResponse> responses, long totalResponses) {
        Map<String, Integer> counts = new HashMap<>();
        responses.stream()
                .filter(response -> response.getCustomer() != null)
                .map(response -> response.getCustomer().getCustomerType())
                .filter(type -> type != null && !type.isBlank())
                .forEach(type -> counts.merge(type, 1, Integer::sum));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new SegmentAnalytics(entry.getKey(), entry.getValue(),
                        totalResponses == 0 ? 0 : round(entry.getValue() * 100.0 / totalResponses)))
                .toList();
    }

    private List<ChannelAnalytics> channelAnalytics(List<SurveyDeliveryEvent> viewedEvents, List<SurveyResponse> responses) {
        Map<String, Integer> viewsByChannel = new HashMap<>();
        List<SurveyDeliveryEvent> attributedViews = viewedEvents.stream()
                .filter(event -> event.getChannel() != null && !event.getChannel().isBlank())
                .sorted(Comparator.comparing(SurveyDeliveryEvent::getOccurredAt))
                .toList();
        attributedViews.forEach(event -> viewsByChannel.merge(event.getChannel(), 1, Integer::sum));

        Set<SurveyDeliveryEvent> usedViews = Collections.newSetFromMap(new IdentityHashMap<>());
        Map<String, Integer> responsesByChannel = new HashMap<>();
        for (SurveyResponse response : responses) {
            attributedViews.stream()
                    .filter(event -> !usedViews.contains(event))
                    .filter(event -> !event.getOccurredAt().isAfter(response.getCompletedAt()))
                    .max(Comparator.comparing(SurveyDeliveryEvent::getOccurredAt))
                    .ifPresent(event -> {
                        usedViews.add(event);
                        responsesByChannel.merge(event.getChannel(), 1, Integer::sum);
                    });
        }
        return viewsByChannel.keySet().stream()
                .sorted()
                .map(channel -> new ChannelAnalytics(channel, viewsByChannel.get(channel),
                        responsesByChannel.getOrDefault(channel, 0),
                        viewsByChannel.get(channel) == 0 ? 0
                                : round(responsesByChannel.getOrDefault(channel, 0) * 100.0 / viewsByChannel.get(channel))))
                .toList();
    }

    private double round(double v){return Math.round(v*10.0)/10.0;}
    public record Analytics(long views,long responses,double responseRate,double completionRate,long abandoned,int questions,int logicRules,List<ResponsePoint> series,List<QuestionAnalytics> questionAnalytics,List<SegmentAnalytics> segmentAnalytics,List<ChannelAnalytics> channelAnalytics){}
    public record ResponsePoint(String date,int responses){}
    public record QuestionAnalytics(int index,String question,int answered,Double average,Map<String,Integer> distribution){}
    public record SegmentAnalytics(String segment,int responses,double rate){}
    public record ChannelAnalytics(String channel,int views,int responses,double rate){}
}
