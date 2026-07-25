package cardoil.backend.service.impl;

import cardoil.backend.dto.request.EmployeRequest;
import cardoil.backend.dto.response.EmployeResponse;
import cardoil.backend.entity.Departement;
import cardoil.backend.entity.Employe;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Role;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.DepartementRepository;
import cardoil.backend.repository.EmployeRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminEmployeService;
import cardoil.backend.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEmployeServiceImpl implements AdminEmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CarteRepository carteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String CHARS_MDP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public List<EmployeResponse> getAll(String login) {
        Entreprise entreprise = getEntreprise(login);
        return employeRepository.findByEntrepriseId(entreprise.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EmployeResponse create(String login, EmployeRequest request) {
        Entreprise entreprise = getEntreprise(login);

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        Departement departement = null;
        if (request.getDepartementId() != null) {
            departement = departementRepository
                    .findByIdAndEntrepriseId(request.getDepartementId(), entreprise.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Département non trouvé pour cette entreprise"));
        }

        String matricule = (request.getMatricule() != null && !request.getMatricule().isBlank())
                ? request.getMatricule()
                : genererMatriculeUnique();

        if (employeRepository.existsByMatricule(matricule)) {
            throw new IllegalArgumentException("Ce matricule est déjà utilisé");
        }

        String loginGenere = genererLoginUnique();
        String mdpClair = genererMotDePasse();

        Employe employe = Employe.builder()
                .login(loginGenere)
                .motDePasse(passwordEncoder.encode(mdpClair))
                .role(Role.EMPLOYE)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .actif(true)
                .doitChangerMotDePasse(true)
                .matricule(matricule)
                .departement(departement)
                .entreprise(entreprise)
                .build();

        employe = employeRepository.save(employe);

        try {
            emailService.envoyerCredentialsEmploye(
                    request.getEmail(),
                    request.getPrenom(),
                    request.getNom(),
                    loginGenere,
                    mdpClair,
                    entreprise.getNom(),
                    departement != null ? departement.getNom() : null
            );
        } catch (Exception e) {
            log.error("Email non envoyé pour l'employé {} : {}", loginGenere, e.getMessage());
        }

        EmployeResponse response = toResponse(employe);
        response.setMotDePasseTemporaire(mdpClair);
        return response;
    }

    @Override
    public EmployeResponse update(String login, Long id, EmployeRequest request) {
        Entreprise entreprise = getEntreprise(login);

        Employe employe = employeRepository.findById(id)
                .filter(e -> e.getEntreprise().getId().equals(entreprise.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé"));

        Departement departement = null;
        if (request.getDepartementId() != null) {
            departement = departementRepository
                    .findByIdAndEntrepriseId(request.getDepartementId(), entreprise.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Département non trouvé pour cette entreprise"));
        }

        employe.setNom(request.getNom());
        employe.setPrenom(request.getPrenom());
        employe.setEmail(request.getEmail());
        employe.setDepartement(departement);

        employe = employeRepository.save(employe);
        return toResponse(employe);
    }

    @Override
    public void delete(String login, Long id) {
        Entreprise entreprise = getEntreprise(login);

        Employe employe = employeRepository.findById(id)
                .filter(e -> e.getEntreprise().getId().equals(entreprise.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé"));

        employeRepository.delete(employe);
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

    private String genererMatriculeUnique() {
        String matricule;
        do {
            matricule = "EMP-" + String.format("%07d", random.nextInt(10_000_000));
        } while (employeRepository.existsByMatricule(matricule));
        return matricule;
    }

    private String genererMotDePasse() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS_MDP.charAt(random.nextInt(CHARS_MDP.length())));
        }
        return sb.toString();
    }

    private EmployeResponse toResponse(Employe employe) {
        return EmployeResponse.builder()
                .id(employe.getId())
                .login(employe.getLogin())
                .matricule(employe.getMatricule())
                .nom(employe.getNom())
                .prenom(employe.getPrenom())
                .email(employe.getEmail())
                .telephone(employe.getTelephone())
                .actif(employe.isActif())
                .dateCreation(employe.getDateCreation())
                .departementId(employe.getDepartement() != null ? employe.getDepartement().getId() : null)
                .departementNom(employe.getDepartement() != null ? employe.getDepartement().getNom() : null)
                .entrepriseId(employe.getEntreprise().getId())
                .entrepriseNom(employe.getEntreprise().getNom())
                .possedeUneCarte(carteRepository.existsByEmployeId(employe.getId()))
                .build();
    }
}