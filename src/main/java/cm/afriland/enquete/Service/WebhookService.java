package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyDeliveryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

@Service
public class WebhookService {
    private final ObjectMapper mapper; private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    public WebhookService(ObjectMapper mapper){this.mapper=mapper;}
    public void dispatch(Survey survey, SurveyDeliveryEvent event, String url){
        if(url==null||url.isBlank()){event.setDeliveryStatus("IGNORED");return;}
        try{
            HttpRequest request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("surveyId",survey.getId(),"eventType",event.getEventType(),"occurredAt",event.getOccurredAt())))).build();
            int status=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
            if(status>=200&&status<300){event.setDeliveryStatus("SENT");event.setErrorMessage(null);}else{event.setDeliveryStatus("FAILED");event.setErrorMessage("HTTP "+status);}
        }catch(Exception ex){event.setDeliveryStatus("FAILED");event.setErrorMessage(ex.getMessage()==null?"Webhook inaccessible":ex.getMessage());}
    }
}
