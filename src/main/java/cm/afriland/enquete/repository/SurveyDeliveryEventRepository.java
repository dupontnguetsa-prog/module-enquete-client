package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyDeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface SurveyDeliveryEventRepository extends JpaRepository<SurveyDeliveryEvent, Long> {
    long countBySurveyAndEventType(Survey survey, String eventType);
    long countBySurveyAndEventTypeAndOccurredAtBetween(Survey survey, String eventType, LocalDateTime from, LocalDateTime to);
    List<SurveyDeliveryEvent> findAllBySurveyOrderByOccurredAtDesc(Survey survey);
    void deleteAllBySurvey(Survey survey);
    List<SurveyDeliveryEvent> findTop100ByDeliveryStatusAndRetryCountLessThanOrderByOccurredAtAsc(String status, int retryCount);
}
