package cardoil.backend.controller;

import cardoil.backend.dto.response.UtilisateurDetailResponse;
import cardoil.backend.dto.response.UtilisateurListItemResponse;
import cardoil.backend.service.AdminUtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ENTREPRISE')")
public class AdminUtilisateurController {

    private final AdminUtilisateurService adminUtilisateurService;

    @GetMapping
    public ResponseEntity<List<UtilisateurListItemResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(adminUtilisateurService.getAll(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDetailResponse> getDetail(Authentication authentication,
                                                                  @PathVariable Long id) {
        return ResponseEntity.ok(adminUtilisateurService.getDetail(authentication.getName(), id));
    }
}