package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyLogicRule;
import cm.afriland.enquete.model.SurveyQuestion;
import cm.afriland.enquete.model.User;
import cm.afriland.enquete.model.Workspace;
import cm.afriland.enquete.repository.SurveyRepository;
import cm.afriland.enquete.repository.SurveyResponseRepository;
import cm.afriland.enquete.repository.SurveyDeliveryEventRepository;
import cm.afriland.enquete.repository.SurveyLogicRuleRepository;
import cm.afriland.enquete.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SurveyResponseRepository responseRepository;
    private final SurveyDeliveryEventRepository deliveryEventRepository;
    private final SurveyLogicRuleRepository logicRuleRepository;
    private final WorkspaceService workspaceService;

    public SurveyService(SurveyRepository surveyRepository, UserRepository userRepository, ObjectMapper objectMapper,
                         SurveyResponseRepository responseRepository, SurveyDeliveryEventRepository deliveryEventRepository,
                         SurveyLogicRuleRepository logicRuleRepository, WorkspaceService workspaceService) {
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.responseRepository = responseRepository;
        this.deliveryEventRepository = deliveryEventRepository;
        this.logicRuleRepository = logicRuleRepository;
        this.workspaceService = workspaceService;
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable."));
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> list(Long userId) {
        return surveyRepository.findAllByOwnerOrderByUpdatedAtDesc(requireUser(userId))
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> list(Long userId, String status) {
        if (status == null || status.isBlank() || "Toutes".equals(status)) return list(userId);
        return surveyRepository.findAllByOwnerAndStatusOrderByUpdatedAtDesc(requireUser(userId), status)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<SurveyResponse> listPage(Long userId, String status, Pageable pageable) {
        User owner = requireUser(userId);
        Page<Survey> page = status == null || status.isBlank() || "Toutes".equals(status)
            ? surveyRepository.findAllByOwner(owner, pageable)
            : surveyRepository.findAllByOwnerAndStatus(owner, status, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SurveyResponse get(Long userId, Long surveyId) {
        return toResponse(findOwned(userId, surveyId));
    }

    @Transactional(readOnly = true)
    public List<cm.afriland.enquete.model.SurveyDeliveryEvent> deliveryLogs(Long userId, Long surveyId) {
        return deliveryEventRepository.findAllBySurveyOrderByOccurredAtDesc(findOwned(userId, surveyId));
    }

    @Transactional
    public SurveyResponse create(Long userId, SurveyRequest request) {
        Survey survey = new Survey();
        User owner = requireUser(userId);
        survey.setOwner(owner);
        assignScope(survey, owner, request);
        apply(survey, request);
        return toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyResponse update(Long userId, Long surveyId, SurveyRequest request) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN", "EDITOR");
        if ("Active".equals(survey.getStatus())) {
            throw new IllegalStateException("Une enquête active doit être mise en pause avant modification.");
        }
        apply(survey, request);
        return toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyResponse saveDraft(Long userId, Long surveyId, SurveyRequest request) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN", "EDITOR");
        if ("Active".equals(survey.getStatus()) || "Programmée".equals(survey.getStatus())) {
            throw new IllegalStateException("Mettez l’enquête en pause avant de modifier sa version publiée.");
        }
        apply(survey, request);
        survey.setStatus("Brouillon");
        return toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public void delete(Long userId, Long surveyId) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN");

        // Remove dependent rows first because they are not all covered by JPA cascade.
        logicRuleRepository.deleteAllBySurvey(survey);
        deliveryEventRepository.deleteAllBySurvey(survey);
        responseRepository.deleteAllBySurvey(survey);
        surveyRepository.flush();
        surveyRepository.delete(survey);
        surveyRepository.flush();
    }

    @Transactional
    public SurveyResponse publish(Long userId, Long surveyId) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN");
        return publishSurvey(survey);
    }

    @Transactional
    public SurveyResponse publish(Long userId, Long surveyId, SurveyRequest request) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN");
        apply(survey, request);
        return publishSurvey(survey);
    }

    private SurveyResponse publishSurvey(Survey survey) {
        validateForPublication(survey);
        if (survey.getQuestions().isEmpty()) throw new IllegalArgumentException("Ajoutez au moins une question avant de publier.");
        if (parseList(survey.getChannels()).isEmpty()) throw new IllegalArgumentException("Choisissez au moins un canal de diffusion.");
        survey.setPublishedAt(LocalDateTime.now());
        survey.setStatus(resolvePublishedStatus(survey.getTriggerKind(), parseMap(survey.getTriggerConfig())));
        return toResponse(surveyRepository.save(survey));
    }

    private void validateForPublication(Survey survey) {
        if (survey.getTitle() == null || survey.getTitle().isBlank()) {
            throw new IllegalArgumentException("Donnez un nom à l’enquête avant de publier.");
        }
        if (survey.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Ajoutez au moins une question avant de publier.");
        }
        for (int i = 0; i < survey.getQuestions().size(); i++) {
            SurveyQuestion question = survey.getQuestions().get(i);
            if (question.getTitle() == null || question.getTitle().isBlank()) {
                throw new IllegalArgumentException("La question " + (i + 1) + " doit avoir un texte.");
            }
            if (question.getType() == null || question.getType().isBlank()) {
                throw new IllegalArgumentException("La question " + (i + 1) + " doit avoir un type.");
            }
            if (("SINGLE_CHOICE".equals(question.getType()) || "MULTIPLE_CHOICE".equals(question.getType()))
                    && parseList(question.getOptions()).isEmpty()) {
                throw new IllegalArgumentException("Ajoutez au moins une option à la question " + (i + 1) + ".");
            }
            if (question.getMinValue() > question.getMaxValue()) {
                throw new IllegalArgumentException("La plage de valeurs de la question " + (i + 1) + " est invalide.");
            }
        }
        validateLogicRules(survey);
        if (parseList(survey.getChannels()).isEmpty()) {
            throw new IllegalArgumentException("Choisissez au moins un canal de diffusion.");
        }
        validateTriggerConfig(survey.getTriggerKind(), parseMap(survey.getTriggerConfig()));
        if ("DATE_TIME".equals(survey.getTriggerKind())) {
            Map<String, Object> config = parseMap(survey.getTriggerConfig());
            String date = String.valueOf(config.getOrDefault("date", ""));
            String time = String.valueOf(config.getOrDefault("time", ""));
            if (date.isBlank() || time.isBlank()) {
                throw new IllegalArgumentException("Choisissez une date et une heure de programmation.");
            }
            try {
                String zone = String.valueOf(config.getOrDefault("timezone", "Africa/Douala"));
                ZonedDateTime planned = ZonedDateTime.of(LocalDateTime.parse(date + "T" + time), ZoneId.of(zone));
                if (!planned.isAfter(ZonedDateTime.now(planned.getZone()))) {
                    throw new IllegalArgumentException("La date de programmation doit être dans le futur.");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("La date ou l’heure de programmation est invalide.");
            }
        }

    }

    private void validateTriggerConfig(String triggerKind, Map<String, Object> config) {
        if (triggerKind == null || triggerKind.isBlank()) {
            throw new IllegalArgumentException("Le type de déclenchement est obligatoire.");
        }
        Object webhook = config.get("webhookUrl");
        if (webhook != null && !String.valueOf(webhook).isBlank()) {
            try {
                java.net.URI uri = java.net.URI.create(String.valueOf(webhook));
                if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                    throw new IllegalArgumentException("L’URL du webhook doit utiliser HTTP ou HTTPS.");
                }
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("L’URL du webhook est invalide.");
            }
        }
        switch (triggerKind) {
            case "API_EVENT" -> {
                if (String.valueOf(config.getOrDefault("eventName", "")).isBlank()) {
                    throw new IllegalArgumentException("Indiquez le nom de l’événement API.");
                }
            }
            case "PAGE_VISIT" -> {
                if (String.valueOf(config.getOrDefault("url", "")).isBlank()) {
                    throw new IllegalArgumentException("Indiquez l’URL de la page à surveiller.");
                }
            }
            case "TIME_ON_PAGE" -> {
                Object rawSeconds = config.get("seconds");
                if (!(rawSeconds instanceof Number) || ((Number) rawSeconds).intValue() < 1) {
                    throw new IllegalArgumentException("Le délai de déclenchement doit être supérieur à zéro.");
                }
            }
            case "AUDIENCE_ENTRY", "MANUAL", "DATE_TIME" -> {
                // These trigger types use their dedicated configuration or no extra field.
            }
            default -> throw new IllegalArgumentException("Type de déclenchement inconnu.");
        }
    }

    private void validateLogicRules(Survey survey) {
            Map<Integer, Set<Integer>> branches = new HashMap<>();
            for (SurveyLogicRule rule : survey.getLogicRules()) {
                if (rule.getSourceQuestion() == null || rule.getTargetQuestion() == null) {
                    throw new IllegalArgumentException("Chaque règle doit avoir une question source et une cible.");
                }
                int source = rule.getSourceQuestion().getDisplayOrder();
                int target = rule.getTargetQuestion().getDisplayOrder();
                if (source == target) {
                    throw new IllegalArgumentException("Une règle ne peut pas pointer vers la même question.");
                }
                if (!"BRANCH".equals(rule.getKind())) continue;
                branches.computeIfAbsent(source, ignored -> new HashSet<>()).add(target);
            }

            Set<Integer> visiting = new HashSet<>();
            Set<Integer> visited = new HashSet<>();
            for (Integer question : branches.keySet()) {
                if (hasBranchCycle(question, branches, visiting, visited)) {
                    throw new IllegalArgumentException("Les règles de branchement contiennent une boucle.");
                }
            }
        }

        private boolean hasBranchCycle(
            int question,
            Map<Integer, Set<Integer>> branches,
            Set<Integer> visiting,
            Set<Integer> visited
        ) {
            if (visiting.contains(question)) return true;
            if (visited.contains(question)) return false;
            visiting.add(question);
            for (Integer target : branches.getOrDefault(question, Set.of())) {
                if (hasBranchCycle(target, branches, visiting, visited)) return true;
            }
            visiting.remove(question);
            visited.add(question);
            return false;
        }

    @Transactional
    public SurveyResponse pause(Long userId, Long surveyId) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN");
        if (!"Active".equals(survey.getStatus()) && !"Programmée".equals(survey.getStatus())) {
            throw new IllegalStateException("Cette enquête n’est pas active.");
        }
        survey.setStatus("En pause");
        return toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyResponse resume(Long userId, Long surveyId) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN");
        survey.setStatus(resolvePublishedStatus(survey.getTriggerKind(), parseMap(survey.getTriggerConfig())));
        return toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyResponse archive(Long userId, Long surveyId) {
        Survey survey = requirePermission(userId, surveyId, "OWNER", "ADMIN");
        survey.setStatus("Archivée");
        return toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyResponse duplicate(Long userId, Long surveyId) {
        Survey original = requirePermission(userId, surveyId, "OWNER", "ADMIN", "EDITOR");
        Survey copy = new Survey();
        copy.setOwner(requireUser(userId));
        copy.setTitle(original.getTitle() + " — copie");
        copy.setDescription(original.getDescription());
        copy.setGoal(original.getGoal());
        copy.setStatus("Brouillon");
        copy.setTriggerKind(original.getTriggerKind());
        copy.setTriggerConfig(original.getTriggerConfig());
        copy.setAudienceMode(original.getAudienceMode());
        copy.setAudienceName(original.getAudienceName());
        copy.setAudienceFilters(original.getAudienceFilters());
        copy.setChannels(original.getChannels());
        copy.setSettings(original.getSettings());
        for (SurveyQuestion q : original.getQuestions()) {
            SurveyQuestion nq = cloneQuestion(q);
            copy.addQuestion(nq);
        }
        Map<Long, SurveyQuestion> questionMap = new HashMap<>();
        for (int i = 0; i < original.getQuestions().size(); i++) questionMap.put(original.getQuestions().get(i).getId(), copy.getQuestions().get(i));
        for (SurveyLogicRule r : original.getLogicRules()) {
            SurveyLogicRule nr = new SurveyLogicRule();
            nr.setKind(r.getKind()); nr.setOperator(r.getOperator()); nr.setValue(r.getValue());
            nr.setSourceQuestion(questionMap.get(r.getSourceQuestion() == null ? null : r.getSourceQuestion().getId()));
            nr.setTargetQuestion(questionMap.get(r.getTargetQuestion() == null ? null : r.getTargetQuestion().getId()));
            copy.addLogicRule(nr);
        }
        return toResponse(surveyRepository.save(copy));
    }

    @Transactional(readOnly = true)
    public Optional<SurveyResponse> publicByKey(String publicKey) {
        return surveyRepository.findByPublicKey(publicKey).filter(this::isPubliclyAvailable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Survey requirePublicSurvey(String publicKey) {
        Survey survey = surveyRepository.findByPublicKey(publicKey).orElseThrow(() -> new NoSuchElementException("Enquête introuvable."));
        if (!isPubliclyAvailable(survey)) throw new IllegalStateException("Cette enquête n’est pas disponible.");
        return survey;
    }

    private boolean isPubliclyAvailable(Survey survey) { return "Active".equals(survey.getStatus()); }

    private String resolvePublishedStatus(String triggerKind, Map<String,Object> cfg) {
        if ("DATE_TIME".equals(triggerKind)) {
            String date = String.valueOf(cfg.getOrDefault("date", ""));
            String time = String.valueOf(cfg.getOrDefault("time", "00:00"));
            try {
                LocalDateTime planned = LocalDateTime.parse(LocalDate.parse(date) + "T" + time);
                return planned.isAfter(LocalDateTime.now()) ? "Programmée" : "Active";
            } catch (Exception ignored) { return "Active"; }
        }
        return "Active";
    }

    private SurveyQuestion cloneQuestion(SurveyQuestion q) {
        SurveyQuestion n = new SurveyQuestion();
        n.setDisplayOrder(q.getDisplayOrder()); n.setType(q.getType()); n.setTitle(q.getTitle()); n.setDescription(q.getDescription());
        n.setRequired(q.isRequired()); n.setMinValue(q.getMinValue()); n.setMaxValue(q.getMaxValue()); n.setMinLabel(q.getMinLabel()); n.setMaxLabel(q.getMaxLabel()); n.setOptions(q.getOptions());
        return n;
    }

    public Survey findOwned(Long userId, Long surveyId) {
        User user = requireUser(userId);
        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(() -> new NoSuchElementException("Enquête introuvable."));
        if (survey.getOwner().getId().equals(user.getId())) return survey;
        if (survey.getWorkspace() != null) {
            workspaceService.requireAccessibleWorkspace(userId, survey.getWorkspace().getId());
            return survey;
        }
        if (survey.getTeam() != null && workspaceService.teams(userId).stream().anyMatch(t -> t.id().equals(survey.getTeam().getId()))) return survey;
        throw new NoSuchElementException("Enquête introuvable.");
    }

    public Survey requirePermission(Long userId, Long surveyId, String... allowedRoles) {
        Survey survey = findOwned(userId, surveyId);
        User user = requireUser(userId);
        String role = survey.getOwner().getId().equals(user.getId()) ? "OWNER"
            : survey.getWorkspace() != null ? workspaceService.roleFor(survey.getWorkspace(), user)
            : workspaceService.roleForTeam(survey.getTeam(), user);
        if (role == null || Arrays.stream(allowedRoles).noneMatch(role::equals)) throw new IllegalStateException("Permission insuffisante.");
        return survey;
    }

    private void assignScope(Survey survey, User owner, SurveyRequest request) {
        if (request.workspaceId() != null) {
            Workspace workspace = workspaceService.requireAccessibleWorkspace(owner.getId(), request.workspaceId());
            String role = workspaceService.roleFor(workspace, owner);
            if (!Set.of("OWNER", "ADMIN", "EDITOR").contains(role)) throw new IllegalStateException("Permission insuffisante.");
            survey.setWorkspace(workspace);
            survey.setTeam(workspace.getTeam());
        } else if (request.teamId() != null) {
            survey.setTeam(workspaceService.requireAccessibleTeam(owner.getId(), request.teamId()));
        }
    }

    private void apply(Survey survey, SurveyRequest request) {
        if (request == null) throw new IllegalArgumentException("Données d’enquête invalides.");
        survey.setTitle(required(request.title(), "Le nom de l’enquête est obligatoire."));
        survey.setDescription(request.description());
        survey.setGoal(required(request.goal(), "L’objectif est obligatoire."));
        survey.setStatus(blankToDefault(request.status(), "Brouillon"));
        survey.setTriggerKind(required(request.triggerKind(), "Le déclenchement est obligatoire."));
        survey.setTriggerConfig(toJson(request.triggerConfig() == null ? Map.of() : request.triggerConfig()));
        survey.setAudienceMode(required(request.audienceMode(), "Le mode d’audience est obligatoire."));
        survey.setAudienceName(required(request.audienceName(), "L’audience est obligatoire."));
        survey.setAudienceFilters(toJson(request.audienceFilters() == null ? List.of() : request.audienceFilters()));
        survey.setChannels(toJson(request.channels() == null ? List.of() : request.channels()));
        survey.setSettings(toJson(request.settings() == null ? Map.of() : request.settings()));

        survey.clearQuestions();
        List<QuestionRequest> qs = request.questions() == null ? List.of() : request.questions();
        for (int i = 0; i < qs.size(); i++) {
            QuestionRequest q = qs.get(i);
            SurveyQuestion sq = new SurveyQuestion();
            sq.setDisplayOrder(i);
            sq.setType(required(q.type(), "Le type de question est obligatoire."));
            sq.setTitle(required(q.title(), "Le texte de la question est obligatoire."));
            sq.setDescription(q.description());
            sq.setRequired(q.required());
            sq.setMinValue(q.min() == null ? 0 : q.min());
            sq.setMaxValue(q.max() == null ? 10 : q.max());
            sq.setMinLabel(q.minLabel());
            sq.setMaxLabel(q.maxLabel());
            sq.setOptions(toJson(q.options() == null ? List.of() : q.options()));
            survey.addQuestion(sq);
        }

        survey.clearLogicRules();
        if (request.logicRules() != null) {
            for (LogicRuleRequest r : request.logicRules()) {
                if (r.sourceIndex() == null || r.targetIndex() == null) continue;
                if (r.sourceIndex() < 0 || r.targetIndex() < 0 || r.sourceIndex() >= survey.getQuestions().size() || r.targetIndex() >= survey.getQuestions().size()) continue;
                SurveyLogicRule sr = new SurveyLogicRule();
                sr.setKind(required(r.kind(), "Le type de logique est obligatoire."));
                sr.setOperator(required(r.operator(), "L’opérateur est obligatoire."));
                sr.setValue(r.value());
                sr.setAction(r.action());
                sr.setSourceQuestion(survey.getQuestions().get(r.sourceIndex()));
                sr.setTargetQuestion(survey.getQuestions().get(r.targetIndex()));
                survey.addLogicRule(sr);
            }
        }
    }

    private String required(String s, String msg) { if (s == null || s.isBlank()) throw new IllegalArgumentException(msg); return s.trim(); }
    private String blankToDefault(String s, String v) { return s == null || s.isBlank() ? v : s.trim(); }
    private String toJson(Object value) { try { return objectMapper.writeValueAsString(value); } catch (JsonProcessingException e) { throw new IllegalArgumentException("Configuration JSON invalide."); } }

    private SurveyResponse toResponse(Survey survey) {
        List<QuestionResponse> questions = survey.getQuestions().stream().map(q -> new QuestionResponse(
            q.getId(), q.getDisplayOrder(), q.getType(), q.getTitle(), q.getDescription(), q.isRequired(), q.getMinValue(), q.getMaxValue(), q.getMinLabel(), q.getMaxLabel(), parseList(q.getOptions())
        )).toList();
        List<LogicRuleResponse> logic = survey.getLogicRules().stream().map(r -> new LogicRuleResponse(
            r.getId(), r.getKind(), indexOf(survey.getQuestions(), r.getSourceQuestion()), r.getOperator(), r.getValue(), indexOf(survey.getQuestions(), r.getTargetQuestion()), r.getAction()
        )).toList();
        return new SurveyResponse(survey.getId(), survey.getPublicKey(), survey.getTitle(), survey.getDescription(), survey.getGoal(), survey.getStatus(), survey.getTriggerKind(), parseMap(survey.getTriggerConfig()), survey.getAudienceMode(), survey.getAudienceName(), parseListMap(survey.getAudienceFilters()), parseList(survey.getChannels()), parseMap(survey.getSettings()), survey.getCreatedAt(), survey.getUpdatedAt(), survey.getPublishedAt(), questions, logic);
    }

    private int indexOf(List<SurveyQuestion> qs, SurveyQuestion q) { return q == null ? -1 : qs.indexOf(q); }

    private List<String> parseList(String json) { if (json == null || json.isBlank()) return List.of(); try { JavaType t = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class); return objectMapper.readValue(json, t); } catch (Exception e) { return List.of(); } }
    private Map<String,Object> parseMap(String json) { if (json == null || json.isBlank()) return Map.of(); try { JavaType t = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class); return objectMapper.readValue(json, t); } catch (Exception e) { return Map.of(); } }
    private List<Map<String,Object>> parseListMap(String json) { if (json == null || json.isBlank()) return List.of(); try { JavaType t = objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class); return objectMapper.readValue(json, t); } catch (Exception e) { return List.of(); } }

    public record SurveyRequest(String title, String description, String goal, String status, String triggerKind, Map<String,Object> triggerConfig, String audienceMode, String audienceName, List<Map<String,Object>> audienceFilters, List<String> channels, Map<String,Object> settings, List<QuestionRequest> questions, List<LogicRuleRequest> logicRules, Long workspaceId, Long teamId) {
        public SurveyRequest(String title, String description, String goal, String status, String triggerKind, Map<String,Object> triggerConfig, String audienceMode, String audienceName, List<Map<String,Object>> audienceFilters, List<String> channels, Map<String,Object> settings, List<QuestionRequest> questions, List<LogicRuleRequest> logicRules) {
            this(title, description, goal, status, triggerKind, triggerConfig, audienceMode, audienceName, audienceFilters, channels, settings, questions, logicRules, null, null);
        }
    }
    public record QuestionRequest(Long id, String type, String title, String description, boolean required, List<String> options, Integer min, Integer max, String minLabel, String maxLabel) {}
    public record LogicRuleRequest(Integer sourceIndex, String kind, String operator, String value, Integer targetIndex, String action) {}
    public record SurveyResponse(Long id, String publicKey, String title, String description, String goal, String status, String triggerKind, Map<String,Object> triggerConfig, String audienceMode, String audienceName, List<Map<String,Object>> audienceFilters, List<String> channels, Map<String,Object> settings, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime publishedAt, List<QuestionResponse> questions, List<LogicRuleResponse> logicRules) {}
    public record QuestionResponse(Long id, Integer displayOrder, String type, String title, String description, boolean required, Integer min, Integer max, String minLabel, String maxLabel, List<String> options) {}
    public record LogicRuleResponse(Long id, String kind, Integer sourceIndex, String operator, String value, Integer targetIndex, String action) {}
}
