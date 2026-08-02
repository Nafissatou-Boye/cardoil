package cardoil.backend.controller;

import cardoil.backend.dto.response.ServiceConsultationResponse;
import cardoil.backend.entity.CategorieService;
import cardoil.backend.entity.ServiceCatalogue;
import cardoil.backend.entity.StatutService;
import cardoil.backend.repository.ServiceCatalogueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Distinct de AdminCompagnieServiceController (/api/admin/services, gestion
// par l'admin compagnie) — celui-ci sert la consultation en lecture seule
// pour Client/Employé, filtrée sur ACTIF uniquement. Réutilise
// ServiceCatalogueRepository.findByCompagnieIdAndStatut, déjà présent dans
// l'interface mais jamais exposé côté consultation — d'où le 404.
@RestController
@RequiredArgsConstructor
public class ServiceCatalogueConsultationController {

    private final ServiceCatalogueRepository serviceCatalogueRepository;

    // Même chemin que celui déjà attendu côté Flutter (ServiceService),
    // pour n'avoir à corriger que ce côté-ci.
    @GetMapping("/api/services/catalogue/active")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYE')")
    public ResponseEntity<List<ServiceConsultationResponse>> getActiveServices(
            @RequestParam Long companyId) {
        List<ServiceConsultationResponse> services = serviceCatalogueRepository
                .findByCompagnieIdAndStatut(companyId, StatutService.ACTIF).stream()
                .sorted((a, b) -> Integer.compare(a.getOrdreTri(), b.getOrdreTri()))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(services);
    }

    private ServiceConsultationResponse toResponse(ServiceCatalogue s) {
        return ServiceConsultationResponse.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getNom())
                .categoryName(categorieLabel(s.getCategorie()))
                .description(s.getDescription())
                .prix(s.getPrix())
                .iconUrl(s.getIcone())
                .colorHex(s.getCouleurHex())
                .status(s.getStatut() != null ? s.getStatut().name() : null)
                .mandatory(s.isObligatoire())
                .defaultDisplayOrder(s.getOrdreTri())
                .companyId(s.getCompagnie().getId())
                .build();
    }

    // Même mapping que categorieLabel() côté formulaire admin Angular, pour
    // rester cohérent entre les deux surfaces.
    private String categorieLabel(CategorieService categorie) {
        if (categorie == null) return null;
        return switch (categorie) {
            case ENERGIE -> "Énergie";
            case LAVAGE -> "Lavage";
            case ENTRETIEN -> "Entretien";
            case BOUTIQUE -> "Boutique";
            case SERVICES_DIGITAUX -> "Services digitaux";
            case AUTRE -> "Autre";
        };
    }
}