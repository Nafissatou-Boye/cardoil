package cardoil.backend.controller;

import cardoil.backend.dto.request.ChangePasswordRequest;
import cardoil.backend.dto.request.LoginRequest;
import cardoil.backend.dto.request.RegisterRequest;
import cardoil.backend.dto.response.LoginResponse;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UtilisateurRepository utilisateurRepository;
   

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
    cardoil.backend.entity.Utilisateur utilisateur = cardoil.backend.entity.Utilisateur.builder()
        .login(request.getLogin())
        .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
        .role(cardoil.backend.entity.Role.valueOf(request.getRole()))
        .nom(request.getNom())
        .prenom(request.getPrenom())
        .email(request.getEmail())
        .build();

    utilisateurRepository.save(utilisateur);
    return ResponseEntity.ok("Utilisateur créé avec succès");
}

private final PasswordEncoder passwordEncoder;

@PostMapping("/change-password")
public ResponseEntity<String> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        org.springframework.security.core.Authentication authentication) {

    String login = authentication.getName();
    cardoil.backend.entity.Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Utilisateur non trouvé"));

    if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
        return ResponseEntity.status(400).body("Ancien mot de passe incorrect");
    }

    utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
    utilisateur.setDoitChangerMotDePasse(false);
    utilisateurRepository.save(utilisateur);

    return ResponseEntity.ok("Mot de passe changé avec succès");
}
}