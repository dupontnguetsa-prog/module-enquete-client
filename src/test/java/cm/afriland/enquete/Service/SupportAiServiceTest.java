package cm.afriland.enquete.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportAiServiceTest {
    private final SupportAiService service =
        new SupportAiService("http://localhost:1", "", "test-model");

    @Test
    void answersBlankPromptLocally() {
        assertEquals("Écrivez votre question pour que je puisse vous aider.",
            service.answer(" "));
    }

    @Test
    void answersKnownSurveyQuestionWithoutExternalProvider() {
        assertTrue(service.answer("Comment créer une enquête ?").contains("créer une enquête"));
    }
}
