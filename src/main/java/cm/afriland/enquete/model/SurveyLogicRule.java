package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_logic_rules")
public class SurveyLogicRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "source_question_id") private SurveyQuestion sourceQuestion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "target_question_id") private SurveyQuestion targetQuestion;
    @Column(nullable = false, length = 40) private String kind;
    @Column(nullable = false, length = 80) private String operator;
    @Column(columnDefinition = "TEXT") private String value;
    @Column(columnDefinition = "TEXT") private String action;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    public SurveyQuestion getSourceQuestion() { return sourceQuestion; }
    public void setSourceQuestion(SurveyQuestion sourceQuestion) { this.sourceQuestion = sourceQuestion; }
    public SurveyQuestion getTargetQuestion() { return targetQuestion; }
    public void setTargetQuestion(SurveyQuestion targetQuestion) { this.targetQuestion = targetQuestion; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
