package cardoil.backend.service.impl;

import cardoil.backend.dto.request.AdminCompagnieRequest;
import cardoil.backend.dto.response.AdminCompagnieResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Role;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.EmailService;
import cardoil.backend.service.SuperAdminUtilisateurService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminUtilisateurServiceImpl implements SuperAdminUtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final CompagnieRepository compagnieRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String CHARS_MDP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public List<AdminCompagnieResponse> getAll() {
        return utilisateurRepository.findByRole(Role.ADMIN_COMPAGNIE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public AdminCompagnieResponse create(AdminCompagnieRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        Compagnie compagnie = compagnieRepository.findById(request.getCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée"));

        String loginGenere = genererLoginUnique();
        String mdpClair = genererMotDePasse();

        Utilisateur utilisateur = Utilisateur.builder()
                .login(loginGenere)
                .motDePasse(passwordEncoder.encode(mdpClair))
                .role(Role.ADMIN_COMPAGNIE)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .actif(request.isActif())
                .compagnie(compagnie)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        try {
            emailService.envoyerCredentials(
                request.getEmail(),
                request.getPrenom(),
                request.getNom(),
                loginGenere,
                mdpClair,
                compagnie.getNom()
            );
        } catch (Exception e) {
            System.err.println("⚠️ Email non envoyé : " + e.getMessage());
        }

        return toResponse(utilisateur);
    }

    @Override
    public AdminCompagnieResponse update(Long id, AdminCompagnieRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByIdAndRole(id, Role.ADMIN_COMPAGNIE)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur non trouvé"));

        Compagnie compagnie = compagnieRepository.findById(request.getCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée"));

        if (!utilisateur.getEmail().equals(request.getEmail())
                && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setActif(request.isActif());
        utilisateur.setCompagnie(compagnie);

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public void delete(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findByIdAndRole(id, Role.ADMIN_COMPAGNIE)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur non trouvé"));
        utilisateurRepository.delete(utilisateur);
    }

    @Override
    public AdminCompagnieResponse toggleActif(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findByIdAndRole(id, Role.ADMIN_COMPAGNIE)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur non trouvé"));
        utilisateur.setActif(!utilisateur.isActif());
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    // ===== HELPERS =====

    private String genererLoginUnique() {
        String login;
        do {
            login = String.format("%07d", random.nextInt(10_000_000));
        } while (utilisateurRepository.existsByLogin(login));
        return login;
    }

    private String genererMotDePasse() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS_MDP.charAt(random.nextInt(CHARS_MDP.length())));
        }
        return sb.toString();
    }

    private AdminCompagnieResponse toResponse(Utilisateur u) {
        return AdminCompagnieResponse.builder()
                .id(u.getId())
                .login(u.getLogin())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .actif(u.isActif())
                .compagnieNom(u.getCompagnie() != null ? u.getCompagnie().getNom() : null)
                .compagnieId(u.getCompagnie() != null ? u.getCompagnie().getId() : null)
                .dateCreation(u.getDateCreation())
                .derniereConnexion(u.getDerniereConnexion())
                .build();
    }
}