package cardoil.backend.controller;

import cardoil.backend.dto.request.CompagnieProfilRequest;
import cardoil.backend.dto.response.CompagnieProfilResponse;
import cardoil.backend.service.AdminParametresService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/parametres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminParametresController {

    private final AdminParametresService adminParametresService;

    @GetMapping("/compagnie")
    public ResponseEntity<CompagnieProfilResponse> getProfil(Authentication authentication) {
        return ResponseEntity.ok(adminParametresService.getProfil(authentication.getName()));
    }

    @PutMapping("/compagnie")
    public ResponseEntity<CompagnieProfilResponse> updateProfil(
            Authentication authentication,
            @Valid @RequestBody CompagnieProfilRequest request) {
        return ResponseEntity.ok(adminParametresService.updateProfil(authentication.getName(), request));
    }
}