package cm.afriland.enquete.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class SupportAiService {
    private final RestClient client;
    private final String apiKey, model;
    public SupportAiService(
            @Value("${support.ai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${support.ai.api-key:}") String apiKey,
            @Value("${support.ai.model:gpt-4o-mini}") String model) {
        this.client = RestClient.create(baseUrl); this.apiKey = apiKey; this.model = model;
    }
    public String answer(String prompt) {
        String localAnswer = localAnswer(prompt);
        if (localAnswer != null) return localAnswer;
        if (apiKey == null || apiKey.isBlank()) {
            return "Je n’ai pas encore la réponse à cette question. Essayez : créer une enquête, brouillon, réponses, analytics ou support.";
        }
        Map<?, ?> response = client.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of("model", model, "messages", List.of(Map.of("role", "user", "content", prompt))))
                .retrieve().body(Map.class);
        try {
            Map<?, ?> choice = (Map<?, ?>) ((List<?>) response.get("choices")).get(0);
            return String.valueOf(((Map<?, ?>) choice.get("message")).get("content"));
        } catch (RuntimeException e) { throw new IllegalStateException("AI provider returned an invalid response"); }
    }
    private String localAnswer(String prompt) {
            if (prompt == null || prompt.isBlank()) return "Écrivez votre question pour que je puisse vous aider.";
            String question = java.text.Normalizer.normalize(prompt.toLowerCase(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            if (containsGreeting(question)) return "Bonjour ! Je peux vous aider avec les enquêtes, les réponses, les analytics et le support.";
            for (Faq faq : FAQS) if (contains(question, faq.terms)) return faq.answer;
            return null;
    }
    private boolean contains(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
    private boolean containsGreeting(String question) {
        String normalized = question.trim().replaceAll("[!?.,;:]+$", "").trim();
        return contains(normalized, "bonjour", "salut", "hello", "bonsoir", "coucou", "bjr")
                && normalized.length() <= 40;
    }
    private record Faq(String[] terms, String answer) {}
    private static final List<Faq> FAQS = createFaqs();
    private static List<Faq> createFaqs() {
        List<Faq> faqs = new ArrayList<>();
        add(faqs, "creer une enquete", "Pour créer une enquête, ouvrez « Enquêtes », cliquez sur « Créer une enquête », ajoutez vos questions puis publiez-la.", "creer", "nouvelle enquete", "faire une enquete", "ajouter une enquete", "commencer une enquete", "creer un sondage", "nouveau sondage", "construire une enquete", "lancer une enquete", "questionnaire");
        add(faqs, "modifier une enquete", "Ouvrez l’enquête dans « Enquêtes », modifiez les questions ou réglages, puis enregistrez le brouillon.", "modifier enquete", "changer question", "editer enquete", "corriger question", "modifier sondage", "mettre a jour enquete", "changer titre", "modifier contenu", "editer questionnaire", "revoir enquete");
        add(faqs, "brouillon", "Une enquête en cours est enregistrée automatiquement comme brouillon pendant sa conception.", "brouillon", "sauvegarde automatique", "enregistrer brouillon", "quitter enquete", "reprendre enquete", "enquete non publiee", "sauver travail", "perdre travail", "continuer plus tard", "sauvegarder");
        add(faqs, "publier", "Dans le builder, vérifiez l’aperçu puis cliquez sur « Publier l’enquête ». Le lien public sera disponible.", "publier", "publication", "mettre en ligne", "deployer enquete", "activer enquete", "lien public", "rendre active", "enquete active", "partager enquete", "diffuser");
        add(faqs, "supprimer", "Dans « Enquêtes », utilisez l’action de suppression sur l’enquête concernée, puis confirmez l’opération.", "supprimer", "effacer", "retirer enquete", "enlever sondage", "delete enquete", "annuler enquete", "detruire enquete", "supprimer sondage", "enlever questionnaire", "corbeille");
        add(faqs, "questions", "Vous pouvez utiliser NPS, échelle, étoiles, choix unique, choix multiple, oui/non, texte court et texte long.", "type question", "types questions", "ajouter question", "question nps", "question etoile", "question texte", "choix multiple", "oui non", "echelle", "formats question");
        add(faqs, "logique", "La logique permet de diriger le répondant vers une question différente selon sa réponse.", "logique", "branchement", "condition", "question suivante", "parcours", "saut question", "regle logique", "afficher question", "si reponse", "chemin questionnaire");
        add(faqs, "reponse", "La page « Réponses » affiche les retours reçus et permet de télécharger un export CSV.", "reponse", "repondant", "voir reponses", "liste reponses", "export csv", "telecharger reponses", "retour client", "feedback recu", "resultats reponses", "consulter reponses");
        add(faqs, "analytics", "La page « Analytics » analyse chaque type de question et affiche les réponses, la complétion, les abandons et les filtres d’audience.", "analytics", "analyse", "resultat", "statistique", "taux completion", "abandon", "nps score", "moyenne question", "graphique", "indicateur");
        add(faqs, "audience", "Les audiences peuvent être filtrées par type de client, agence, ville, statut relationnel ou produit.", "audience", "ciblage", "cibler client", "filtre client", "segment", "agence", "ville client", "type client", "produit client", "statut relation");
        add(faqs, "anonyme", "Une enquête peut accepter des réponses anonymes. Les réponses identifiées peuvent être liées par e-mail ou identifiant client.", "anonyme", "anonymat", "sans nom", "reponse anonyme", "collecter email", "identifier client", "customer id", "email client", "confidentialite", "donnees client");
        add(faqs, "declenchement", "Une enquête peut être déclenchée par lien, visite de page, temps passé, événement API, audience, date ou manuellement.", "declenchement", "trigger", "visite page", "temps page", "evenement api", "entree audience", "date lancement", "envoi manuel", "quand lancer", "automatiser envoi");
        add(faqs, "support", "Utilisez l’onglet « Contacter l’équipe » pour envoyer une demande. Elle apparaîtra dans votre boîte de réception support.", "support", "aide", "contacter", "equipe", "assistance", "probleme", "demande support", "boite reception", "parler equipe", "service client");
        add(faqs, "csv", "L’export CSV se trouve dans la page « Réponses ». Il contient le répondant, la date et les réponses aux questions.", "csv", "exporter", "excel", "fichier reponses", "telecharger csv", "export donnees", "export excel", "telecharger resultats", "format csv", "sortir reponses");
        add(faqs, "securite", "Les enquêtes et réponses privées sont liées à votre compte authentifié. Ne partagez jamais vos identifiants.", "securite", "connexion", "mot de passe", "compte", "session", "acces prive", "donnees securisees", "confidentialite compte", "authentification", "identifiant");
        return faqs;
    }
    private static void add(List<Faq> faqs, String ignored, String answer, String... terms) { faqs.add(new Faq(terms, answer)); }
    public static class AiNotConfiguredException extends RuntimeException { public AiNotConfiguredException(String m) { super(m); } }
}
