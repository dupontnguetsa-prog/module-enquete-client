package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="survey_id", nullable=false) private Survey survey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="customer_id") private CustomerProfile customer;
    @Column(columnDefinition="TEXT", nullable=false) private String answers;
    @Column(nullable=false) private boolean anonymous;
    @Column(nullable=false) private LocalDateTime startedAt;
    @Column(nullable=false) private LocalDateTime completedAt;
    @Column(length=240) private String triggeredAction;
    @Column(name="respondent_email", length=180) private String respondentEmail;
    public Long getId(){return id;} public Survey getSurvey(){return survey;} public void setSurvey(Survey v){survey=v;}
    public CustomerProfile getCustomer(){return customer;} public void setCustomer(CustomerProfile v){customer=v;} public String getAnswers(){return answers;} public void setAnswers(String v){answers=v;}
    public boolean isAnonymous(){return anonymous;} public void setAnonymous(boolean v){anonymous=v;} public LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(LocalDateTime v){startedAt=v;}
    public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
    public String getTriggeredAction(){return triggeredAction;} public void setTriggeredAction(String v){triggeredAction=v;}
    public String getRespondentEmail(){return respondentEmail;} public void setRespondentEmail(String v){respondentEmail=v;}
}
