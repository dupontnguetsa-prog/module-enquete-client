package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_conversations")
public class SupportConversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200) private String subject;
    @Column(name = "visitor_name", length = 160) private String visitorName;
    @Column(name = "visitor_email", length = 180) private String visitorEmail;
    @Column(name = "visitor_key", nullable = false, length = 120) private String visitorKey;
    @Column(nullable = false, length = 30) private String status = "OPEN";
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC") private List<SupportMessage> messages = new ArrayList<>();

    @PrePersist void created() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void updated() { updatedAt = Instant.now(); }
    public Long getId() { return id; }
    public String getSubject() { return subject; } public void setSubject(String v) { subject = v; }
    public String getVisitorName() { return visitorName; } public void setVisitorName(String v) { visitorName = v; }
    public String getVisitorEmail() { return visitorEmail; } public void setVisitorEmail(String v) { visitorEmail = v; }
    public String getVisitorKey() { return visitorKey; } public void setVisitorKey(String v) { visitorKey = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
    public List<SupportMessage> getMessages() { return messages; }
}
