package cardoil.backend.controller;

import cardoil.backend.dto.request.PrixRequest;
import cardoil.backend.dto.response.PrixJourResponse;
import cardoil.backend.dto.response.PrixProduitDTO;
import cardoil.backend.service.AdminPrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/produits/{produitId}/prix")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminPrixController {

    private final AdminPrixService adminPrixService;

    @PostMapping
    public ResponseEntity<PrixProduitDTO> definirPrix(Authentication authentication,
                                                        @PathVariable Long produitId,
                                                        @RequestBody PrixRequest request) {
        return ResponseEntity.ok(adminPrixService.definirPrix(authentication.getName(), produitId, request));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<PrixJourResponse>> getHistorique(Authentication authentication,
                                                                  @PathVariable Long produitId) {
        return ResponseEntity.ok(adminPrixService.getHistorique(authentication.getName(), produitId));
    }

    // 🆕 Toutes les configurations PrixProduit (passées, en vigueur, futures) — contrairement à
    // getHistorique() qui ne montre que PrixJour (jamais le futur programmé).
    @GetMapping("/programmation")
    public ResponseEntity<List<PrixProduitDTO>> getProgrammation(Authentication authentication,
                                                                   @PathVariable Long produitId) {
        return ResponseEntity.ok(adminPrixService.getProgrammation(authentication.getName(), produitId));
    }
}