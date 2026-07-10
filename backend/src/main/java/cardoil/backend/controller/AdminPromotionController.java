package cardoil.backend.controller;

import cardoil.backend.dto.request.PromotionRequest;
import cardoil.backend.dto.response.PromotionResponse;
import cardoil.backend.service.AdminPromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminPromotionController {

    private final AdminPromotionService adminPromotionService;

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminPromotionService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(Authentication authentication,
                                                      @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(adminPromotionService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(Authentication authentication,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(adminPromotionService.update(authentication.getName(), id, request));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<PromotionResponse> changerStatut(Authentication authentication,
                                                             @PathVariable Long id,
                                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminPromotionService.changerStatut(authentication.getName(), id, body.get("statut")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminPromotionService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}