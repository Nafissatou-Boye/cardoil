package cardoil.backend.controller;

import cardoil.backend.dto.request.AdminDepartementRequest;
import cardoil.backend.dto.request.CrediterDepartementRequest;
import cardoil.backend.dto.request.DepartementRequest;
import cardoil.backend.dto.response.AdminDepartementInfoResponse;
import cardoil.backend.dto.response.DepartementResponse;
import cardoil.backend.dto.response.EntrepriseInfoResponse;
import cardoil.backend.service.AdminDepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ENTREPRISE')")
public class AdminDepartementController {

    private final AdminDepartementService adminDepartementService;

    @GetMapping
    public ResponseEntity<List<DepartementResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminDepartementService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<DepartementResponse> create(Authentication authentication,
                                                        @Valid @RequestBody DepartementRequest request) {
        return ResponseEntity.ok(adminDepartementService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartementResponse> update(Authentication authentication,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody DepartementRequest request) {
        return ResponseEntity.ok(adminDepartementService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminDepartementService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<DepartementResponse> toggleActif(Authentication authentication,
                                                             @PathVariable Long id) {
        return ResponseEntity.ok(adminDepartementService.toggleActif(authentication.getName(), id));
    }

    @PostMapping("/{id}/crediter")
    public ResponseEntity<DepartementResponse> crediterBudget(Authentication authentication,
                                                                 @PathVariable Long id,
                                                                 @Valid @RequestBody CrediterDepartementRequest request) {
        return ResponseEntity.ok(adminDepartementService.crediterBudget(authentication.getName(), id, request.getMontant()));
    }

    @GetMapping("/{id}/admin")
    public ResponseEntity<AdminDepartementInfoResponse> getAdmin(Authentication authentication,
                                                                   @PathVariable Long id) {
        return ResponseEntity.ok(adminDepartementService.getAdmin(authentication.getName(), id));
    }

    @PostMapping("/{id}/admin")
    public ResponseEntity<AdminDepartementInfoResponse> createAdmin(Authentication authentication,
                                                                       @PathVariable Long id,
                                                                       @Valid @RequestBody AdminDepartementRequest request) {
        return ResponseEntity.ok(adminDepartementService.createAdmin(authentication.getName(), id, request));
    }

    @PutMapping("/{id}/admin")
    public ResponseEntity<AdminDepartementInfoResponse> remplacerAdmin(Authentication authentication,
                                                                          @PathVariable Long id,
                                                                          @Valid @RequestBody AdminDepartementRequest request) {
        return ResponseEntity.ok(adminDepartementService.remplacerAdmin(authentication.getName(), id, request));
    }

    @GetMapping("/entreprise-info")
    public ResponseEntity<EntrepriseInfoResponse> getInfoEntreprise(Authentication authentication) {
        return ResponseEntity.ok(adminDepartementService.getInfoEntreprise(authentication.getName()));
    }
}