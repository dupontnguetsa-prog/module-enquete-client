package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.*;
import cm.afriland.enquete.model.*;
import cm.afriland.enquete.dto.SupportConversationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportController {
    private final SupportService support; private final SupportAiService ai; private final UserService users;
    private final RealtimeService realtime;
    public SupportController(SupportService s, SupportAiService a, UserService u, RealtimeService realtime) { support=s; ai=a; users=u; this.realtime=realtime; }

    public record CreateRequest(@Size(max=200) String subject, @Size(max=10000) String message, @Size(max=160) String visitorName, @Email @Size(max=180) String visitorEmail, @NotBlank @Size(max=120) String visitorKey) {}
    public record MessageRequest(@NotBlank @Size(max=10000) String content, @NotBlank @Size(max=120) String visitorKey) {}
    public record ReplyRequest(@NotBlank @Size(max=10000) String content) {}
    public record AiRequest(@NotBlank @Size(max=10000) String prompt) {}
    @PostMapping("/conversations")
    public ResponseEntity<SupportConversationResponse> create(@Valid @RequestBody CreateRequest r) { SupportConversationResponse result=support.create(r.subject(),r.message(),r.visitorName(),r.visitorEmail(),r.visitorKey()); realtime.publish("inbox", "conversation", result); return ResponseEntity.status(HttpStatus.CREATED).body(result); }
    @PostMapping("/conversations/{id}/messages")
    public SupportConversationResponse message(@PathVariable Long id, @Valid @RequestBody MessageRequest r) { return support.addVisitorMessage(id,r.visitorKey(),r.content()); }
    @GetMapping("/inbox")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','SUPPORT')")
    public List<SupportConversationResponse> inbox(HttpServletRequest request) { requireUser(request); return support.inbox(); }
    @GetMapping("/conversations")
    public SupportConversationResponse visitorConversation(@RequestParam String visitorKey) { return support.byVisitorKey(visitorKey); }
    @PostMapping("/conversations/{id}/replies")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','SUPPORT')")
    public SupportConversationResponse reply(@PathVariable Long id, @Valid @RequestBody ReplyRequest r, HttpServletRequest request) { SupportConversationResponse result=support.reply(id,requireUser(request),r.content()); realtime.publish("inbox", "conversation", result); return result; }
    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','SUPPORT')")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id, HttpServletRequest request) { requireUser(request); support.deleteConversation(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/ai")
    public MapResponse ai(@Valid @RequestBody AiRequest r) { return new MapResponse(ai.answer(r.prompt())); }
    public record MapResponse(String answer) {}
    private User requireUser(HttpServletRequest request) { User u=AuthContext.currentUser(request,users); if(u==null) throw new org.springframework.security.access.AccessDeniedException("Authentication required"); return u; }
}
