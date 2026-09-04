package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.UserService;
import cm.afriland.enquete.Service.PasswordResetService;
import cm.afriland.enquete.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    private final PasswordEncoder encoder;
    private final SecurityContextRepository securityContextRepository;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService users, PasswordEncoder encoder, SecurityContextRepository repo, PasswordResetService passwordResetService) {
        this.users = users;
        this.encoder = encoder;
        this.securityContextRepository = repo;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        User user = resolve(request);
        return user == null
            ? ResponseEntity.status(401).body("Aucun utilisateur connecté.")
            : ResponseEntity.ok(view(user));
    }

    @PostMapping("/identifier")
    public ResponseEntity<?> identifier(@RequestBody IdentifierRequest body) {
        if (body == null || body.identifiant() == null || body.identifiant().isBlank()) {
            return ResponseEntity.badRequest().body("Identifiant obligatoire.");
        }
        return users.trouverParIdentifiant(body.identifiant().trim())
            .<ResponseEntity<?>>map(user -> ResponseEntity.ok(view(user)))
            .orElseGet(() -> ResponseEntity.status(404).body("Identifiant incorrect."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody LoginRequest body,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (body == null || body.identifiant() == null || body.password() == null) {
            return ResponseEntity.badRequest().body("Identifiant et mot de passe obligatoires.");
        }
        Optional<User> found = users.trouverParIdentifiant(body.identifiant().trim());
        if (found.isEmpty() || !encoder.matches(body.password(), found.get().getPassword())) {
            return ResponseEntity.status(401).body("Identifiant ou mot de passe incorrect.");
        }
        authenticate(found.get(), request, response);
        return ResponseEntity.ok(view(found.get()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
        @RequestBody RegisterRequest body,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (body == null) return ResponseEntity.badRequest().body("Donnees invalides.");

        String nom = blank(body.nom());
        String email = blank(body.email());
        String identifiant = blank(body.identifiant());
        String password = blank(body.password());

        if (nom == null || identifiant == null || password == null) {
            return ResponseEntity.badRequest().body("Nom, identifiant et mot de passe sont obligatoires.");
        }
        if (email != null) {
            email = email.toLowerCase();
            if (!email.contains("@")) return ResponseEntity.badRequest().body("Adresse e-mail invalide.");
        }
        if (identifiant.length() < 3) {
            return ResponseEntity.badRequest().body("L identifiant doit contenir au moins 3 caracteres.");
        }
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 8 caracteres.");
        }
        if (users.identifiantExiste(identifiant)) {
            return ResponseEntity.status(409).body("Cet identifiant est deja utilise.");
        }
        if (email != null && users.emailExiste(email)) {
            return ResponseEntity.status(409).body("Cette adresse e-mail est deja utilisee.");
        }

        User user = new User();
        user.setNom(nom);
        user.setEmail(email);
        user.setIdentifiant(identifiant);
        user.setPassword(password);
        user.setRole("OWNER");
        user = users.enregistrer(user);
        authenticate(user, request, response);
        return ResponseEntity.ok(view(user));
    }

    @PutMapping("/photo")
    public ResponseEntity<?> photo(@RequestBody PhotoRequest body, HttpServletRequest request) {
        User user = resolve(request);
        if (user == null) return ResponseEntity.status(401).body("Non authentifié.");
        if (body == null || body.photoUrl() == null || body.photoUrl().isBlank()) {
            return ResponseEntity.badRequest().body("Photo invalide.");
        }
        user.setPhotoUrl(body.photoUrl());
        users.enregistrerSansReencoder(user);
        return ResponseEntity.ok(view(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest body) {
        String email = body == null ? null : blank(body.email());
        if (email != null && email.contains("@")) {
            passwordResetService.requestReset(email, frontendUrl);
        }
        return ResponseEntity.ok(new MessageResponse("Si cette adresse correspond à un compte, un lien de réinitialisation a été envoyé."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest body) {
        String token = body == null ? null : blank(body.token());
        String password = body == null ? null : blank(body.password());
        if (token == null || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body("Le jeton et un mot de passe d’au moins 8 caractères sont obligatoires.");
        }
        if (!passwordResetService.resetPassword(token, password)) {
            return ResponseEntity.badRequest().body("Ce lien est invalide, expiré ou a déjà été utilisé.");
        }
        return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé. Vous pouvez maintenant vous connecter."));
    }

    private void authenticate(User user, HttpServletRequest request, HttpServletResponse response) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            try {
                old.invalidate();
            } catch (IllegalStateException ignored) {
                // The request may already have invalidated the session.
            }
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("authenticatedUserId", user.getId());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String role = "USER".equalsIgnoreCase(user.getRole()) ? "OWNER" : user.getRole();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            user.getIdentifiant(),
            null,
            AuthorityUtils.createAuthorityList("ROLE_" + role)
        ));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    private User resolve(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object raw = session.getAttribute("authenticatedUserId");
            if (raw != null) {
                try {
                    return users.trouverParId(Long.valueOf(String.valueOf(raw))).orElse(null);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getName())) {
            String principal = authentication.getName();
            return users.trouverParIdentifiant(principal)
                .orElseGet(() -> users.trouverParEmail(principal.toLowerCase()).orElse(null));
        }
        return null;
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UserView view(User user) {
        return new UserView(
            user.getId(),
            user.getNom(),
            user.getEmail(),
            user.getIdentifiant(),
            user.getPhotoUrl(),
            user.getRole()
        );
    }

    public record IdentifierRequest(String identifiant) {}
    public record LoginRequest(String identifiant, String password) {}
    public record RegisterRequest(String nom, String email, String identifiant, String password) {}
    public record PhotoRequest(String photoUrl) {}
    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(String token, String password) {}
    public record MessageResponse(String message) {}

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    public record UserView(Long id, String nom, String email, String identifiant, String photoUrl, String role) {}
}
