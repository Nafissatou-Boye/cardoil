package cardoil.backend.controller;

import cardoil.backend.dto.response.RapportGlobalResponse;
import cardoil.backend.service.AdminRapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rapports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminRapportController {

    private final AdminRapportService adminRapportService;

    @GetMapping
    public ResponseEntity<RapportGlobalResponse> getRapport(
            Authentication authentication,
            @RequestParam(defaultValue = "30D") String periode) {
        return ResponseEntity.ok(adminRapportService.getRapport(authentication.getName(), periode));
    }
}