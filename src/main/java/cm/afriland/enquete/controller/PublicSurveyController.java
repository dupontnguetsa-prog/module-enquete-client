package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.SurveyResponseService;
import cm.afriland.enquete.Service.RealtimeService;
import cm.afriland.enquete.Service.SurveyService;
import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyDeliveryEvent;
import cm.afriland.enquete.repository.SurveyDeliveryEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController @RequestMapping("/api/public/surveys")
public class PublicSurveyController {
    private final SurveyService surveys; private final SurveyResponseService responses; private final SurveyDeliveryEventRepository events; private final RealtimeService realtime;
    public PublicSurveyController(SurveyService surveys,SurveyResponseService responses,SurveyDeliveryEventRepository events,RealtimeService realtime){this.surveys=surveys;this.responses=responses;this.events=events;this.realtime=realtime;}
    @GetMapping("/{key}") public ResponseEntity<?> get(@PathVariable String key){return surveys.publicByKey(key).map(v->ResponseEntity.ok(v)).orElseGet(()->ResponseEntity.notFound().build());}
    @PostMapping("/{key}/view") public ResponseEntity<?> view(@PathVariable String key,@RequestBody(required=false) Map<String,Object> body){Survey s=surveys.requirePublicSurvey(key); SurveyDeliveryEvent e=new SurveyDeliveryEvent();e.setSurvey(s);e.setEventType("VIEWED");e.setChannel(body==null?null:String.valueOf(body.get("channel")));e.setPageUrl(body==null?null:String.valueOf(body.get("pageUrl")));e.setOccurredAt(LocalDateTime.now());events.save(e);realtime.publish("analytics","view",Map.of("surveyId",s.getId()));return ResponseEntity.ok(Map.of("ok",true));}
    @PostMapping("/{key}/responses") public ResponseEntity<?> submit(@PathVariable String key,@RequestBody SurveyResponseService.SubmitRequest body){var result=responses.submit(key,body);realtime.publish("analytics","analytics",Map.of("surveyKey",key));return ResponseEntity.ok(result);}
}
