package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.SupportConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Long> {
    @EntityGraph(attributePaths = {"messages", "messages.senderUser"})
    List<SupportConversation> findAllByOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = {"messages", "messages.senderUser"})
    java.util.Optional<SupportConversation> findById(Long id);

    @EntityGraph(attributePaths = {"messages", "messages.senderUser"})
    java.util.Optional<SupportConversation> findTopByVisitorKeyOrderByUpdatedAtDesc(String visitorKey);
}
