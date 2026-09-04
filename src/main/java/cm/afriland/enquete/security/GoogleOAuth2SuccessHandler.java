package cm.afriland.enquete.security;

import cm.afriland.enquete.model.User;
import cm.afriland.enquete.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.UUID;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository users; private final PasswordEncoder encoder;
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    public GoogleOAuth2SuccessHandler(UserRepository users, PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
    @Override public void onAuthenticationSuccess(HttpServletRequest request,HttpServletResponse response,Authentication authentication) throws IOException, ServletException {
        OAuth2User google=(OAuth2User)authentication.getPrincipal();
        String email=read(google,"email"); String name=read(google,"name"); String picture=read(google,"picture");
        if(email==null||email.isBlank()){response.sendRedirect(frontendUrl+"/identification?error=google_email_missing");return;}
        email=email.trim().toLowerCase(); if(name==null||name.isBlank())name=email.substring(0,email.indexOf('@'));
        User user=users.findByEmail(email).orElseGet(User::new); user.setEmail(email); user.setNom(name.trim());
        if(user.getIdentifiant()==null||user.getIdentifiant().isBlank())user.setIdentifiant(uniqueIdentifier(email));
        if(user.getPassword()==null||user.getPassword().isBlank())user.setPassword(encoder.encode(UUID.randomUUID().toString()));
        if(user.getRole()==null||user.getRole().isBlank()||"USER".equalsIgnoreCase(user.getRole()))user.setRole("OWNER");
        if(picture!=null&&!picture.isBlank())user.setPhotoUrl(picture.trim());
        users.save(user);
        request.getSession(true).setAttribute("authenticatedUserId",user.getId());
        SecurityContext context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            user.getIdentifiant(), null,
            AuthorityUtils.createAuthorityList("ROLE_"+user.getRole())));
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context,request,response);
        response.sendRedirect(frontendUrl+"/bureau");
    }
    private String read(OAuth2User u,String k){Object v=u.getAttributes().get(k);return v==null?null:String.valueOf(v);}
    private String uniqueIdentifier(String email){String base=email.substring(0,email.indexOf('@')).replaceAll("[^a-zA-Z0-9._-]","");if(base.isBlank())base="googleuser";String c=base;int n=1;while(users.existsByIdentifiant(c))c=base+(n++);return c;}
}
