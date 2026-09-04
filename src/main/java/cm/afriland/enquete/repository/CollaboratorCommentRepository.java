package cm.afriland.enquete.repository;
import cm.afriland.enquete.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CollaboratorCommentRepository extends JpaRepository<CollaboratorComment,Long>{ List<CollaboratorComment> findAllBySurveyOrderByCreatedAtAsc(Survey s); Optional<CollaboratorComment> findByIdAndSurvey(Long id,Survey s); }
