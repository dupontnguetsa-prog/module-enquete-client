package cm.afriland.enquete.repository;
import cm.afriland.enquete.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate,Long>{ List<SurveyTemplate> findAllByOwnerOrderByUpdatedAtDesc(User owner); Optional<SurveyTemplate> findByIdAndOwner(Long id,User owner); }
