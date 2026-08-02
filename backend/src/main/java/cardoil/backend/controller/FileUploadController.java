package cardoil.backend.controller;

import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Endpoint autonome (pas lié à un serviceId) : permet de choisir l'image
// PENDANT la création d'un nouveau service, avant qu'il ait un id.
// L'admin reste résolu depuis Authentication (même garde qu'ailleurs), un
// dossier par compagnie pour ranger les fichiers séparément.
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class FileUploadController {

    private final UtilisateurRepository utilisateurRepository;

    private static final long TAILLE_MAX = 2L * 1024 * 1024; // 2 Mo
    private static final List<String> TYPES_AUTORISES =
            List.of("image/png", "image/jpeg", "image/webp", "image/svg+xml");

    @PostMapping("/icone")
    public ResponseEntity<Map<String, String>> uploaderIcone(
            Authentication authentication,
            @RequestParam("fichier") MultipartFile fichier) throws IOException {

        Compagnie compagnie = resolveCompagnie(authentication.getName());

        if (fichier.isEmpty()) {
            throw new IllegalStateException("Fichier vide");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new IllegalStateException("Fichier trop volumineux (2 Mo maximum)");
        }
        String contentType = fichier.getContentType();
        if (contentType == null || !TYPES_AUTORISES.contains(contentType)) {
            throw new IllegalStateException("Format non autorisé (PNG, JPEG, WEBP ou SVG uniquement)");
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> "";
        };

        // Nom généré, jamais le nom original — évite toute traversée de
        // chemin et toute collision entre deux admins qui uploaderaient un
        // fichier au même nom.
        String nomFichier = UUID.randomUUID() + extension;

        Path dossier = Paths.get("uploads", "services", compagnie.getId().toString());
        Files.createDirectories(dossier);
        Path destination = dossier.resolve(nomFichier);
        fichier.transferTo(destination);

        // Chemin relatif renvoyé, pas une URL absolue — le backend ne sait
        // pas s'il est appelé depuis localhost (navigateur) ou 10.0.2.2
        // (émulateur Android) ; chaque client préfixe avec sa propre base.
        String url = "/uploads/services/" + compagnie.getId() + "/" + nomFichier;
        return ResponseEntity.ok(Map.of("url", url));
    }

    private Compagnie resolveCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }
        return utilisateur.getCompagnie();
    }
}