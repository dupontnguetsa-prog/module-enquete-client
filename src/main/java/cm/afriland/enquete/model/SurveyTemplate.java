package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_templates")
public class SurveyTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_id", nullable = false) private User owner;
    @Column(nullable = false, length = 180) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @PrePersist void create(){createdAt=LocalDateTime.now();updatedAt=createdAt;}
    @PreUpdate void update(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} @com.fasterxml.jackson.annotation.JsonIgnore public User getOwner(){return owner;} public void setOwner(User v){owner=v;}
    public String getName(){return name;} public void setName(String v){name=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getContent(){return content;} public void setContent(String v){content=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
