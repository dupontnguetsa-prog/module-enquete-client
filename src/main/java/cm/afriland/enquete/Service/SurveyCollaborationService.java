package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.*;
import cm.afriland.enquete.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class SurveyCollaborationService {
    private final SurveyService surveys; private final SurveyTemplateRepository templates; private final QuestionBankItemRepository bank;
    private final SurveyVersionRepository versions; private final CollaboratorCommentRepository comments;
    public SurveyCollaborationService(SurveyService s, SurveyTemplateRepository t, QuestionBankItemRepository b, SurveyVersionRepository v, CollaboratorCommentRepository c){surveys=s;templates=t;bank=b;versions=v;comments=c;}
    private User user(Long id){return surveys.requireUser(id);}
    @Transactional(readOnly=true) public List<SurveyTemplate> templates(Long uid){return templates.findAllByOwnerOrderByUpdatedAtDesc(user(uid));}
    @Transactional public SurveyTemplate saveTemplate(Long uid,Long id,TemplateRequest r){User u=user(uid); SurveyTemplate t=id==null?new SurveyTemplate():templates.findByIdAndOwner(id,u).orElseThrow(()->new NoSuchElementException("Modèle introuvable.")); t.setOwner(u); t.setName(required(r.name(),"Le nom du modèle est obligatoire."));t.setDescription(r.description());t.setContent(required(r.content(),"Le contenu du modèle est obligatoire."));return templates.save(t);}
    @Transactional public void deleteTemplate(Long uid,Long id){templates.delete(templates.findByIdAndOwner(id,user(uid)).orElseThrow(()->new NoSuchElementException("Modèle introuvable.")));}
    @Transactional(readOnly=true) public List<QuestionBankItem> bank(Long uid){return bank.findAllByOwnerOrderByCreatedAtDesc(user(uid));}
    @Transactional public QuestionBankItem saveBank(Long uid,Long id,BankRequest r){User u=user(uid);QuestionBankItem q=id==null?new QuestionBankItem():bank.findByIdAndOwner(id,u).orElseThrow(()->new NoSuchElementException("Question introuvable."));q.setOwner(u);q.setType(required(r.type(),"Le type est obligatoire."));q.setTitle(required(r.title(),"Le texte est obligatoire."));q.setDescription(r.description());q.setOptions(r.options());q.setRequired(r.required());return bank.save(q);}
    @Transactional public void deleteBank(Long uid,Long id){bank.delete(bank.findByIdAndOwner(id,user(uid)).orElseThrow(()->new NoSuchElementException("Question introuvable.")));}
    @Transactional(readOnly=true) public List<SurveyVersion> versions(Long uid,Long sid){return versions.findAllBySurveyOrderByVersionNumberDesc(surveys.requirePermission(uid,sid,"OWNER","ADMIN","EDITOR"));}
    @Transactional public SurveyVersion createVersion(Long uid,Long sid,VersionRequest r){Survey s=surveys.requirePermission(uid,sid,"OWNER","ADMIN","EDITOR");SurveyVersion v=new SurveyVersion();v.setSurvey(s);v.setCreatedBy(user(uid));v.setVersionNumber(versions.countBySurvey(s)+1);v.setSnapshot(required(r.snapshot(),"Le snapshot est obligatoire."));v.setChangeNote(r.changeNote());return versions.save(v);}
    @Transactional(readOnly=true) public List<CollaboratorComment> comments(Long uid,Long sid){return comments.findAllBySurveyOrderByCreatedAtAsc(surveys.requirePermission(uid,sid,"OWNER","ADMIN","EDITOR","ANALYST"));}
    @Transactional public CollaboratorComment addComment(Long uid,Long sid,CommentRequest r){Survey s=surveys.requirePermission(uid,sid,"OWNER","ADMIN","EDITOR","ANALYST");CollaboratorComment c=new CollaboratorComment();c.setSurvey(s);c.setAuthor(user(uid));c.setBody(required(r.body(),"Le commentaire est obligatoire."));c.setQuestionId(r.questionId());return comments.save(c);}
    @Transactional public CollaboratorComment resolve(Long uid,Long sid,Long cid,boolean resolved){Survey s=surveys.requirePermission(uid,sid,"OWNER","ADMIN","EDITOR");CollaboratorComment c=comments.findByIdAndSurvey(cid,s).orElseThrow(()->new NoSuchElementException("Commentaire introuvable."));c.setResolved(resolved);return comments.save(c);}
    @Transactional(readOnly=true) public PublicationValidation validate(Long uid,Long sid){Survey s=surveys.requirePermission(uid,sid,"OWNER","ADMIN","EDITOR");List<String> errors=new ArrayList<>();if(s.getTitle()==null||s.getTitle().isBlank())errors.add("Le titre est obligatoire.");if(s.getQuestions().isEmpty())errors.add("Ajoutez au moins une question.");for(SurveyQuestion q:s.getQuestions())if(q.getTitle()==null||q.getTitle().isBlank())errors.add("Chaque question doit avoir un texte.");if(s.getChannels()==null||s.getChannels().isBlank()||"[]".equals(s.getChannels()))errors.add("Choisissez au moins un canal de diffusion.");return new PublicationValidation(errors.isEmpty(),errors);}
    private String required(String v,String m){if(v==null||v.isBlank())throw new IllegalArgumentException(m);return v.trim();}
    public record TemplateRequest(String name,String description,String content){} public record BankRequest(String type,String title,String description,String options,boolean required){} public record VersionRequest(String snapshot,String changeNote){} public record CommentRequest(String body,Long questionId){} public record PublicationValidation(boolean valid,List<String> errors){}
}
