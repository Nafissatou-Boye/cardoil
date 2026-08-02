package cardoil.backend.controller;

import cardoil.backend.dto.response.PromotionConsultationResponse;
import cardoil.backend.entity.Promotion;
import cardoil.backend.entity.StatutPromotion;
import cardoil.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/fidelite/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYE')")
public class PromotionConsultationController {

    private final PromotionRepository promotionRepository;


    @GetMapping
    public ResponseEntity<List<PromotionConsultationResponse>> getAll(@RequestParam Long companyId) {
        return ResponseEntity.ok(promotionRepository.findByCompagnieIdOrderByDateDebutDesc(companyId).stream()
                .map(this::toResponse)
                .toList());
    }

 
    @GetMapping("/active")
    public ResponseEntity<List<PromotionConsultationResponse>> getActive(@RequestParam Long companyId) {
        return ResponseEntity.ok(promotionRepository.findByCompagnieIdOrderByDateDebutDesc(companyId).stream()
                .filter(p -> p.getStatut() == StatutPromotion.ACTIVE)
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionConsultationResponse> getOne(@PathVariable Long id) {
        return promotionRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private PromotionConsultationResponse toResponse(Promotion p) {
        return PromotionConsultationResponse.builder()
                .id(p.getId())
                .name(p.getNom())
                .description(p.getDescription())
                .startDate(p.getDateDebut() != null ? p.getDateDebut().toString() : null)
                .endDate(p.getDateFin() != null ? p.getDateFin().toString() : null)
                .status(mapStatut(p.getStatut()))
                .type(p.getType() != null ? p.getType().name() : null)
                .minPurchaseAmount(p.getMontantMinimum())
                .companyId(p.getCompagnie() != null ? p.getCompagnie().getId() : null)
                .build();
    }

    private String mapStatut(StatutPromotion statut) {
        if (statut == StatutPromotion.ACTIVE) return "ACTIVE";
        if (statut == StatutPromotion.EXPIREE) return "EXPIRED";
        return "INACTIVE";
    }
}