package cm.afriland.enquete.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeService {
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> clients = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String channel) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        clients.computeIfAbsent(channel, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> clients.getOrDefault(channel, new CopyOnWriteArrayList<>()).remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("channel", channel)));
        } catch (IOException exception) {
            cleanup.run();
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    public void publish(String channel, String event, Object data) {
        for (SseEmitter emitter : clients.getOrDefault(channel, new CopyOnWriteArrayList<>())) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (IOException exception) {
                emitter.completeWithError(exception);
            }
        }
    }
}
