package cm.afriland.enquete.dto;

import java.time.Instant;

public record SupportMessageResponse(
        Long id,
        String content,
        String senderType,
        String agentName,
        Instant createdAt
) {}
