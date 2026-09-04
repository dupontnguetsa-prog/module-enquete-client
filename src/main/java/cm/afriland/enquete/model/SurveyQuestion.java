package cm.afriland.enquete.model;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_questions")
public class SurveyQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false) private Survey survey;
    @Column(name = "display_order", nullable = false) private Integer displayOrder;
    @Column(nullable = false, length = 40) private String type;
    @Column(nullable = false, columnDefinition = "TEXT") private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false) private boolean required;
    @Column(nullable = false) private Integer minValue;
    @Column(nullable = false) private Integer maxValue;
    @Column(name = "min_label", length = 180) private String minLabel;
    @Column(name = "max_label", length = 180) private String maxLabel;
    @Column(columnDefinition = "TEXT") private String options;

    public Long getId() { return id; }
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public Integer getMinValue() { return minValue; }
    public void setMinValue(Integer minValue) { this.minValue = minValue; }
    public Integer getMaxValue() { return maxValue; }
    public void setMaxValue(Integer maxValue) { this.maxValue = maxValue; }
    public String getMinLabel() { return minLabel; }
    public void setMinLabel(String minLabel) { this.minLabel = minLabel; }
    public String getMaxLabel() { return maxLabel; }
    public void setMaxLabel(String maxLabel) { this.maxLabel = maxLabel; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
}
