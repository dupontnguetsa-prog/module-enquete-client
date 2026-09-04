package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="collaborator_comments")
public class CollaboratorComment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="survey_id", nullable=false) private Survey survey;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="author_id", nullable=false) private User author;
    @Column(nullable=false, columnDefinition="TEXT") private String body;
    @Column(name="question_id") private Long questionId;
    @Column(nullable=false) private boolean resolved;
    @Column(nullable=false) private LocalDateTime createdAt;
    @Column(nullable=false) private LocalDateTime updatedAt;
    @PrePersist void create(){createdAt=LocalDateTime.now();updatedAt=createdAt;} @PreUpdate void update(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} @com.fasterxml.jackson.annotation.JsonIgnore public Survey getSurvey(){return survey;} public void setSurvey(Survey v){survey=v;} @com.fasterxml.jackson.annotation.JsonIgnore public User getAuthor(){return author;} public void setAuthor(User v){author=v;}
    public String getBody(){return body;} public void setBody(String v){body=v;} public Long getQuestionId(){return questionId;} public void setQuestionId(Long v){questionId=v;} public boolean isResolved(){return resolved;} public void setResolved(boolean v){resolved=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
