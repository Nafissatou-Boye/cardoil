package cardoil.backend.service.impl;

import cardoil.backend.dto.request.AdminDepartementRequest;
import cardoil.backend.dto.request.DepartementRequest;
import cardoil.backend.dto.response.AdminDepartementInfoResponse;
import cardoil.backend.dto.response.DepartementResponse;
import cardoil.backend.dto.response.EntrepriseInfoResponse;
import cardoil.backend.entity.Departement;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Role;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.DepartementRepository;
import cardoil.backend.repository.EmployeRepository;
import cardoil.backend.repository.EntrepriseRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminDepartementService;
import cardoil.backend.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDepartementServiceImpl implements AdminDepartementService {

    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String CHARS_MDP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public List<DepartementResponse> getAll(String login) {
        Entreprise entreprise = getEntreprise(login);
        return departementRepository.findByEntrepriseId(entreprise.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DepartementResponse create(String login, DepartementRequest request) {
        Entreprise entreprise = getEntreprise(login);

        BigDecimal budgetDemande = request.getBudget() != null ? request.getBudget() : BigDecimal.ZERO;

        if (budgetDemande.compareTo(BigDecimal.ZERO) > 0) {
            if (entreprise.getSoldeDisponible().compareTo(budgetDemande) < 0) {
                throw new IllegalArgumentException(
                        "Solde entreprise insuffisant : disponible " + entreprise.getSoldeDisponible()
                                + " FCFA, demandé " + budgetDemande + " FCFA");
            }
            entreprise.setSoldeDisponible(entreprise.getSoldeDisponible().subtract(budgetDemande));
            entrepriseRepository.save(entreprise);
        }

        Departement departement = Departement.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .budget(budgetDemande)
                .actif(request.isActif())
                .entreprise(entreprise)
                .build();

        return toResponse(departementRepository.save(departement));
    }

    @Override
    public DepartementResponse update(String login, Long id, DepartementRequest request) {
        Entreprise entreprise = getEntreprise(login);

        Departement departement = departementRepository.findByIdAndEntrepriseId(id, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        BigDecimal ancienBudget = departement.getBudget() != null ? departement.getBudget() : BigDecimal.ZERO;
        BigDecimal nouveauBudget = request.getBudget() != null ? request.getBudget() : BigDecimal.ZERO;
        BigDecimal delta = nouveauBudget.subtract(ancienBudget);

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            if (entreprise.getSoldeDisponible().compareTo(delta) < 0) {
                throw new IllegalArgumentException(
                        "Solde entreprise insuffisant pour augmenter ce budget de " + delta + " FCFA");
            }
            entreprise.setSoldeDisponible(entreprise.getSoldeDisponible().subtract(delta));
            entrepriseRepository.save(entreprise);
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            entreprise.setSoldeDisponible(entreprise.getSoldeDisponible().add(delta.abs()));
            entrepriseRepository.save(entreprise);
        }

        departement.setNom(request.getNom());
        departement.setDescription(request.getDescription());
        departement.setBudget(nouveauBudget);
        departement.setActif(request.isActif());

        return toResponse(departementRepository.save(departement));
    }

    @Override
    public void delete(String login, Long id) {
        Entreprise entreprise = getEntreprise(login);

        Departement departement = departementRepository.findByIdAndEntrepriseId(id, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        long nbEmployes = employeRepository.findByDepartementId(departement.getId()).size();
        if (nbEmployes > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer : " + nbEmployes + " employé(s) encore rattaché(s) à ce département");
        }

        // Le budget non consommé du département retourne au solde disponible de l'entreprise
        BigDecimal budgetARestituer = departement.getBudget() != null ? departement.getBudget() : BigDecimal.ZERO;
        if (budgetARestituer.compareTo(BigDecimal.ZERO) > 0) {
            entreprise.setSoldeDisponible(entreprise.getSoldeDisponible().add(budgetARestituer));
            entrepriseRepository.save(entreprise);
        }

        departementRepository.delete(departement);
    }

    @Override
    public DepartementResponse toggleActif(String login, Long id) {
        Entreprise entreprise = getEntreprise(login);

        Departement departement = departementRepository.findByIdAndEntrepriseId(id, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        departement.setActif(!departement.isActif());
        return toResponse(departementRepository.save(departement));
    }

    @Override
    public DepartementResponse crediterBudget(String login, Long id, BigDecimal montant) {
        Entreprise entreprise = getEntreprise(login);

        Departement departement = departementRepository.findByIdAndEntrepriseId(id, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        if (montant == null || montant.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Le montant doit être d'au moins 1 FCFA");
        }

        if (entreprise.getSoldeDisponible().compareTo(montant) < 0) {
            throw new IllegalArgumentException(
                    "Solde entreprise insuffisant : disponible " + entreprise.getSoldeDisponible()
                            + " FCFA, demandé " + montant + " FCFA");
        }

        entreprise.setSoldeDisponible(entreprise.getSoldeDisponible().subtract(montant));
        entrepriseRepository.save(entreprise);

        BigDecimal budgetActuel = departement.getBudget() != null ? departement.getBudget() : BigDecimal.ZERO;
        departement.setBudget(budgetActuel.add(montant));

        return toResponse(departementRepository.save(departement));
    }

    @Override
    public EntrepriseInfoResponse getInfoEntreprise(String login) {
        Entreprise entreprise = getEntreprise(login);
        return EntrepriseInfoResponse.builder()
                .id(entreprise.getId())
                .nom(entreprise.getNom())
                .soldeDisponible(entreprise.getSoldeDisponible())
                .build();
    }

    // ===== ADMIN DÉPARTEMENT =====

    @Override
    public AdminDepartementInfoResponse getAdmin(String login, Long departementId) {
        Entreprise entreprise = getEntreprise(login);

        departementRepository.findByIdAndEntrepriseId(departementId, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        return utilisateurRepository.findByDepartementGereIdAndActif(departementId, true)
                .map(u -> AdminDepartementInfoResponse.builder()
                        .id(u.getId())
                        .login(u.getLogin())
                        .nom(u.getNom())
                        .prenom(u.getPrenom())
                        .email(u.getEmail())
                        .actif(u.isActif())
                        .build())
                .orElse(null);
    }

    @Override
    public AdminDepartementInfoResponse createAdmin(String login, Long departementId, AdminDepartementRequest request) {
        Entreprise entreprise = getEntreprise(login);

        Departement departement = departementRepository.findByIdAndEntrepriseId(departementId, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        String loginGenere = genererLoginUnique();
        String mdpClair = genererMotDePasse();

        Utilisateur utilisateur = Utilisateur.builder()
                .login(loginGenere)
                .motDePasse(passwordEncoder.encode(mdpClair))
                .role(Role.ADMIN_DEPARTEMENT)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .actif(true)
                .doitChangerMotDePasse(true)
                .entreprise(entreprise)
                .departementGere(departement)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        try {
            emailService.envoyerCredentialsAdminDepartement(
                    request.getEmail(),
                    request.getPrenom(),
                    request.getNom(),
                    loginGenere,
                    mdpClair,
                    entreprise.getNom(),
                    departement.getNom()
            );
        } catch (Exception e) {
            log.error("Email non envoyé pour l'admin département {} : {}", loginGenere, e.getMessage());
        }

        return AdminDepartementInfoResponse.builder()
                .id(utilisateur.getId())
                .login(loginGenere)
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .actif(true)
                .motDePasseTemporaire(mdpClair)
                .build();
    }

    @Override
    public AdminDepartementInfoResponse remplacerAdmin(String login, Long departementId, AdminDepartementRequest request) {
        Entreprise entreprise = getEntreprise(login);

        Departement departement = departementRepository.findByIdAndEntrepriseId(departementId, entreprise.getId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));

        utilisateurRepository.findByDepartementGereIdAndActif(departementId, true)
                .ifPresent(ancien -> {
                    ancien.setActif(false);
                    ancien.setDepartementGere(null);
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
                .role(Role.ADMIN_DEPARTEMENT)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .actif(true)
                .doitChangerMotDePasse(true)
                .entreprise(entreprise)
                .departementGere(departement)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        try {
            emailService.envoyerCredentialsAdminDepartement(
                    request.getEmail(),
                    request.getPrenom(),
                    request.getNom(),
                    loginGenere,
                    mdpClair,
                    entreprise.getNom(),
                    departement.getNom()
            );
        } catch (Exception e) {
            log.error("Email non envoyé pour l'admin département {} : {}", loginGenere, e.getMessage());
        }

        return AdminDepartementInfoResponse.builder()
                .id(utilisateur.getId())
                .login(loginGenere)
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .actif(true)
                .motDePasseTemporaire(mdpClair)
                .build();
    }

    // ===== HELPERS =====

    private Entreprise getEntreprise(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getEntreprise() == null) {
            throw new IllegalStateException("Aucune entreprise associée à cet utilisateur");
        }

        return utilisateur.getEntreprise();
    }

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

    private DepartementResponse toResponse(Departement departement) {
        int nombreEmployes = employeRepository.findByDepartementId(departement.getId()).size();

        return DepartementResponse.builder()
                .id(departement.getId())
                .nom(departement.getNom())
                .description(departement.getDescription())
                .budget(departement.getBudget())
                .actif(departement.isActif())
                .nombreEmployes(nombreEmployes)
                .build();
    }
}