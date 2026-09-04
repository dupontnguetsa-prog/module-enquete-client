package cm.afriland.enquete.repository;
import cm.afriland.enquete.model.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface QuestionBankItemRepository extends JpaRepository<QuestionBankItem,Long>{ List<QuestionBankItem> findAllByOwnerOrderByCreatedAtDesc(User owner); Optional<QuestionBankItem> findByIdAndOwner(Long id,User owner); }
