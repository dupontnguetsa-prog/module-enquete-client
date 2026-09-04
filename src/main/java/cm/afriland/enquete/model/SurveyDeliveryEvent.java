package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="survey_delivery_events")
public class SurveyDeliveryEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="survey_id", nullable=false) private Survey survey;
    @Column(nullable=false, length=40) private String eventType;
    @Column(length=180) private String channel;
    @Column(length=180) private String externalEvent;
    @Column(length=180) private String pageUrl;
    @Column(nullable=false) private LocalDateTime occurredAt;
    @Column(length=20) private String deliveryStatus;
    @Column(nullable=false) private int retryCount;
    @Column(length=500) private String errorMessage;
    @Column(length=500) private String webhookUrl;
    public Long getId(){return id;} public Survey getSurvey(){return survey;} public void setSurvey(Survey v){survey=v;} public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public String getChannel(){return channel;} public void setChannel(String v){channel=v;} public String getExternalEvent(){return externalEvent;} public void setExternalEvent(String v){externalEvent=v;} public String getPageUrl(){return pageUrl;} public void setPageUrl(String v){pageUrl=v;} public LocalDateTime getOccurredAt(){return occurredAt;} public void setOccurredAt(LocalDateTime v){occurredAt=v;} public String getDeliveryStatus(){return deliveryStatus;} public void setDeliveryStatus(String v){deliveryStatus=v;} public int getRetryCount(){return retryCount;} public void setRetryCount(int v){retryCount=v;} public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){errorMessage=v;} public String getWebhookUrl(){return webhookUrl;} public void setWebhookUrl(String v){webhookUrl=v;}
}
