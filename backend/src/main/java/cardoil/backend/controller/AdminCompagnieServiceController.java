package cardoil.backend.controller;

import cardoil.backend.dto.request.AssignerStationsRequest;
import cardoil.backend.dto.request.ChangerStatutServiceRequest;
import cardoil.backend.dto.request.ServiceCatalogueRequest;
import cardoil.backend.dto.response.ServiceCatalogueResponse;
import cardoil.backend.service.AdminCompagnieServiceCatalogueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminCompagnieServiceController {

    private final AdminCompagnieServiceCatalogueService serviceCatalogueService;

    @GetMapping
    public ResponseEntity<List<ServiceCatalogueResponse>> getServices(Authentication authentication) {
        return ResponseEntity.ok(serviceCatalogueService.getServices(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCatalogueResponse> getService(
            Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(serviceCatalogueService.getService(authentication.getName(), id));
    }

    @PostMapping
    public ResponseEntity<ServiceCatalogueResponse> creerService(
            Authentication authentication, @Valid @RequestBody ServiceCatalogueRequest request) {
        return ResponseEntity.ok(serviceCatalogueService.creerService(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceCatalogueResponse> modifierService(
            Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody ServiceCatalogueRequest request) {
        return ResponseEntity.ok(serviceCatalogueService.modifierService(authentication.getName(), id, request));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ServiceCatalogueResponse> changerStatut(
            Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody ChangerStatutServiceRequest request) {
        return ResponseEntity.ok(serviceCatalogueService.changerStatut(authentication.getName(), id, request));
    }

    @PutMapping("/{id}/stations")
    public ResponseEntity<ServiceCatalogueResponse> assignerStations(
            Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody AssignerStationsRequest request) {
        return ResponseEntity.ok(serviceCatalogueService.assignerStations(authentication.getName(), id, request));
    }
}