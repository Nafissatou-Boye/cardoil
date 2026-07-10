package cardoil.backend.controller;

import cardoil.backend.dto.request.AdminCompagnieRequest;
import cardoil.backend.dto.response.AdminCompagnieResponse;
import cardoil.backend.service.SuperAdminUtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminUtilisateurController {

    private final SuperAdminUtilisateurService superAdminUtilisateurService;

    @GetMapping
    public ResponseEntity<List<AdminCompagnieResponse>> getAll() {
        return ResponseEntity.ok(superAdminUtilisateurService.getAll());
    }

    @PostMapping
    public ResponseEntity<AdminCompagnieResponse> create(
            @Valid @RequestBody AdminCompagnieRequest request) {
        return ResponseEntity.ok(superAdminUtilisateurService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminCompagnieResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminCompagnieRequest request) {
        return ResponseEntity.ok(superAdminUtilisateurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        superAdminUtilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<AdminCompagnieResponse> toggleActif(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminUtilisateurService.toggleActif(id));
    }
}