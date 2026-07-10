package cardoil.backend.controller;

import cardoil.backend.dto.response.DashboardAdminCompagnieResponse;
import cardoil.backend.service.AdminCompagnieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminCompagnieController {

    private final AdminCompagnieService adminCompagnieService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAdminCompagnieResponse> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(adminCompagnieService.getDashboard(authentication.getName()));
    }
}