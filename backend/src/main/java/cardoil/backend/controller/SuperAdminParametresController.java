package cardoil.backend.controller;

import cardoil.backend.dto.request.SuperAdminProfilRequest;
import cardoil.backend.dto.response.SuperAdminProfilResponse;
import cardoil.backend.service.SuperAdminParametresService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin/parametres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminParametresController {

    private final SuperAdminParametresService superAdminParametresService;

    @GetMapping("/profil")
    public ResponseEntity<SuperAdminProfilResponse> getProfil(Authentication authentication) {
        return ResponseEntity.ok(superAdminParametresService.getProfil(authentication.getName()));
    }

    @PutMapping("/profil")
    public ResponseEntity<SuperAdminProfilResponse> updateProfil(
            Authentication authentication,
            @Valid @RequestBody SuperAdminProfilRequest request) {
        return ResponseEntity.ok(superAdminParametresService.updateProfil(
                authentication.getName(), request));
    }
}