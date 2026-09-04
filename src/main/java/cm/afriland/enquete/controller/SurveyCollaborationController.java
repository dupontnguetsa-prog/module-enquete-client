package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.*;
import cm.afriland.enquete.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SurveyCollaborationController {
    private final SurveyCollaborationService service; private final UserService users;
    public SurveyCollaborationController(SurveyCollaborationService s,UserService u){service=s;users=u;}
    private Long current(HttpServletRequest r){User u=AuthContext.currentUser(r,users);if(u==null)throw new SurveyController.UnauthorizedException();return u.getId();}
    @GetMapping({"/survey-templates","/templates"}) public List<?> templates(HttpServletRequest r){return service.templates(current(r));}
    @PostMapping({"/survey-templates","/templates"}) public Object createTemplate(@RequestBody SurveyCollaborationService.TemplateRequest b,HttpServletRequest r){return service.saveTemplate(current(r),null,b);}
    @PutMapping("/survey-templates/{id}") public Object updateTemplate(@PathVariable Long id,@RequestBody SurveyCollaborationService.TemplateRequest b,HttpServletRequest r){return service.saveTemplate(current(r),id,b);}
    @DeleteMapping("/survey-templates/{id}") public void deleteTemplate(@PathVariable Long id,HttpServletRequest r){service.deleteTemplate(current(r),id);}
    @GetMapping({"/question-bank","/questions/bank"}) public List<?> bank(HttpServletRequest r){return service.bank(current(r));}
    @PostMapping({"/question-bank","/questions/bank"}) public Object createBank(@RequestBody SurveyCollaborationService.BankRequest b,HttpServletRequest r){return service.saveBank(current(r),null,b);}
    @PutMapping("/question-bank/{id}") public Object updateBank(@PathVariable Long id,@RequestBody SurveyCollaborationService.BankRequest b,HttpServletRequest r){return service.saveBank(current(r),id,b);}
    @DeleteMapping("/question-bank/{id}") public void deleteBank(@PathVariable Long id,HttpServletRequest r){service.deleteBank(current(r),id);}
    @GetMapping("/surveys/{id}/versions") public List<?> versions(@PathVariable Long id,HttpServletRequest r){return service.versions(current(r),id);}
    @PostMapping("/surveys/{id}/versions") public Object version(@PathVariable Long id,@RequestBody SurveyCollaborationService.VersionRequest b,HttpServletRequest r){return service.createVersion(current(r),id,b);}
    @GetMapping("/surveys/{id}/comments") public List<?> comments(@PathVariable Long id,HttpServletRequest r){return service.comments(current(r),id);}
    @PostMapping("/surveys/{id}/comments") public Object comment(@PathVariable Long id,@RequestBody SurveyCollaborationService.CommentRequest b,HttpServletRequest r){return service.addComment(current(r),id,b);}
    @PatchMapping("/surveys/{id}/comments/{commentId}") public Object resolve(@PathVariable Long id,@PathVariable Long commentId,@RequestParam boolean resolved,HttpServletRequest r){return service.resolve(current(r),id,commentId,resolved);}
    @GetMapping({"/surveys/{id}/publication-validation","/surveys/{id}/validate-publication"}) public Object validate(@PathVariable Long id,HttpServletRequest r){return service.validate(current(r),id);}
}
