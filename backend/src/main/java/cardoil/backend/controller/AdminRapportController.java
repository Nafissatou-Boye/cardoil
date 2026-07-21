package cardoil.backend.controller;

import cardoil.backend.dto.response.RapportDepartementResponse;
import cardoil.backend.dto.response.RapportEmployeResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.dto.response.SuiviBudgetResponse;
import cardoil.backend.service.AdminRapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rapports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ENTREPRISE')")
public class AdminRapportController {

    private final AdminRapportService adminRapportService;

    @GetMapping("/departements")
    public ResponseEntity<List<RapportDepartementResponse>> getRapportDepartements(Authentication authentication) {
        return ResponseEntity.ok(adminRapportService.getRapportDepartements(authentication.getName()));
    }

    @GetMapping("/employes")
    public ResponseEntity<List<RapportEmployeResponse>> getRapportEmployes(Authentication authentication) {
        return ResponseEntity.ok(adminRapportService.getRapportEmployes(authentication.getName()));
    }

    @GetMapping("/budget")
    public ResponseEntity<SuiviBudgetResponse> getSuiviBudget(Authentication authentication) {
        return ResponseEntity.ok(adminRapportService.getSuiviBudget(authentication.getName()));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<RechargeResponse>> getHistoriqueGlobal(Authentication authentication) {
        return ResponseEntity.ok(adminRapportService.getHistoriqueGlobal(authentication.getName()));
    }
}