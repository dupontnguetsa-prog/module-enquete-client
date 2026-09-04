package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.*;
import cm.afriland.enquete.repository.CustomerProfileRepository;
import cm.afriland.enquete.repository.SurveyRepository;
import cm.afriland.enquete.repository.SurveyResponseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class SurveyResponseService {
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final CustomerProfileRepository customerRepository;
    private final ObjectMapper objectMapper;

    public SurveyResponseService(SurveyRepository surveyRepository, SurveyResponseRepository responseRepository, CustomerProfileRepository customerRepository, ObjectMapper objectMapper) {
        this.surveyRepository = surveyRepository; this.responseRepository = responseRepository; this.customerRepository = customerRepository; this.objectMapper = objectMapper;
    }

    @Transactional
    public ResponseResult submit(String publicKey, SubmitRequest request) {
        Survey survey = surveyRepository.findByPublicKey(publicKey).orElseThrow(() -> new IllegalArgumentException("Enquête introuvable."));
        if (!"Active".equals(survey.getStatus())) throw new IllegalStateException("Cette enquête n’accepte pas de réponses actuellement.");
        if (request == null) throw new IllegalArgumentException("Réponse invalide.");
        validateAnswers(survey, request.answers());
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey); response.setAnonymous(request.anonymous());
        response.setAnswers(writeJson(request.answers() == null ? Map.of() : request.answers()));
        response.setStartedAt(request.startedAt() == null ? LocalDateTime.now() : request.startedAt());
        response.setCompletedAt(LocalDateTime.now());
        if (!response.isAnonymous()) {
            String normalizedEmail = request.email() == null ? null : request.email().trim().toLowerCase();
            response.setRespondentEmail(normalizedEmail);
            if (request.customerId() != null) {
                response.setCustomer(customerRepository.findById(request.customerId())
                    .filter(customer -> normalizedEmail == null
                        || customer.getEmail() == null
                        || customer.getEmail().equalsIgnoreCase(normalizedEmail))
                    .orElseThrow(() -> new IllegalArgumentException("Le profil client ne correspond pas à l’adresse e-mail.")));
            } else if (request.email() != null && !request.email().isBlank()) {
                response.setCustomer(customerRepository.findByEmailIgnoreCase(request.email().trim()).orElse(null));
            }
        }
        if (oneResponseOnly(survey) && response.getCustomer() != null
                && responseRepository.existsBySurveyAndCustomer(survey, response.getCustomer())) {
            throw new IllegalStateException("Vous avez déjà répondu à cette enquête.");
        }
        if (oneResponseOnly(survey) && response.getRespondentEmail() != null
                && responseRepository.existsBySurveyAndRespondentEmailIgnoreCase(survey, response.getRespondentEmail())) {
            throw new IllegalStateException("Vous avez déjà répondu à cette enquête.");
        }
        String action = "";
        for (SurveyLogicRule rule : survey.getLogicRules()) {
            if (rule.getAction() != null && !rule.getAction().isBlank() && matches(rule.getOperator(), request.answers(), rule)) {
                action = rule.getAction();
                break;
            }
        }
        response.setTriggeredAction(action.isBlank() ? null : action);
        responseRepository.save(response);
        return new ResponseResult(response.getId(), response.getCompletedAt(), action);
    }

    private boolean matches(String operator, Map<String,Object> answers, SurveyLogicRule rule) {
        Object answer = answers == null ? null : answers.get(String.valueOf(rule.getSourceQuestion().getDisplayOrder()));
        if (answer == null) return false;
        String actual = String.valueOf(answer);
        String expected = String.valueOf(rule.getValue());
        if (answer instanceof Iterable<?> values) {
            List<String> normalized = new ArrayList<>();
            values.forEach(value -> normalized.add(String.valueOf(value).trim().toLowerCase()));
            if ("IN".equals(operator)) return normalized.contains(expected.trim().toLowerCase());
            if ("NOT_IN".equals(operator)) return normalized.stream().noneMatch(value -> value.equals(expected.trim().toLowerCase()));
            if ("CONTAINS".equals(operator)) return normalized.stream().anyMatch(value -> value.contains(expected.toLowerCase()));
            if ("NOT_CONTAINS".equals(operator)) return normalized.stream().noneMatch(value -> value.contains(expected.toLowerCase()));
        }
        if ("EQUALS".equals(operator)) return actual.trim().equalsIgnoreCase(expected.trim());
        if ("NOT_EQUALS".equals(operator)) return !actual.trim().equalsIgnoreCase(expected.trim());
        if ("CONTAINS".equals(operator)) return actual.toLowerCase().contains(expected.toLowerCase());
        if ("NOT_CONTAINS".equals(operator)) return !actual.toLowerCase().contains(expected.toLowerCase());
        if ("IS_EMPTY".equals(operator)) return actual.trim().isEmpty();
        if ("IS_NOT_EMPTY".equals(operator)) return !actual.trim().isEmpty();
        if ("IN".equals(operator)) return Arrays.stream(expected.split(",")).map(String::trim).anyMatch(value -> value.equalsIgnoreCase(actual.trim()));
        if ("NOT_IN".equals(operator)) return Arrays.stream(expected.split(",")).map(String::trim).noneMatch(value -> value.equalsIgnoreCase(actual.trim()));
        try {
            double numeric = Double.parseDouble(actual);
            double target = Double.parseDouble(expected);
            if ("GREATER_THAN".equals(operator)) return numeric > target;
            if ("LESS_THAN".equals(operator)) return numeric < target;
            if ("GREATER_OR_EQUAL".equals(operator)) return numeric >= target;
            if ("LESS_OR_EQUAL".equals(operator)) return numeric <= target;
            if ("BETWEEN".equals(operator)) {
                String[] bounds = expected.split(",");
                if (bounds.length != 2) return false;
                return numeric >= Double.parseDouble(bounds[0].trim()) && numeric <= Double.parseDouble(bounds[1].trim());
            }
        } catch (NumberFormatException ignored) {
            return false;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<ResponseView> listOwned(Long userId, Long surveyId, SurveyService surveyService) {
        var owner = surveyService.requireUser(userId); Survey survey = surveyRepository.findByIdAndOwner(surveyId, owner).orElseThrow();
        return responseRepository.findAllBySurveyOrderByCompletedAtDesc(survey).stream().map(r -> new ResponseView(r.getId(), r.isAnonymous(), r.getCustomer() == null ? null : r.getCustomer().getName(), r.getAnswers(), r.getCompletedAt(), r.getTriggeredAction())).toList();
    }

    @Transactional(readOnly = true)
    public Page<ResponseView> listOwned(Long userId, Long surveyId, SurveyService surveyService, Pageable pageable) {
        var owner = surveyService.requireUser(userId);
        Survey survey = surveyRepository.findByIdAndOwner(surveyId, owner).orElseThrow();
        return responseRepository.findAllBySurveyOrderByCompletedAtDesc(survey, pageable)
            .map(r -> new ResponseView(r.getId(), r.isAnonymous(),
                r.getCustomer() == null ? null : r.getCustomer().getName(),
                r.getAnswers(), r.getCompletedAt(), r.getTriggeredAction()));
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(Long userId, Long surveyId, SurveyService surveyService) {
        var owner = surveyService.requireUser(userId);
        Survey survey = surveyRepository.findByIdAndOwner(surveyId, owner).orElseThrow();
        List<SurveyResponse> responses = responseRepository.findAllBySurveyOrderByCompletedAtDesc(survey);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(csvCell("Réponse")).append(',').append(csvCell("Répondant")).append(',').append(csvCell("Date")).append(',').append(csvCell("Action")).append(',');
        csv.append(survey.getQuestions().stream().map(q -> csvCell(q.getTitle())).collect(Collectors.joining(","))).append('\n');
        for (SurveyResponse response : responses) {
            csv.append(csvCell(String.valueOf(response.getId()))).append(',')
                .append(csvCell(response.isAnonymous() ? "Anonyme" : response.getCustomer() == null ? "Client" : response.getCustomer().getName())).append(',')
                .append(csvCell(response.getCompletedAt().toString())).append(',').append(csvCell(response.getTriggeredAction() == null ? "" : response.getTriggeredAction())).append(',');
            try {
                Map<?, ?> answers = objectMapper.readValue(response.getAnswers(), Map.class);
                csv.append(survey.getQuestions().stream().map(q -> {
                    String key = String.valueOf(q.getDisplayOrder());
                    return csvCell(String.valueOf(answers.containsKey(key) ? answers.get(key) : ""));
                }).collect(Collectors.joining(",")));
            } catch (Exception e) {
                csv.append(csvCell("Réponse invalide"));
            }
            csv.append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csvCell(String value) {
        return "\"" + String.valueOf(value).replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    private String writeJson(Object value) { try { return objectMapper.writeValueAsString(value); } catch (JsonProcessingException e) { throw new IllegalArgumentException("Réponses invalides."); } }

    private boolean oneResponseOnly(Survey survey) {
        try {
            Object value = objectMapper.readValue(survey.getSettings(), Map.class).get("oneResponse");
            return value == null || Boolean.parseBoolean(String.valueOf(value));
        } catch (Exception e) {
            return true;
        }
    }

    private void validateAnswers(Survey survey, Map<String,Object> answers) {
        Map<String,Object> safeAnswers = answers == null ? Map.of() : answers;
        for (int i = 0; i < survey.getQuestions().size(); i++) {
            var question = survey.getQuestions().get(i);
            Object answer = safeAnswers.get(String.valueOf(question.getDisplayOrder()));
            if (question.isRequired() && (answer == null || String.valueOf(answer).isBlank()
                    || answer instanceof java.util.Collection<?> collection && collection.isEmpty())) {
                throw new IllegalArgumentException("Répondez à la question " + (i + 1) + ".");
            }
            if (answer == null) continue;
            if (("NPS".equals(question.getType()) || "SCALE".equals(question.getType()))
                    && !(answer instanceof Number)) {
                throw new IllegalArgumentException("La réponse à la question " + (i + 1) + " doit être numérique.");
            }
            if (answer instanceof Number number
                    && (number.doubleValue() < question.getMinValue() || number.doubleValue() > question.getMaxValue())) {
                throw new IllegalArgumentException("La réponse à la question " + (i + 1) + " est hors limite.");
            }
        }
    }
    public record SubmitRequest(Map<String,Object> answers, boolean anonymous, Long customerId, String email, LocalDateTime startedAt) {}
    public record ResponseResult(Long id, LocalDateTime completedAt, String action) {}
    public record ResponseView(Long id, boolean anonymous, String customerName, String answers, LocalDateTime completedAt, String triggeredAction) {}
}
