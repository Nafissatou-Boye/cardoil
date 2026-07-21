package cardoil.backend.controller;

import cardoil.backend.dto.request.CarteRequest;
import cardoil.backend.dto.request.EmployeDepartementRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.request.StatutCarteRequest;
import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.EmployeResponse;
import cardoil.backend.dto.response.EspaceDepartementInfoResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.service.EspaceDepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departement")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_DEPARTEMENT')")
public class EspaceDepartementController {

    private final EspaceDepartementService espaceDepartementService;

    @GetMapping("/info")
    public ResponseEntity<EspaceDepartementInfoResponse> getInfo(Authentication authentication) {
        return ResponseEntity.ok(espaceDepartementService.getInfo(authentication.getName()));
    }

    @GetMapping("/employes")
    public ResponseEntity<List<EmployeResponse>> getEmployes(Authentication authentication) {
        return ResponseEntity.ok(espaceDepartementService.getEmployes(authentication.getName()));
    }

    @PostMapping("/employes")
    public ResponseEntity<EmployeResponse> createEmploye(Authentication authentication,
                                                            @Valid @RequestBody EmployeDepartementRequest request) {
        return ResponseEntity.ok(espaceDepartementService.createEmploye(authentication.getName(), request));
    }

    @PutMapping("/employes/{id}")
    public ResponseEntity<EmployeResponse> updateEmploye(Authentication authentication,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody EmployeDepartementRequest request) {
        return ResponseEntity.ok(espaceDepartementService.updateEmploye(authentication.getName(), id, request));
    }

    @DeleteMapping("/employes/{id}")
    public ResponseEntity<Void> deleteEmploye(Authentication authentication, @PathVariable Long id) {
        espaceDepartementService.deleteEmploye(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cartes")
    public ResponseEntity<List<CarteResponse>> getCartes(Authentication authentication) {
        return ResponseEntity.ok(espaceDepartementService.getCartes(authentication.getName()));
    }

    @PostMapping("/cartes")
    public ResponseEntity<CarteResponse> createCarte(Authentication authentication,
                                                        @Valid @RequestBody CarteRequest request) {
        return ResponseEntity.ok(espaceDepartementService.createCarte(authentication.getName(), request));
    }

    @PatchMapping("/cartes/{id}/statut")
    public ResponseEntity<CarteResponse> changerStatut(Authentication authentication,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody StatutCarteRequest request) {
        return ResponseEntity.ok(espaceDepartementService.changerStatutCarte(authentication.getName(), id, request.getStatut()));
    }

    @PostMapping("/cartes/{id}/recharger")
    public ResponseEntity<RechargeResponse> recharger(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody RechargeRequest request) {
        return ResponseEntity.ok(espaceDepartementService.rechargerCarte(authentication.getName(), id, request));
    }

    @GetMapping("/cartes/{id}/recharges")
    public ResponseEntity<List<RechargeResponse>> historique(Authentication authentication,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(espaceDepartementService.getHistoriqueRecharges(authentication.getName(), id));
    }
}