package cm.afriland.enquete.repository;
import cm.afriland.enquete.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SurveyVersionRepository extends JpaRepository<SurveyVersion,Long>{ List<SurveyVersion> findAllBySurveyOrderByVersionNumberDesc(Survey s); Optional<SurveyVersion> findBySurveyAndVersionNumber(Survey s,int n); int countBySurvey(Survey s); }
