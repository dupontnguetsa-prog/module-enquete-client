package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "support_messages")
public class SupportMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conversation_id")
    private SupportConversation conversation;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "sender_type", nullable = false, length = 20) private String senderType;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sender_user_id") private User senderUser;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @PrePersist void created() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public SupportConversation getConversation() { return conversation; } public void setConversation(SupportConversation v) { conversation = v; }
    public String getContent() { return content; } public void setContent(String v) { content = v; }
    public String getSenderType() { return senderType; } public void setSenderType(String v) { senderType = v; }
    public User getSenderUser() { return senderUser; } public void setSenderUser(User v) { senderUser = v; }
    public Instant getCreatedAt() { return createdAt; }
}
