package cardoil.backend.controller;

import cardoil.backend.dto.request.StationAdminRequest;
import cardoil.backend.dto.response.StationResponse;
import cardoil.backend.service.AdminStationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminStationController {

    private final AdminStationService adminStationService;

    @GetMapping
    public ResponseEntity<List<StationResponse>> getMesStations(Authentication authentication) {
        return ResponseEntity.ok(adminStationService.getMesStations(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<StationResponse> create(Authentication authentication,
                                                    @Valid @RequestBody StationAdminRequest request) {
        return ResponseEntity.ok(adminStationService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StationResponse> update(Authentication authentication,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody StationAdminRequest request) {
        return ResponseEntity.ok(adminStationService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminStationService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}