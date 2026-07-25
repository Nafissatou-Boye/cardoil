package cardoil.backend.controller;

import cardoil.backend.dto.request.ChangerCompagnieRequest;
import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.service.ClientProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/profil")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientProfilController {

    private final ClientProfilService clientProfilService;

    @GetMapping("/compagnie")
    public ResponseEntity<CompagnieOptionResponse> getMaCompagnie(Authentication authentication) {
        return ResponseEntity.ok(clientProfilService.getMaCompagnie(authentication.getName()));
    }

    @PatchMapping("/compagnie")
    public ResponseEntity<Void> changerCompagnie(Authentication authentication,
                                                   @Valid @RequestBody ChangerCompagnieRequest request) {
        clientProfilService.changerCompagnie(authentication.getName(), request.getCompagnieId());
        return ResponseEntity.ok().build();
    }
}