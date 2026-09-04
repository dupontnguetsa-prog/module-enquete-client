package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    @Override
    @EntityGraph(attributePaths = {"workspace", "team", "owner", "questions", "logicRules"})
    Optional<Survey> findById(Long id);
    @EntityGraph(attributePaths = {"questions", "logicRules", "owner"})
    List<Survey> findAllByOwnerOrderByUpdatedAtDesc(User owner);
    @EntityGraph(attributePaths = {"questions", "logicRules", "owner"})
    List<Survey> findAllByOwnerAndStatusOrderByUpdatedAtDesc(User owner, String status);
    @EntityGraph(attributePaths = {"questions", "logicRules", "owner"})
    Page<Survey> findAllByOwner(User owner, Pageable pageable);
    @EntityGraph(attributePaths = {"questions", "logicRules", "owner"})
    Page<Survey> findAllByOwnerAndStatus(User owner, String status, Pageable pageable);
    @EntityGraph(attributePaths = {"questions", "logicRules", "owner"})
    Optional<Survey> findByIdAndOwner(Long id, User owner);
    @EntityGraph(attributePaths = {"questions", "logicRules", "owner"})
    Optional<Survey> findByPublicKey(String publicKey);
    List<Survey> findAllByStatus(String status);
}
