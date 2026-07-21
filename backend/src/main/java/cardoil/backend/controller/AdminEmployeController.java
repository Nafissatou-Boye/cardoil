package cardoil.backend.controller;

import cardoil.backend.dto.request.EmployeRequest;
import cardoil.backend.dto.response.EmployeResponse;
import cardoil.backend.service.AdminEmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/employes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ENTREPRISE')")
public class AdminEmployeController {

    private final AdminEmployeService adminEmployeService;

    @GetMapping
    public ResponseEntity<List<EmployeResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminEmployeService.getAll(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> create(Authentication authentication,
                                                   @Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(adminEmployeService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeResponse> update(Authentication authentication,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.ok(adminEmployeService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        adminEmployeService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}