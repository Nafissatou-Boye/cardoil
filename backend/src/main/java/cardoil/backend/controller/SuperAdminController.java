package cardoil.backend.controller;

import cardoil.backend.dto.response.DashboardSuperAdminResponse;
import cardoil.backend.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<DashboardSuperAdminResponse> getDashboard() {
        return ResponseEntity.ok(superAdminService.getDashboard());
    }
}