package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.SurveyAnalyticsService;
import cm.afriland.enquete.Service.SurveyResponseService;
import cm.afriland.enquete.Service.SurveyService;
import cm.afriland.enquete.Service.UserService;
import cm.afriland.enquete.Service.SurveyMailService;
import cm.afriland.enquete.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController @RequestMapping("/api/surveys")
public class SurveyController {
    private final SurveyService surveys; private final SurveyAnalyticsService analytics; private final SurveyResponseService responses; private final UserService users; private final SurveyMailService mail;
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    public SurveyController(SurveyService surveys,SurveyAnalyticsService analytics,SurveyResponseService responses,UserService users,SurveyMailService mail){this.surveys=surveys;this.analytics=analytics;this.responses=responses;this.users=users;this.mail=mail;}
    private User current(HttpServletRequest req){User u=AuthContext.currentUser(req,users);if(u==null)throw new UnauthorizedException();return u;}
    @GetMapping public Object list(@RequestParam(required = false) String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,HttpServletRequest r){int safePage=Math.max(0,page);int safeSize=Math.min(Math.max(1,size),100);return surveys.listPage(current(r).getId(),status,PageRequest.of(safePage,safeSize,Sort.by(Sort.Direction.DESC,"updatedAt")));}
    @GetMapping("/{id}") public Object get(@PathVariable Long id,HttpServletRequest r){return surveys.get(current(r).getId(),id);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','EDITOR')")
    @PostMapping public Object create(@RequestBody SurveyService.SurveyRequest body,HttpServletRequest r){return surveys.create(current(r).getId(),body);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','EDITOR')")
    @PutMapping("/{id}") public Object update(@PathVariable Long id,@RequestBody SurveyService.SurveyRequest body,HttpServletRequest r){return surveys.update(current(r).getId(),id,body);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','EDITOR')")
    @PostMapping("/{id}/save-draft") public Object draft(@PathVariable Long id,@RequestBody SurveyService.SurveyRequest body,HttpServletRequest r){return surveys.saveDraft(current(r).getId(),id,body);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/{id}/publish") public Object publish(@PathVariable Long id,@RequestBody(required=false) SurveyService.SurveyRequest body,HttpServletRequest r){Long userId=current(r).getId();return body==null?surveys.publish(userId,id):surveys.publish(userId,id,body);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/{id}/send") public ResponseEntity<Void> send(@PathVariable Long id,@RequestBody SendRequest body,HttpServletRequest r){User user=current(r);mail.send(surveys.requirePermission(user.getId(),id,"OWNER","ADMIN"),body.email(),frontendUrl.replaceAll("/+$",""));return ResponseEntity.noContent().build();}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/{id}/pause") public Object pause(@PathVariable Long id,HttpServletRequest r){return surveys.pause(current(r).getId(),id);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/{id}/resume") public Object resume(@PathVariable Long id,HttpServletRequest r){return surveys.resume(current(r).getId(),id);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/{id}/archive") public Object archive(@PathVariable Long id,HttpServletRequest r){return surveys.archive(current(r).getId(),id);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','EDITOR')")
    @PostMapping("/{id}/duplicate") public Object duplicate(@PathVariable Long id,HttpServletRequest r){return surveys.duplicate(current(r).getId(),id);}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id,HttpServletRequest r){surveys.delete(current(r).getId(),id);return ResponseEntity.noContent().build();}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','ANALYST')")
    @GetMapping("/{id}/responses") public Object responses(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size,HttpServletRequest r){int safePage=Math.max(0,page);int safeSize=Math.min(Math.max(1,size),100);return responses.listOwned(current(r).getId(),id,surveys,PageRequest.of(safePage,safeSize, Sort.by(Sort.Direction.DESC,"completedAt")));}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','ANALYST')")
    @GetMapping("/{id}/responses/export") public ResponseEntity<byte[]> exportResponses(@PathVariable Long id,HttpServletRequest r){return ResponseEntity.ok().header("Content-Type","text/csv; charset=UTF-8").header("Content-Disposition","attachment; filename=\"enquete-"+id+"-reponses.csv\"").body(responses.exportCsv(current(r).getId(),id,surveys));}
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','ANALYST')")
    @GetMapping("/{id}/analytics") public Object analytics(@PathVariable Long id,@RequestParam(required=false) Integer days,@RequestParam(required=false) String audienceField,@RequestParam(required=false) String audienceValue,HttpServletRequest r){return analytics.analytics(current(r).getId(),id,surveys,days,audienceField,audienceValue);}
    @GetMapping("/{id}/delivery-logs") public Object deliveryLogs(@PathVariable Long id,HttpServletRequest r){return surveys.deliveryLogs(current(r).getId(),id);}
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED) public static class UnauthorizedException extends RuntimeException{}
    public record SendRequest(String email) {}
}
