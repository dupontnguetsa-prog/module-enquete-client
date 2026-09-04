package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_versions", uniqueConstraints = @UniqueConstraint(columnNames={"survey_id","version_number"}))
public class SurveyVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="survey_id", nullable=false) private Survey survey;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="created_by", nullable=false) private User createdBy;
    @Column(name="version_number", nullable=false) private int versionNumber;
    @Column(nullable=false, columnDefinition="TEXT") private String snapshot;
    @Column(length=500) private String changeNote;
    @Column(nullable=false) private LocalDateTime createdAt;
    @PrePersist void create(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} @com.fasterxml.jackson.annotation.JsonIgnore public Survey getSurvey(){return survey;} public void setSurvey(Survey v){survey=v;} @com.fasterxml.jackson.annotation.JsonIgnore public User getCreatedBy(){return createdBy;} public void setCreatedBy(User v){createdBy=v;}
    public int getVersionNumber(){return versionNumber;} public void setVersionNumber(int v){versionNumber=v;} public String getSnapshot(){return snapshot;} public void setSnapshot(String v){snapshot=v;} public String getChangeNote(){return changeNote;} public void setChangeNote(String v){changeNote=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
