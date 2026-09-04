package cm.afriland.enquete.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()==null?"Requête invalide.":e.getMessage()));}
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> state(IllegalStateException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",e.getMessage()==null?"Opération impossible.":e.getMessage()));}
    @ExceptionHandler(java.util.NoSuchElementException.class)
    ResponseEntity<?> notFound(java.util.NoSuchElementException e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message",e.getMessage()==null?"Ressource introuvable.":e.getMessage()));}
    @ExceptionHandler(cm.afriland.enquete.Service.SupportAiService.AiNotConfiguredException.class)
    ResponseEntity<?> aiNotConfigured(RuntimeException e){return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message",e.getMessage()));}
}
