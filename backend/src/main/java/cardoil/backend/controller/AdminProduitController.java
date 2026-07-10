package cardoil.backend.controller;

import cardoil.backend.dto.request.ProduitRequest;
import cardoil.backend.dto.response.ProduitResponse;
import cardoil.backend.service.AdminProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/produits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminProduitController {

    private final AdminProduitService adminProduitService;

    @GetMapping
    public ResponseEntity<List<ProduitResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminProduitService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ProduitResponse> create(Authentication authentication,
                                                    @Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(adminProduitService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduitResponse> update(Authentication authentication,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(adminProduitService.update(authentication.getName(), id, request));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ProduitResponse> changerStatut(Authentication authentication,
                                                           @PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminProduitService.changerStatut(authentication.getName(), id, body.get("statut")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminProduitService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}