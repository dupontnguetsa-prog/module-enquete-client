package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyLogicRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyLogicRuleRepository extends JpaRepository<SurveyLogicRule, Long> {
    void deleteAllBySurvey(Survey survey);
}
