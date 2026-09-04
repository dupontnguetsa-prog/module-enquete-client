package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.UserService;
import cm.afriland.enquete.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthContext {
    private AuthContext() {}
    public static User currentUser(HttpServletRequest request, UserService users) {
        HttpSession session=request.getSession(false);
        if(session!=null){Object raw=session.getAttribute("authenticatedUserId"); if(raw!=null)try{var user=users.trouverParId(Long.parseLong(String.valueOf(raw)));if(user.isPresent())return user.get();}catch(NumberFormatException ignored){}}
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null&&auth.isAuthenticated()&&!"anonymousUser".equals(auth.getName())){
            String p=auth.getName(); return users.trouverParIdentifiant(p).orElseGet(()->users.trouverParEmail(p.toLowerCase()).orElse(null));
        }
        return null;
    }
}
