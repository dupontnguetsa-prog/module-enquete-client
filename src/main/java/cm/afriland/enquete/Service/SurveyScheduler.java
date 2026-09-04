package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.repository.SurveyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import cm.afriland.enquete.repository.SurveyDeliveryEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
public class SurveyScheduler {
    private final SurveyRepository surveys;private final ObjectMapper mapper; private final SurveyDeliveryEventRepository events; private final WebhookService webhooks;
    public SurveyScheduler(SurveyRepository surveys,ObjectMapper mapper,SurveyDeliveryEventRepository events,WebhookService webhooks){this.surveys=surveys;this.mapper=mapper;this.events=events;this.webhooks=webhooks;}
    @Scheduled(fixedDelay=60000)
    public void activateScheduled(){List<Survey> scheduled=surveys.findAllByStatus("Programmée");for(Survey s:scheduled){try{Map<String,Object> cfg=mapper.readValue(s.getTriggerConfig()==null?"{}":s.getTriggerConfig(),new TypeReference<>(){});String d=String.valueOf(cfg.getOrDefault("date",""));String t=String.valueOf(cfg.getOrDefault("time","00:00"));String zone=String.valueOf(cfg.getOrDefault("timezone","Africa/Douala"));if(!d.isBlank()){ZonedDateTime when=ZonedDateTime.of(LocalDateTime.parse(d+"T"+t),ZoneId.of(zone));if(!when.isAfter(ZonedDateTime.now(when.getZone()))){s.setStatus("Active");surveys.save(s);}}}catch(Exception e){System.getLogger(SurveyScheduler.class.getName()).log(System.Logger.Level.WARNING,"Planification invalide pour l'enquête "+s.getId(),e);}}}
    @Scheduled(fixedDelay=60000)
    public void retryWebhooks(){for(var event:events.findTop100ByDeliveryStatusAndRetryCountLessThanOrderByOccurredAtAsc("FAILED",3)){event.setRetryCount(event.getRetryCount()+1);webhooks.dispatch(event.getSurvey(),event,event.getWebhookUrl());events.save(event);}}
}
