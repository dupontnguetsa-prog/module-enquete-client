FIX DU CYCLE DE DÉPENDANCES SPRING SECURITY

ERREUR :
securityConfig <-> googleOAuth2SuccessHandler

CAUSE :
SecurityConfig créait le bean PasswordEncoder.
GoogleOAuth2SuccessHandler dépendait de PasswordEncoder.
SecurityConfig dépendait aussi de GoogleOAuth2SuccessHandler.
Cela formait un cycle.

CORRECTION :
PasswordEncoder est maintenant dans PasswordConfig.java.

À FAIRE :
1. Remplacer SecurityConfig.java par celui de ce dossier.
2. Créer PasswordConfig.java directement dans :
   cm.afriland.enquete
3. NE PAS garder un autre bean PasswordEncoder dans SecurityConfig.java.
4. Relancer Spring Boot.

Structure :
cm.afriland.enquete
├── SecurityConfig.java
├── PasswordConfig.java
├── controller
├── model
├── repository
└── security
    └── GoogleOAuth2SuccessHandler.java
