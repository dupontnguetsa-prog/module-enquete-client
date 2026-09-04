package cm.afriland.enquete.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final Path directory;
    public MediaController(@Value("${app.upload-dir:uploads}") String directory) throws Exception {
        this.directory=Paths.get(directory).toAbsolutePath().normalize();Files.createDirectories(this.directory);
    }
    @PostMapping(value="/profile", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> profile(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        if(file.isEmpty()||file.getSize()>5_000_000||file.getContentType()==null||!file.getContentType().startsWith("image/")) return ResponseEntity.badRequest().body("Image invalide.");
        String extension=switch(file.getContentType()){case "image/jpeg"->".jpg";case "image/png"->".png";case "image/webp"->".webp";default->".img";};
        String name=UUID.randomUUID()+extension;Path target=directory.resolve(name).normalize();
        if(!target.getParent().equals(directory)) return ResponseEntity.badRequest().body("Nom de fichier invalide.");
        Files.copy(file.getInputStream(),target,StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/api/media/"+name);
    }
    @GetMapping("/{name}")
    public ResponseEntity<FileSystemResource> get(@PathVariable String name){
        if(!name.matches("[a-zA-Z0-9-]+\\.(jpg|png|webp|img)")) return ResponseEntity.notFound().build();
        Path file=directory.resolve(name).normalize();if(!file.startsWith(directory)||!Files.exists(file))return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(contentType(name)).body(new FileSystemResource(file));
    }
    private MediaType contentType(String name){if(name.endsWith(".png"))return MediaType.IMAGE_PNG;if(name.endsWith(".webp"))return MediaType.parseMediaType("image/webp");return MediaType.IMAGE_JPEG;}
}
