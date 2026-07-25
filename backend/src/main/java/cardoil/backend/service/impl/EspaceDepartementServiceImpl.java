package cardoil.backend.service.impl;

import cardoil.backend.dto.request.CarteRequest;
import cardoil.backend.dto.request.EmployeDepartementRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.EmployeResponse;
import cardoil.backend.dto.response.EspaceDepartementInfoResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.DepartementRepository;
import cardoil.backend.repository.EmployeRepository;
import cardoil.backend.repository.RechargeRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.EmailService;
import cardoil.backend.service.EspaceDepartementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EspaceDepartementServiceImpl implements EspaceDepartementService {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;
    private final CarteRepository carteRepository;
    private final RechargeRepository rechargeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String CHARS_MDP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHARS_NUMERO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final BigDecimal MONTANT_MINIMUM = BigDecimal.ONE;

    private static final Map<StatutCarte, Set<StatutCarte>> TRANSITIONS_AUTORISEES = new EnumMap<>(StatutCarte.class);
    static {
        TRANSITIONS_AUTORISEES.put(StatutCarte.ACTIVE, EnumSet.of(StatutCarte.SUSPENDUE, StatutCarte.BLOQUEE, StatutCarte.EXPIREE));
        TRANSITIONS_AUTORISEES.put(StatutCarte.SUSPENDUE, EnumSet.of(StatutCarte.ACTIVE, StatutCarte.BLOQUEE));
        TRANSITIONS_AUTORISEES.put(StatutCarte.BLOQUEE, EnumSet.of(StatutCarte.ACTIVE, StatutCarte.EXPIREE));
        TRANSITIONS_AUTORISEES.put(StatutCarte.EXPIREE, EnumSet.noneOf(StatutCarte.class));
    }

    @Override
    public EspaceDepartementInfoResponse getInfo(String login) {
        Departement departement = getDepartement(login);
        List<Employe> employes = employeRepository.findByDepartementId(departement.getId());

        int actives = 0, suspendues = 0, bloquees = 0, expirees = 0;
        BigDecimal soldeTotal = BigDecimal.ZERO;

        for (Employe employe : employes) {
            var optCarte = carteRepository.findByEmployeId(employe.getId());
            if (optCarte.isEmpty()) continue;
            Carte carte = optCarte.get();
            switch (carte.getStatut()) {
                case ACTIVE -> { actives++; soldeTotal = soldeTotal.add(carte.getSolde()); }
                case SUSPENDUE -> suspendues++;
                case BLOQUEE -> bloquees++;
                case EXPIREE -> expirees++;
            }
        }

        return EspaceDepartementInfoResponse.builder()
                .departementId(departement.getId())
                .departementNom(departement.getNom())
                .entrepriseNom(departement.getEntreprise().getNom())
                .budgetDisponible(departement.getBudget() != null ? departement.getBudget() : BigDecimal.ZERO)
                .nombreEmployes(employes.size())
                .cartesActives(actives)
                .cartesSuspendues(suspendues)
                .cartesBloquees(bloquees)
                .cartesExpirees(expirees)
                .soldeTotalCartes(soldeTotal)
                .build();
    }

    @Override
    public List<EmployeResponse> getEmployes(String login) {
        Departement departement = getDepartement(login);
        return employeRepository.findByDepartementId(departement.getId()).stream()
                .map(this::toEmployeResponse)
                .toList();
    }

    @Override
    public EmployeResponse createEmploye(String login, EmployeDepartementRequest request) {
        Departement departement = getDepartement(login);
        Entreprise entreprise = departement.getEntreprise();

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
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
                    request.getEmail(), request.getPrenom(), request.getNom(),
                    loginGenere, mdpClair, entreprise.getNom(), departement.getNom());
        } catch (Exception e) {
            log.error("Email non envoyé pour l'employé {} : {}", loginGenere, e.getMessage());
        }

        EmployeResponse response = toEmployeResponse(employe);
        response.setMotDePasseTemporaire(mdpClair);
        return response;
    }

    @Override
    public EmployeResponse updateEmploye(String login, Long employeId, EmployeDepartementRequest request) {
        Departement departement = getDepartement(login);

        Employe employe = employeRepository.findById(employeId)
                .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(departement.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé dans ce département"));

        employe.setNom(request.getNom());
        employe.setPrenom(request.getPrenom());
        employe.setEmail(request.getEmail());

        employe = employeRepository.save(employe);
        return toEmployeResponse(employe);
    }

    @Override
    public void deleteEmploye(String login, Long employeId) {
        Departement departement = getDepartement(login);

        Employe employe = employeRepository.findById(employeId)
                .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(departement.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé dans ce département"));

        employeRepository.delete(employe);
    }

    @Override
    public List<CarteResponse> getCartes(String login) {
        Departement departement = getDepartement(login);
        return employeRepository.findByDepartementId(departement.getId()).stream()
                .map(employe -> carteRepository.findByEmployeId(employe.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toCarteResponse)
                .toList();
    }

    @Override
    public CarteResponse createCarte(String login, CarteRequest request) {
        Departement departement = getDepartement(login);

        Employe employe = employeRepository.findById(request.getEmployeId())
                .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(departement.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé dans ce département"));

        if (carteRepository.existsByEmployeId(employe.getId())) {
            throw new IllegalArgumentException("Cet employé possède déjà une carte");
        }

        boolean estDotation = request.getTypeCarte() == TypeCarteEmploye.DOTATION_PLAFONNEE
                || request.getTypeCarte() == TypeCarteEmploye.DOTATION_AVEC_REPORT;

        if (estDotation) {
            if (request.getMontantDotationMensuelle() == null) {
                throw new IllegalArgumentException("Le montant de dotation mensuelle est obligatoire pour ce type de carte");
            }
            if (request.getDateRenouvellement() == null) {
                throw new IllegalArgumentException("La date de renouvellement est obligatoire pour ce type de carte");
            }
        }

        Carte carte = Carte.builder()
                .numeroCarte(genererNumeroCarteUnique())
                .employe(employe)
                .typeCarte(request.getTypeCarte())
                .solde(BigDecimal.ZERO)
                .statut(StatutCarte.ACTIVE)
                .dateExpiration(request.getDateExpiration())
                .montantDotationMensuelle(estDotation ? request.getMontantDotationMensuelle() : null)
                .dateRenouvellement(estDotation ? request.getDateRenouvellement() : null)
                .plafondCumuleMax(request.getTypeCarte() == TypeCarteEmploye.DOTATION_AVEC_REPORT ? request.getPlafondCumuleMax() : null)
                .build();

        return toCarteResponse(carteRepository.save(carte));
    }

    @Override
    public CarteResponse changerStatutCarte(String login, Long carteId, StatutCarte nouveauStatut) {
        Carte carte = getCarteDuDepartement(login, carteId);

        Set<StatutCarte> transitionsPossibles = TRANSITIONS_AUTORISEES.getOrDefault(carte.getStatut(), EnumSet.noneOf(StatutCarte.class));
        if (!transitionsPossibles.contains(nouveauStatut)) {
            throw new IllegalStateException("Transition non autorisée : " + carte.getStatut() + " -> " + nouveauStatut);
        }

        carte.setStatut(nouveauStatut);
        return toCarteResponse(carteRepository.save(carte));
    }

    @Override
    public RechargeResponse rechargerCarte(String login, Long carteId, RechargeRequest request) {
        Utilisateur admin = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        Departement departement = getDepartement(login);
        Carte carte = getCarteDuDepartement(login, carteId);

        if (carte.getStatut() != StatutCarte.ACTIVE) {
            throw new IllegalStateException("Carte " + carte.getStatut() + " : recharge impossible");
        }

        BigDecimal budgetActuel = departement.getBudget() != null ? departement.getBudget() : BigDecimal.ZERO;
        if (budgetActuel.compareTo(request.getMontant()) < 0) {
            throw new IllegalArgumentException(
                    "Budget du département insuffisant : disponible " + budgetActuel
                            + " FCFA, demandé " + request.getMontant() + " FCFA");
        }
        departement.setBudget(budgetActuel.subtract(request.getMontant()));
        departementRepository.save(departement);

        carte.setSolde(carte.getSolde().add(request.getMontant()));
        carteRepository.save(carte);

        Recharge recharge = Recharge.builder()
                .carte(carte)
                .montant(request.getMontant())
                .effectuePar(admin)
                .type(TypeRecharge.MANUELLE)
                .build();

        return toRechargeResponse(rechargeRepository.save(recharge));
    }

    @Override
    public List<RechargeResponse> getHistoriqueRecharges(String login, Long carteId) {
        getCarteDuDepartement(login, carteId);
        return rechargeRepository.findByCarteIdOrderByDateRechargeDesc(carteId).stream()
                .map(this::toRechargeResponse)
                .toList();
    }

    // ===== HELPERS =====

    private Departement getDepartement(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getDepartementGere() == null) {
            throw new IllegalStateException("Aucun département géré associé à cet utilisateur");
        }

        return utilisateur.getDepartementGere();
    }

    private Carte getCarteDuDepartement(String login, Long carteId) {
        Departement departement = getDepartement(login);
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new EntityNotFoundException("Carte non trouvée"));

        if (carte.getEmploye().getDepartement() == null
                || !carte.getEmploye().getDepartement().getId().equals(departement.getId())) {
            throw new EntityNotFoundException("Carte non trouvée");
        }
        return carte;
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

    private String genererNumeroCarteUnique() {
        String numero;
        do {
            StringBuilder sb = new StringBuilder("CARD-");
            for (int i = 0; i < 10; i++) {
                sb.append(CHARS_NUMERO.charAt(random.nextInt(CHARS_NUMERO.length())));
            }
            numero = sb.toString();
        } while (carteRepository.existsByNumeroCarte(numero));
        return numero;
    }

    private EmployeResponse toEmployeResponse(Employe employe) {
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

    private CarteResponse toCarteResponse(Carte carte) {
        Departement departement = carte.getEmploye().getDepartement();
        return CarteResponse.builder()
                .id(carte.getId())
                .numeroCarte(carte.getNumeroCarte())
                .employeId(carte.getEmploye().getId())
                .employeNomComplet(carte.getEmploye().getPrenom() + " " + carte.getEmploye().getNom())
                .matricule(carte.getEmploye().getMatricule())
                .typeCarte(carte.getTypeCarte())
                .solde(carte.getSolde())
                .statut(carte.getStatut())
                .dateCreation(carte.getDateCreation())
                .dateExpiration(carte.getDateExpiration())
                .montantDotationMensuelle(carte.getMontantDotationMensuelle())
                .dateRenouvellement(carte.getDateRenouvellement())
                .plafondCumuleMax(carte.getPlafondCumuleMax())
                .sourceFinancement(departement != null ? "Département : " + departement.getNom() : "Entreprise (direct)")
                .build();
    }

    private RechargeResponse toRechargeResponse(Recharge recharge) {
        return RechargeResponse.builder()
                .id(recharge.getId())
                .numeroCarte(recharge.getCarte().getNumeroCarte())
                .montant(recharge.getMontant())
                .dateRecharge(recharge.getDateRecharge())
                .effectuePar(recharge.getEffectuePar() != null
                        ? recharge.getEffectuePar().getPrenom() + " " + recharge.getEffectuePar().getNom()
                        : "Système (dotation automatique)")
                .type(recharge.getType())
                .build();
    }
}