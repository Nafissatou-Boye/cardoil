package cardoil.backend.controller;

import cardoil.backend.dto.response.GerantDashboardResponse;
import cardoil.backend.service.GerantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gerant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GERANT')")
public class GerantController {

    private final GerantService gerantService;

    @GetMapping("/dashboard")
    public ResponseEntity<GerantDashboardResponse> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(gerantService.getDashboard(authentication.getName()));
    }
}