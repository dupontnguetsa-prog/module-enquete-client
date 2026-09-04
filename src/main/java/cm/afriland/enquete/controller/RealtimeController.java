package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.RealtimeService;
import cm.afriland.enquete.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {
    private final RealtimeService realtime;
    private final UserService users;

    public RealtimeController(RealtimeService realtime, UserService users) {
        this.realtime = realtime;
        this.users = users;
    }

    @GetMapping(value = "/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String channel, HttpServletRequest request) {
        if (!channel.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("Canal invalide.");
        if (AuthContext.currentUser(request, users) == null) throw new UnauthorizedException();
        return realtime.subscribe(channel);
    }

    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    static class UnauthorizedException extends RuntimeException {}
}
