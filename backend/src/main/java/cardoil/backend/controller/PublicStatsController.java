package cardoil.backend.controller;

import cardoil.backend.dto.response.PublicStatsResponse;
import cardoil.backend.service.PublicStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Pas de @PreAuthorize — accessible sans connexion (page de login),
// à ajouter aux chemins publics de SecurityConfig.
@RestController
@RequiredArgsConstructor
public class PublicStatsController {

    private final PublicStatsService publicStatsService;

    @GetMapping("/api/public/stats")
    public ResponseEntity<PublicStatsResponse> getStats() {
        return ResponseEntity.ok(publicStatsService.getStats());
    }
}