package cardoil.backend.controller;

import cardoil.backend.dto.request.CarteRequest;
import cardoil.backend.dto.request.RechargeGroupeeRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.request.StatutCarteRequest;
import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.RechargeGroupeeResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.service.AdminCarteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cartes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ENTREPRISE')")
public class AdminCarteController {

    private final AdminCarteService adminCarteService;

    @GetMapping
    public ResponseEntity<List<CarteResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminCarteService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<CarteResponse> create(Authentication authentication,
                                                 @Valid @RequestBody CarteRequest request) {
        return ResponseEntity.ok(adminCarteService.create(authentication.getName(), request));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<CarteResponse> changerStatut(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody StatutCarteRequest request) {
        return ResponseEntity.ok(adminCarteService.changerStatut(authentication.getName(), id, request.getStatut()));
    }

    @PostMapping("/{id}/recharger")
    public ResponseEntity<RechargeResponse> recharger(Authentication authentication,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody RechargeRequest request) {
        return ResponseEntity.ok(adminCarteService.recharger(authentication.getName(), id, request));
    }

    @GetMapping("/{id}/recharges")
    public ResponseEntity<List<RechargeResponse>> historique(Authentication authentication,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(adminCarteService.getHistoriqueRecharges(authentication.getName(), id));
    }

    @PostMapping("/{id}/renouveler")
    public ResponseEntity<CarteResponse> renouveler(Authentication authentication,
                                                      @PathVariable Long id) {
        return ResponseEntity.ok(adminCarteService.renouveler(authentication.getName(), id));
    }

    @PostMapping("/recharge-groupee")
    public ResponseEntity<RechargeGroupeeResponse> rechargerGroupe(Authentication authentication,
                                                                     @Valid @RequestBody RechargeGroupeeRequest request) {
        return ResponseEntity.ok(adminCarteService.rechargerGroupe(authentication.getName(), request));
    }

    @GetMapping("/recharges-groupees")
    public ResponseEntity<List<RechargeGroupeeResponse>> historiqueGroupe(Authentication authentication) {
        return ResponseEntity.ok(adminCarteService.getHistoriqueRechargesGroupees(authentication.getName()));
    }
}