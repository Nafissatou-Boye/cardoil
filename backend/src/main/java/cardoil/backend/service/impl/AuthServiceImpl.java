package cardoil.backend.service.impl;

import cardoil.backend.dto.request.LoginRequest;
import cardoil.backend.dto.response.LoginResponse;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.security.JwtUtils;
import cardoil.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Login ou mot de passe incorrect"));

        if (utilisateur.isBloque()) {
            throw new LockedException("Compte bloqué après trop de tentatives échouées");
        }

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            utilisateur.setTentativesEchouees(utilisateur.getTentativesEchouees() + 1);
            if (utilisateur.getTentativesEchouees() >= 5) {
                utilisateur.setBloque(true);
            }
            utilisateurRepository.save(utilisateur);
            throw new BadCredentialsException("Login ou mot de passe incorrect");
        }

        utilisateur.setTentativesEchouees(0);
        utilisateurRepository.save(utilisateur);

        String token = jwtUtils.generateToken(
                utilisateur.getLogin(),
                utilisateur.getRole().name()
        );

       return LoginResponse.builder()
        .token(token)
        .role(utilisateur.getRole().name())
        .nom(utilisateur.getNom())
        .prenom(utilisateur.getPrenom())
        .doitChangerMotDePasse(utilisateur.isDoitChangerMotDePasse())
        .build();
    }
}