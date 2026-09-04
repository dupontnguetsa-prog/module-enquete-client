package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.time.LocalDateTime;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    long countBySurvey(Survey survey);
    List<SurveyResponse> findAllBySurveyOrderByCompletedAtDesc(Survey survey);
    List<SurveyResponse> findAllBySurveyAndCompletedAtBetweenOrderByCompletedAtDesc(Survey survey, LocalDateTime from, LocalDateTime to);
    Page<SurveyResponse> findAllBySurveyOrderByCompletedAtDesc(Survey survey, Pageable pageable);
    boolean existsBySurveyAndCustomer(Survey survey, cm.afriland.enquete.model.CustomerProfile customer);
    boolean existsBySurveyAndRespondentEmailIgnoreCase(Survey survey, String respondentEmail);
    void deleteAllBySurvey(Survey survey);
}
