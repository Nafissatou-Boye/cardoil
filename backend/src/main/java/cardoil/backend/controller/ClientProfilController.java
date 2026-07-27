package cardoil.backend.controller;

import cardoil.backend.dto.request.ChangerCompagnieRequest;
import cardoil.backend.dto.response.ClientProfilResponse;
import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.dto.response.QrCodeResponse;
import cardoil.backend.service.ClientProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientProfilController {

    private final ClientProfilService clientProfilService;

    @GetMapping("/moi")
    public ResponseEntity<ClientProfilResponse> getMonProfil(Authentication authentication) {
        return ResponseEntity.ok(clientProfilService.getMonProfil(authentication.getName()));
    }

    @PostMapping("/qr-code")
    public ResponseEntity<QrCodeResponse> genererQrCode(Authentication authentication) {
        return ResponseEntity.ok(clientProfilService.genererQrCode(authentication.getName()));
    }

    @GetMapping("/profil/compagnie")
    public ResponseEntity<CompagnieOptionResponse> getMaCompagnie(Authentication authentication) {
        return ResponseEntity.ok(clientProfilService.getMaCompagnie(authentication.getName()));
    }

    @PatchMapping("/profil/compagnie")
    public ResponseEntity<Void> changerCompagnie(Authentication authentication,
                                                   @Valid @RequestBody ChangerCompagnieRequest request) {
        clientProfilService.changerCompagnie(authentication.getName(), request.getCompagnieId());
        return ResponseEntity.ok().build();
    }
}