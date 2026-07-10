package cardoil.backend.controller;

import cardoil.backend.dto.request.EntrepriseRequest;
import cardoil.backend.dto.response.AdminEntrepriseInfoResponse;
import cardoil.backend.dto.response.EntrepriseResponse;
import cardoil.backend.service.AdminEntrepriseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/entreprises")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminEntrepriseController {

    private final AdminEntrepriseService adminEntrepriseService;

    @GetMapping
    public ResponseEntity<List<EntrepriseResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminEntrepriseService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<EntrepriseResponse> create(Authentication authentication,
                                                      @Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(adminEntrepriseService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntrepriseResponse> update(Authentication authentication,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.ok(adminEntrepriseService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminEntrepriseService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<EntrepriseResponse> toggleActif(Authentication authentication,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(adminEntrepriseService.toggleActif(authentication.getName(), id));
    }

    @GetMapping("/{id}/admin")
public ResponseEntity<AdminEntrepriseInfoResponse> getAdmin(
        Authentication authentication,
        @PathVariable Long id) {
    return ResponseEntity.ok(adminEntrepriseService.getAdmin(authentication.getName(), id));
}
}