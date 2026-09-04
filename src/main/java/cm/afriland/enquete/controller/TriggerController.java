package cm.afriland.enquete.controller;

import cm.afriland.enquete.model.Survey;
import cm.afriland.enquete.model.SurveyDeliveryEvent;
import cm.afriland.enquete.repository.SurveyDeliveryEventRepository;
import cm.afriland.enquete.repository.SurveyRepository;
import cm.afriland.enquete.repository.CustomerProfileRepository;
import cm.afriland.enquete.model.CustomerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import cm.afriland.enquete.Service.ApiKeyService;

@RestController @RequestMapping("/api/public/triggers")
public class TriggerController {
    private final SurveyRepository surveys; private final SurveyDeliveryEventRepository events; private final ObjectMapper mapper; private final CustomerProfileRepository customers; private final ApiKeyService apiKeys; private final cm.afriland.enquete.Service.WebhookService webhooks;
    public TriggerController(SurveyRepository surveys,SurveyDeliveryEventRepository events,ObjectMapper mapper,CustomerProfileRepository customers,ApiKeyService apiKeys,cm.afriland.enquete.Service.WebhookService webhooks){this.surveys=surveys;this.events=events;this.mapper=mapper;this.customers=customers;this.apiKeys=apiKeys;this.webhooks=webhooks;}
    @PostMapping("/{key}/event") public ResponseEntity<?> event(@PathVariable String key,@RequestBody Map<String,Object> body,HttpServletRequest request){if(apiKeys.authenticate(request.getHeader("X-API-Key")).isEmpty())return ResponseEntity.status(401).body(Map.of("error","Clé API invalide."));Survey s=surveys.findByPublicKey(key).orElseThrow();if(!"Active".equals(s.getStatus()))return ResponseEntity.ok(Map.of("triggered",false));Map<String,Object> cfg=parse(s.getTriggerConfig());String wanted=String.valueOf(cfg.getOrDefault("eventName",""));String incoming=String.valueOf(body.getOrDefault("eventName",""));if(!"API_EVENT".equals(s.getTriggerKind())||!wanted.equals(incoming))return ResponseEntity.ok(Map.of("triggered",false));recordEvent(s,"EVENT",String.valueOf(body.getOrDefault("channel","api")),incoming,null);return ResponseEntity.ok(Map.of("triggered",true,"publicKey",s.getPublicKey()));}
    @PostMapping("/{key}/page-visit") public ResponseEntity<?> page(@PathVariable String key,@RequestBody Map<String,Object> body){Survey s=surveys.findByPublicKey(key).orElseThrow();if(!"Active".equals(s.getStatus()))return ResponseEntity.ok(Map.of("triggered",false));Map<String,Object> cfg=parse(s.getTriggerConfig());String url=String.valueOf(body.getOrDefault("pageUrl",""));String wanted=String.valueOf(cfg.getOrDefault("url",""));if(!"PAGE_VISIT".equals(s.getTriggerKind())||(!wanted.isBlank()&&!url.contains(wanted)))return ResponseEntity.ok(Map.of("triggered",false));recordEvent(s,"PAGE_VISIT","widget",null,url);return ResponseEntity.ok(Map.of("triggered",true,"publicKey",s.getPublicKey()));}
    @PostMapping("/{key}/time-on-page") public ResponseEntity<?> time(@PathVariable String key,@RequestBody Map<String,Object> body){Survey s=surveys.findByPublicKey(key).orElseThrow();Map<String,Object> cfg=parse(s.getTriggerConfig());int elapsed=((Number)body.getOrDefault("seconds",0)).intValue();int minimum=((Number)cfg.getOrDefault("seconds",30)).intValue();if(!"TIME_ON_PAGE".equals(s.getTriggerKind())||elapsed<minimum)return ResponseEntity.ok(Map.of("triggered",false));recordEvent(s,"TIME_ON_PAGE","widget",null,String.valueOf(body.getOrDefault("pageUrl","")));return ResponseEntity.ok(Map.of("triggered",true,"publicKey",s.getPublicKey()));}

    @PostMapping("/{key}/audience-entry") public ResponseEntity<?> audienceEntry(@PathVariable String key,@RequestBody Map<String,Object> body){
        Survey s=surveys.findByPublicKey(key).orElseThrow(); if(!"Active".equals(s.getStatus())||!"AUDIENCE_ENTRY".equals(s.getTriggerKind()))return ResponseEntity.ok(Map.of("triggered",false));
        Long id=body.get("customerId") instanceof Number n?n.longValue():null; if(id==null)return ResponseEntity.ok(Map.of("triggered",false));
        CustomerProfile c=customers.findById(id).orElse(null); if(c==null)return ResponseEntity.ok(Map.of("triggered",false));
        Object raw=null; try{raw=mapper.readValue(s.getAudienceFilters()==null?"[]":s.getAudienceFilters(),java.util.List.class);}catch(Exception ignored){}
        java.util.List<Map<String,Object>> filters=raw instanceof java.util.List<?> list?list.stream().filter(x->x instanceof Map<?,?>).map(x->mapper.convertValue(x,new TypeReference<Map<String,Object>>(){})).toList():java.util.List.of();
        for(Map<String,Object> f:filters){String field=String.valueOf(f.getOrDefault("field",""));String wanted=String.valueOf(f.getOrDefault("value",""));String actual=switch(field){case "customerType"->c.getCustomerType();case "agency"->c.getAgency();case "city"->c.getCity();case "relationshipStatus"->c.getRelationshipStatus();case "product"->c.getProduct();default->null;};if(actual==null||!actual.equalsIgnoreCase(wanted))return ResponseEntity.ok(Map.of("triggered",false));}
        recordEvent(s,"AUDIENCE_ENTRY","audience",null,null); return ResponseEntity.ok(Map.of("triggered",true,"publicKey",s.getPublicKey()));
    }
    @PostMapping("/{key}/manual") public ResponseEntity<?> manual(@PathVariable String key){Survey s=surveys.findByPublicKey(key).orElseThrow();if(!"Active".equals(s.getStatus()))return ResponseEntity.ok(Map.of("triggered",false));recordEvent(s,"MANUAL","manual",null,null);return ResponseEntity.ok(Map.of("triggered",true,"publicKey",s.getPublicKey()));}
    private void recordEvent(Survey s,String type,String channel,String external,String page){SurveyDeliveryEvent e=new SurveyDeliveryEvent();e.setSurvey(s);e.setEventType(type);e.setChannel(channel);e.setExternalEvent(external);e.setPageUrl(page);e.setOccurredAt(LocalDateTime.now());e.setDeliveryStatus("SENT");e.setRetryCount(0);e.setWebhookUrl(String.valueOf(parse(s.getTriggerConfig()).getOrDefault("webhookUrl","")));events.save(e);webhooks.dispatch(s,e,e.getWebhookUrl());events.save(e);}
    private Map<String,Object> parse(String json){if(json==null||json.isBlank())return Map.of();try{return mapper.readValue(json,new TypeReference<>(){});}catch(Exception e){return Map.of();}}
}
