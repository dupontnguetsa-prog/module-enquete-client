package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_bank_items")
public class QuestionBankItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_id", nullable = false) private User owner;
    @Column(nullable = false, length = 40) private String type;
    @Column(nullable = false, columnDefinition = "TEXT") private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(columnDefinition = "TEXT") private String options;
    @Column(nullable = false) private boolean required;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void create(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} @com.fasterxml.jackson.annotation.JsonIgnore public User getOwner(){return owner;} public void setOwner(User v){owner=v;} public String getType(){return type;} public void setType(String v){type=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getOptions(){return options;} public void setOptions(String v){options=v;} public boolean isRequired(){return required;} public void setRequired(boolean v){required=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
