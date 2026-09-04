package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.User;
import cm.afriland.enquete.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository; this.passwordEncoder = passwordEncoder;
    }
    public Optional<User> trouverParId(Long id){ return userRepository.findById(id); }
    public Optional<User> trouverParIdentifiant(String id){ return userRepository.findByIdentifiant(id); }
    public Optional<User> trouverParEmail(String email){ return userRepository.findByEmail(email); }
    public boolean identifiantExiste(String id){ return userRepository.existsByIdentifiant(id); }
    public boolean emailExiste(String email){ return userRepository.existsByEmail(email); }
    public User enregistrer(User user){ user.setPassword(passwordEncoder.encode(user.getPassword())); return userRepository.save(user); }
    public User enregistrerSansReencoder(User user){ return userRepository.save(user); }
}
