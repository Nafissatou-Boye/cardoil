package cardoil.backend.service.impl;

import cardoil.backend.dto.request.PersonnelRequest;
import cardoil.backend.dto.response.PersonnelResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Role;
import cardoil.backend.entity.Station;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.EntrepriseRepository;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminPersonnelService;
import cardoil.backend.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPersonnelServiceImpl implements AdminPersonnelService {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EntrepriseRepository entrepriseRepository;

    private static final String CHARS_MDP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
public PersonnelResponse createAdminEntreprise(String login, Long entrepriseId, PersonnelRequest request) {
    Compagnie compagnie = getCompagnie(login);

    Entreprise entreprise = entrepriseRepository.findByIdAndCompagnieId(entrepriseId, compagnie.getId())
            .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));

    if (utilisateurRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Un compte existe déjà avec cet email");
    }

    String loginGenere = genererLoginUnique();
    String mdpClair = genererMotDePasse();

    Utilisateur utilisateur = Utilisateur.builder()
            .login(loginGenere)
            .motDePasse(passwordEncoder.encode(mdpClair))
            .role(Role.ADMIN_ENTREPRISE)
            .nom(request.getNom())
            .prenom(request.getPrenom())
            .email(request.getEmail())
            .actif(true)
            .doitChangerMotDePasse(true)
            .compagnie(compagnie)
            .entreprise(entreprise)
            .build();

    utilisateur = utilisateurRepository.save(utilisateur);

    try {
        emailService.envoyerCredentials(
            request.getEmail(),
            request.getPrenom(),
            request.getNom(),
            loginGenere,
            mdpClair,
            entreprise.getNom()
        );
    } catch (Exception e) {
        System.err.println("⚠️ Email non envoyé : " + e.getMessage());
    }

    PersonnelResponse response = toResponse(utilisateur);
    response.setMotDePasseTemporaire(mdpClair);
    return response;

}

    @Override
    public List<PersonnelResponse> getAll(String login) {
        Compagnie compagnie = getCompagnie(login);
        return utilisateurRepository.findByCompagnieIdAndRoleIn(compagnie.getId(),
                List.of(Role.GERANT, Role.POMPISTE)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PersonnelResponse create(String login, PersonnelRequest request) {
        Compagnie compagnie = getCompagnie(login);

        // Vérifier unicité de l'email AVANT de créer
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        String loginGenere = genererLoginUnique();
        String mdpClair = genererMotDePasse();

        Role role = request.getRole() != null ? request.getRole() : Role.GERANT;

        Utilisateur utilisateur = Utilisateur.builder()
                .login(loginGenere)
                .motDePasse(passwordEncoder.encode(mdpClair))
                .role(role)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .actif(request.isActif())
                .doitChangerMotDePasse(true)
                .compagnie(compagnie)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);
        assignerStation(compagnie, utilisateur, request.getStationId());

        String nomStation = null;
        if (request.getStationId() != null) {
            nomStation = stationRepository.findById(request.getStationId())
                    .map(Station::getNom)
                    .orElse(null);
        }

        try {
            emailService.envoyerCredentials(
                request.getEmail(),
                request.getPrenom(),
                request.getNom(),
                loginGenere,
                mdpClair,
                nomStation
            );
        } catch (Exception e) {
            System.err.println("⚠️ Email non envoyé : " + e.getMessage());
        }

        return toResponse(utilisateur);
    }

    @Override
    public PersonnelResponse update(String login, Long id, PersonnelRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Utilisateur utilisateur = utilisateurRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé"));

        if (utilisateur.getRole() != Role.GERANT && utilisateur.getRole() != Role.POMPISTE) {
            throw new IllegalStateException("Cet utilisateur ne peut pas être modifié depuis cette page");
        }

        if (request.getLogin() != null && !request.getLogin().isBlank()
                && !utilisateur.getLogin().equals(request.getLogin())) {
            if (utilisateurRepository.existsByLogin(request.getLogin())) {
                throw new IllegalArgumentException("Ce login est déjà utilisé");
            }
            utilisateur.setLogin(request.getLogin());
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setActif(request.isActif());

        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }

        utilisateurRepository.save(utilisateur);
        assignerStation(compagnie, utilisateur, request.getStationId());

        return toResponse(utilisateur);
    }

    @Override
    public void delete(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);

        Utilisateur utilisateur = utilisateurRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé"));

        if (utilisateur.getRole() != Role.GERANT && utilisateur.getRole() != Role.POMPISTE) {
            throw new IllegalStateException("Cet utilisateur ne peut pas être supprimé depuis cette page");
        }

        stationRepository.findByGerantId(utilisateur.getId())
                .forEach(station -> {
                    station.setGerant(null);
                    stationRepository.save(station);
                });

        utilisateurRepository.delete(utilisateur);
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

    private void assignerStation(Compagnie compagnie, Utilisateur utilisateur, Long nouvelleStationId) {
        stationRepository.findByGerantId(utilisateur.getId())
                .forEach(station -> {
                    if (nouvelleStationId == null || !station.getId().equals(nouvelleStationId)) {
                        station.setGerant(null);
                        stationRepository.save(station);
                    }
                });

        if (nouvelleStationId != null) {
            Station station = stationRepository.findByIdAndCompagnieId(nouvelleStationId, compagnie.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Station non trouvée"));
            station.setGerant(utilisateur);
            stationRepository.save(station);
        }
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }

        return utilisateur.getCompagnie();
    }

    private PersonnelResponse toResponse(Utilisateur utilisateur) {
        PersonnelResponse.StationInfo stationInfo = stationRepository.findByGerantId(utilisateur.getId())
                .stream()
                .findFirst()
                .map(s -> PersonnelResponse.StationInfo.builder()
                        .id(s.getId())
                        .nom(s.getNom())
                        .build())
                .orElse(null);

        return PersonnelResponse.builder()
                .id(utilisateur.getId())
                .login(utilisateur.getLogin())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .actif(utilisateur.isActif())
                .dateCreation(utilisateur.getDateCreation())
                .derniereConnexion(utilisateur.getDerniereConnexion())
                .station(stationInfo)
                .build();
    }

    @Override
public PersonnelResponse remplacerAdminEntreprise(String login, Long entrepriseId, PersonnelRequest request) {
    Compagnie compagnie = getCompagnie(login);

    Entreprise entreprise = entrepriseRepository.findByIdAndCompagnieId(entrepriseId, compagnie.getId())
            .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));

    // Désactiver l'ancien admin
  utilisateurRepository.findByEntrepriseIdAndActif(entrepriseId, true)
    .ifPresent(ancien -> {
        ancien.setActif(false);
        ancien.setEntreprise(null);
        utilisateurRepository.save(ancien);
    });

    if (utilisateurRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Un compte existe déjà avec cet email");
    }

    String loginGenere = genererLoginUnique();
    String mdpClair = genererMotDePasse();

    Utilisateur utilisateur = Utilisateur.builder()
            .login(loginGenere)
            .motDePasse(passwordEncoder.encode(mdpClair))
            .role(Role.ADMIN_ENTREPRISE)
            .nom(request.getNom())
            .prenom(request.getPrenom())
            .email(request.getEmail())
            .actif(true)
            .doitChangerMotDePasse(true)
            .compagnie(compagnie)
            .entreprise(entreprise)
            .build();

    utilisateur = utilisateurRepository.save(utilisateur);

    try {
        emailService.envoyerCredentials(
            request.getEmail(),
            request.getPrenom(),
            request.getNom(),
            loginGenere,
            mdpClair,
            entreprise.getNom()
        );
    } catch (Exception e) {
        System.err.println("⚠️ Email non envoyé : " + e.getMessage());
    }

    PersonnelResponse response = toResponse(utilisateur);
    response.setMotDePasseTemporaire(mdpClair);
    return response;
}
}