package cm.afriland.enquete.dto;

import java.time.Instant;
import java.util.List;

public record SupportConversationResponse(
        Long id,
        String subject,
        String visitorName,
        String visitorEmail,
        String status,
        Instant createdAt,
        Instant updatedAt,
        List<SupportMessageResponse> messages
) {}
