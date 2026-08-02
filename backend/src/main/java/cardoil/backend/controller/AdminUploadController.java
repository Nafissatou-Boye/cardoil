package cardoil.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminUploadController {

    @PostMapping(value = "/icone", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploaderIcone(@RequestParam("fichier") MultipartFile fichier) {
        if (fichier.isEmpty()) {
            throw new IllegalStateException("Aucun fichier fourni");
        }

        String contentType = fichier.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalStateException("Le fichier doit être une image");
        }

        String nomOriginal = fichier.getOriginalFilename();
        String extension = "";
        if (nomOriginal != null && nomOriginal.contains(".")) {
            extension = nomOriginal.substring(nomOriginal.lastIndexOf('.'));
        }
        String nomFichier = "icone-" + System.currentTimeMillis() + "-"
                + (int) (Math.random() * 10000) + extension;

        try {
            Path dossier = Paths.get("uploads/services");
            Files.createDirectories(dossier);
            fichier.transferTo(dossier.resolve(nomFichier));
        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de l'enregistrement de l'image : " + e.getMessage());
        }

        // Chemin relatif — le frontend préfixe avec l'origine du backend
        // (ServicesComponent.iconeUrl()).
        return ResponseEntity.ok(Map.of("url", "/uploads/services/" + nomFichier));
    }
}