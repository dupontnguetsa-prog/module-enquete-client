package cm.afriland.enquete.Service;
import cm.afriland.enquete.model.*;
import cm.afriland.enquete.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

@Service
public class ApiKeyService {
    private final ApiKeyRepository keys;
    public ApiKeyService(ApiKeyRepository keys){this.keys=keys;}
    public Created create(User owner,String name){byte[] raw=new byte[32];new SecureRandom().nextBytes(raw);String value="ak_"+Base64.getUrlEncoder().withoutPadding().encodeToString(raw);ApiKey key=new ApiKey();key.setOwner(owner);key.setName(name==null||name.isBlank()?"Clé API":name.trim());key.setKeyHash(hash(value));keys.save(key);return new Created(key.getId(),key.getName(),value,key.getCreatedAt());}
    public Optional<User> authenticate(String raw){if(raw==null||raw.isBlank())return Optional.empty();return keys.findByKeyHashAndRevokedAtIsNull(hash(raw.trim())).map(ApiKey::getOwner);}
    public List<ApiKeyView> list(User owner){return keys.findAllByOwnerOrderByCreatedAtDesc(owner).stream().map(k->new ApiKeyView(k.getId(),k.getName(),k.getCreatedAt(),k.getRevokedAt()!=null)).toList();}
    public void revoke(User owner,Long id){ApiKey key=keys.findById(id).orElseThrow();if(!key.getOwner().getId().equals(owner.getId()))throw new SecurityException("Permission insuffisante.");key.setRevokedAt(java.time.LocalDateTime.now());keys.save(key);}
    private String hash(String v){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    public record Created(Long id,String name,String key,java.time.LocalDateTime createdAt){}
    public record ApiKeyView(Long id,String name,java.time.LocalDateTime createdAt,boolean revoked){}
}
