package cardoil.backend.controller;

import cardoil.backend.dto.request.ChangerMotDePasseRequest;
import cardoil.backend.service.MotDePasseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MotDePasseController {

    private final MotDePasseService motDePasseService;

    // Rôle-agnostique volontairement : Client (4 chiffres), Gérant/Pompiste
    // (6 chiffres) et Employé en ont tous besoin, chacun avec sa propre
    // contrainte de format validée côté service selon le rôle.
    @PatchMapping("/api/compte/mot-de-passe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changerMotDePasse(
            Authentication authentication,
            @Valid @RequestBody ChangerMotDePasseRequest request) {
        motDePasseService.changerMotDePasse(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}