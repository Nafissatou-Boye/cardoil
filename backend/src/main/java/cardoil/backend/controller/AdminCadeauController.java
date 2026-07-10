package cardoil.backend.controller;

import cardoil.backend.dto.request.CadeauRequest;
import cardoil.backend.dto.response.CadeauResponse;
import cardoil.backend.service.AdminCadeauService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cadeaux")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminCadeauController {

    private final AdminCadeauService adminCadeauService;

    @GetMapping
    public ResponseEntity<List<CadeauResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminCadeauService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<CadeauResponse> create(Authentication authentication,
                                                   @Valid @RequestBody CadeauRequest request) {
        return ResponseEntity.ok(adminCadeauService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CadeauResponse> update(Authentication authentication,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody CadeauRequest request) {
        return ResponseEntity.ok(adminCadeauService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminCadeauService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CadeauResponse> toggleActif(Authentication authentication,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(adminCadeauService.toggleActif(authentication.getName(), id));
    }
}