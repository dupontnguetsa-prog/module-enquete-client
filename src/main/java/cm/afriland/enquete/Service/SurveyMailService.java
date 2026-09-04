package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.Survey;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.Arrays;

@Service
public class SurveyMailService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final ObjectProvider<JavaMailSender> mailSenders;
    public SurveyMailService(ObjectProvider<JavaMailSender> mailSenders) { this.mailSenders = mailSenders; }

    public void send(Survey survey, String recipients, String baseUrl) {
        if (recipients == null || recipients.isBlank()) throw new IllegalArgumentException("Adresse email obligatoire.");
        String[] addresses = Arrays.stream(recipients.split("[,;\\s]+")).map(String::trim).filter(v -> !v.isBlank()).toArray(String[]::new);
        if (addresses.length == 0 || Arrays.stream(addresses).anyMatch(v -> !EMAIL.matcher(v).matches())) throw new IllegalArgumentException("Une adresse email est invalide.");
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) throw new IllegalStateException("L’envoi email n’est pas configuré. Renseignez les paramètres Gmail SMTP.");
        String link = baseUrl + "/survey/" + survey.getPublicKey();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(addresses);
        message.setSubject("Votre avis : " + survey.getTitle());
        message.setText("Bonjour,\n\nNous vous invitons à répondre à cette enquête :\n" + link + "\n\nMerci.");
        sender.send(message);
    }

    public void sendPasswordReset(String email, String link) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) throw new IllegalStateException("L’envoi email n’est pas configuré. Renseignez MAIL_USERNAME et MAIL_PASSWORD.");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour,\n\nUtilisez ce lien pour choisir un nouveau mot de passe (valable 30 minutes et utilisable une seule fois) :\n"
            + link + "\n\nSi vous n’êtes pas à l’origine de cette demande, ignorez cet e-mail.");
        sender.send(message);
    }
}
