package cardoil.backend.controller;

import cardoil.backend.dto.response.CompteEmployeResponse;
import cardoil.backend.dto.response.QrCodeResponse;
import cardoil.backend.service.EmployeCompteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmployeCompteController {

    private final EmployeCompteService employeCompteService;

    @GetMapping("/api/employe/mon-compte")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<CompteEmployeResponse> monCompte(Authentication authentication) {
        return ResponseEntity.ok(employeCompteService.getMonCompte(authentication.getName()));
    }

    // Symétrique à ClientProfilController.genererQrCode() (POST /api/client/qr-code).
    @PostMapping("/api/employe/qr-code")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<QrCodeResponse> genererQrCode(Authentication authentication) {
        return ResponseEntity.ok(employeCompteService.genererQrCode(authentication.getName()));
    }
}