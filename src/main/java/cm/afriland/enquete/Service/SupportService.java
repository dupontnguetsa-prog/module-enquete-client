package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.*;
import cm.afriland.enquete.repository.*;
import cm.afriland.enquete.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SupportService {
    private final SupportConversationRepository conversations;
    private final SupportMessageRepository messages;
    public SupportService(SupportConversationRepository c, SupportMessageRepository m) { conversations = c; messages = m; }

    @Transactional
    public SupportConversationResponse create(String subject, String initialMessage, String name, String email, String visitorKey) {
        if ((subject == null || subject.isBlank()) && (initialMessage == null || initialMessage.isBlank())) throw new IllegalArgumentException("subject or message is required");
        if (visitorKey == null || visitorKey.isBlank()) throw new IllegalArgumentException("visitorKey is required");
        String text = initialMessage == null || initialMessage.isBlank() ? (subject == null ? "" : subject.trim()) : initialMessage.trim();
        String title = subject == null || subject.isBlank() ? text.substring(0, Math.min(200, text.length())) : subject.trim();
        SupportConversation c = new SupportConversation(); c.setSubject(title); c.setVisitorName(name); c.setVisitorEmail(email); c.setVisitorKey(visitorKey);
        conversations.save(c); add(c, "USER", null, text); return response(c);
    }
    @Transactional
    public SupportConversationResponse addVisitorMessage(Long id, String visitorKey, String content) {
        SupportConversation c = get(id);
        if (!c.getVisitorKey().equals(visitorKey)) throw new IllegalArgumentException("visitorKey does not match this conversation");
        add(c, "USER", null, content); c.setStatus("OPEN"); return response(c);
    }
    @Transactional
    public SupportConversationResponse reply(Long id, User user, String content) {
        SupportConversation c = get(id); add(c, "AGENT", user, content); c.setStatus("ANSWERED"); return response(c);
    }
    @Transactional(readOnly = true) public List<SupportConversationResponse> inbox() {
        return conversations.findAllByOrderByUpdatedAtDesc().stream().map(this::response).toList();
    }
    @Transactional(readOnly = true) public SupportConversationResponse byVisitorKey(String visitorKey) {
        if (visitorKey == null || visitorKey.isBlank()) throw new IllegalArgumentException("visitorKey is required");
        return conversations.findTopByVisitorKeyOrderByUpdatedAtDesc(visitorKey)
                .map(this::response)
                .orElseThrow(() -> new java.util.NoSuchElementException("Conversation introuvable"));
    }
    @Transactional(readOnly = true) public SupportConversation get(Long id) {
        return conversations.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Conversation introuvable"));
    }
    @Transactional
    public void deleteConversation(Long conversationId) {
        SupportConversation conversation = conversations.findById(conversationId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Conversation introuvable"));
        conversations.delete(conversation);
    }
    private void add(SupportConversation c, String type, User user, String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is required");
        SupportMessage m = new SupportMessage(); m.setConversation(c); m.setSenderType(type); m.setSenderUser(user); m.setContent(content.trim()); messages.save(m);
        c.getMessages().add(m); conversations.save(c);
    }

    private SupportConversationResponse response(SupportConversation c) {
        List<SupportMessageResponse> messageResponses = c.getMessages().stream()
                .map(m -> new SupportMessageResponse(
                        m.getId(), m.getContent(), m.getSenderType(),
                        m.getSenderUser() == null ? null : displayName(m.getSenderUser()),
                        m.getCreatedAt()))
                .toList();
        return new SupportConversationResponse(c.getId(), c.getSubject(), c.getVisitorName(),
                c.getVisitorEmail(), c.getStatus(), c.getCreatedAt(), c.getUpdatedAt(), messageResponses);
    }

    private String displayName(User user) {
        return user.getNom() == null || user.getNom().isBlank() ? user.getIdentifiant() : user.getNom();
    }
}
