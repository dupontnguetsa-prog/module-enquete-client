package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "surveys")
public class Survey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_key", unique = true, nullable = false, length = 32)
    private String publicKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false, length = 60) private String goal;
    @Column(nullable = false, length = 40) private String status;
    @Column(name = "trigger_kind", nullable = false, length = 80) private String triggerKind;
    @Column(name = "trigger_config", columnDefinition = "TEXT") private String triggerConfig;
    @Column(name = "audience_mode", nullable = false, length = 30) private String audienceMode;
    @Column(name = "audience_name", nullable = false, length = 160) private String audienceName;
    @Column(name = "audience_filters", columnDefinition = "TEXT") private String audienceFilters;
    @Column(columnDefinition = "TEXT") private String channels;
    @Column(columnDefinition = "TEXT") private String settings;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC") private List<SurveyQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC") private Set<SurveyLogicRule> logicRules = new LinkedHashSet<>();

    @PrePersist void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (publicKey == null || publicKey.isBlank()) publicKey = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }

    public void addQuestion(SurveyQuestion q) { questions.add(q); q.setSurvey(this); }
    public void addLogicRule(SurveyLogicRule r) { logicRules.add(r); r.setSurvey(this); }
    public void clearQuestions() { questions.clear(); }
    public void clearLogicRules() { logicRules.clear(); }

    public Long getId() { return id; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTriggerKind() { return triggerKind; }
    public void setTriggerKind(String triggerKind) { this.triggerKind = triggerKind; }
    public String getTriggerConfig() { return triggerConfig; }
    public void setTriggerConfig(String triggerConfig) { this.triggerConfig = triggerConfig; }
    public String getAudienceMode() { return audienceMode; }
    public void setAudienceMode(String audienceMode) { this.audienceMode = audienceMode; }
    public String getAudienceName() { return audienceName; }
    public void setAudienceName(String audienceName) { this.audienceName = audienceName; }
    public String getAudienceFilters() { return audienceFilters; }
    public void setAudienceFilters(String audienceFilters) { this.audienceFilters = audienceFilters; }
    public String getChannels() { return channels; }
    public void setChannels(String channels) { this.channels = channels; }
    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public List<SurveyQuestion> getQuestions() { return questions; }
    public Set<SurveyLogicRule> getLogicRules() { return logicRules; }
}
