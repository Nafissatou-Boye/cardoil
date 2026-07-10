package cardoil.backend.controller;

import cardoil.backend.dto.request.PersonnelRequest;
import cardoil.backend.dto.response.PersonnelResponse;
import cardoil.backend.service.AdminPersonnelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/personnel")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminPersonnelController {

    private final AdminPersonnelService adminPersonnelService;

    @GetMapping
    public ResponseEntity<List<PersonnelResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminPersonnelService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<PersonnelResponse> create(Authentication authentication,
                                                      @Valid @RequestBody PersonnelRequest request) {
        return ResponseEntity.ok(adminPersonnelService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonnelResponse> update(Authentication authentication,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody PersonnelRequest request) {
        return ResponseEntity.ok(adminPersonnelService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminPersonnelService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin-entreprise/{entrepriseId}")
public ResponseEntity<PersonnelResponse> createAdminEntreprise(
        Authentication authentication,
        @PathVariable Long entrepriseId,
        @Valid @RequestBody PersonnelRequest request) {
    return ResponseEntity.ok(adminPersonnelService.createAdminEntreprise(
            authentication.getName(), entrepriseId, request));
}

@PutMapping("/admin-entreprise/{entrepriseId}")
public ResponseEntity<PersonnelResponse> remplacerAdminEntreprise(
        Authentication authentication,
        @PathVariable Long entrepriseId,
        @Valid @RequestBody PersonnelRequest request) {
    return ResponseEntity.ok(adminPersonnelService.remplacerAdminEntreprise(
            authentication.getName(), entrepriseId, request));
}
}