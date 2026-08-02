package cardoil.backend.controller;

import cardoil.backend.dto.response.RapportGlobalResponse;
import cardoil.backend.service.AdminCompagnieRapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminCompagnieRapportController {

    private final AdminCompagnieRapportService adminCompagnieRapportService;

    @GetMapping("/api/admin/rapports")
    public ResponseEntity<RapportGlobalResponse> getRapport(
            Authentication authentication,
            @RequestParam String periode) {
        return ResponseEntity.ok(adminCompagnieRapportService.getRapport(authentication.getName(), periode));
    }
}