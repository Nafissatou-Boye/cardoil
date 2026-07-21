package cardoil.backend.service.impl;

import cardoil.backend.dto.request.CarteRequest;
import cardoil.backend.dto.request.LigneRechargeRequest;
import cardoil.backend.dto.request.RechargeGroupeeRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.RechargeGroupeeResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.DepartementRepository;
import cardoil.backend.repository.EmployeRepository;
import cardoil.backend.repository.EntrepriseRepository;
import cardoil.backend.repository.RechargeGroupeeRepository;
import cardoil.backend.repository.RechargeRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminCarteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminCarteServiceImpl implements AdminCarteService {

    private final CarteRepository carteRepository;
    private final EmployeRepository employeRepository;
    private final RechargeRepository rechargeRepository;
    private final RechargeGroupeeRepository rechargeGroupeeRepository;
    private final DepartementRepository departementRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS_NUMERO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final BigDecimal MONTANT_MINIMUM = BigDecimal.ONE;

    private static final Map<StatutCarte, Set<StatutCarte>> TRANSITIONS_AUTORISEES = new EnumMap<>(StatutCarte.class);
    static {
        TRANSITIONS_AUTORISEES.put(StatutCarte.ACTIVE, EnumSet.of(StatutCarte.SUSPENDUE, StatutCarte.BLOQUEE, StatutCarte.EXPIREE));
        TRANSITIONS_AUTORISEES.put(StatutCarte.SUSPENDUE, EnumSet.of(StatutCarte.ACTIVE, StatutCarte.BLOQUEE));
        TRANSITIONS_AUTORISEES.put(StatutCarte.BLOQUEE, EnumSet.of(StatutCarte.ACTIVE, StatutCarte.EXPIREE));
        TRANSITIONS_AUTORISEES.put(StatutCarte.EXPIREE, EnumSet.noneOf(StatutCarte.class));
    }

    @Override
    public List<CarteResponse> getAll(String login) {
        Entreprise entreprise = getEntreprise(login);
        return carteRepository.findByEmploye_Entreprise_Id(entreprise.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CarteResponse create(String login, CarteRequest request) {
        Entreprise entreprise = getEntreprise(login);

        Employe employe = employeRepository.findById(request.getEmployeId())
                .filter(e -> e.getEntreprise().getId().equals(entreprise.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé pour cette entreprise"));

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

        return toResponse(carteRepository.save(carte));
    }

    @Override
    public CarteResponse changerStatut(String login, Long carteId, StatutCarte nouveauStatut) {
        Carte carte = getCarteDeLEntreprise(login, carteId);

        Set<StatutCarte> transitionsPossibles = TRANSITIONS_AUTORISEES.getOrDefault(carte.getStatut(), EnumSet.noneOf(StatutCarte.class));
        if (!transitionsPossibles.contains(nouveauStatut)) {
            throw new IllegalStateException(
                    "Transition non autorisée : " + carte.getStatut() + " -> " + nouveauStatut);
        }

        carte.setStatut(nouveauStatut);
        return toResponse(carteRepository.save(carte));
    }

    @Override
    public RechargeResponse recharger(String login, Long carteId, RechargeRequest request) {
        Utilisateur admin = getUtilisateur(login);
        Carte carte = getCarteDeLEntreprise(login, carteId);

        if (carte.getStatut() != StatutCarte.ACTIVE) {
            throw new IllegalStateException("Carte " + carte.getStatut() + " : recharge impossible");
        }

        // Débite la source de financement (département ou entreprise) avant de créditer la carte
        deduireSourceFinancement(carte.getEmploye(), request.getMontant());

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
        getCarteDeLEntreprise(login, carteId);
        return rechargeRepository.findByCarteIdOrderByDateRechargeDesc(carteId).stream()
                .map(this::toRechargeResponse)
                .toList();
    }

    @Override
    public CarteResponse renouveler(String login, Long carteId) {
        Carte carte = getCarteDeLEntreprise(login, carteId);
        appliquerRenouvellement(carte);
        return toResponse(carteRepository.save(carte));
    }

    @Override
    public RechargeGroupeeResponse rechargerGroupe(String login, RechargeGroupeeRequest request) {
        Utilisateur admin = getUtilisateur(login);
        Entreprise entreprise = getEntreprise(login);

        int reussies = 0;
        int echecs = 0;
        BigDecimal montantTotal = BigDecimal.ZERO;
        List<String> erreurs = new ArrayList<>();

        RechargeGroupee batch = RechargeGroupee.builder()
                .nomFichier(request.getNomFichier())
                .effectuePar(admin)
                .entreprise(entreprise)
                .build();
        batch = rechargeGroupeeRepository.save(batch);

        for (LigneRechargeRequest ligne : request.getLignes()) {
            String numero = ligne.getNumeroCarte() != null ? ligne.getNumeroCarte().trim() : "";

            if (numero.isBlank()) {
                echecs++;
                erreurs.add("(ligne vide) : numéro de carte manquant");
                continue;
            }

            if (ligne.getMontant() == null || ligne.getMontant().compareTo(MONTANT_MINIMUM) < 0) {
                echecs++;
                erreurs.add(numero + " : montant invalide (minimum " + MONTANT_MINIMUM + " FCFA)");
                continue;
            }

            Optional<Carte> optCarte = carteRepository
                    .findByNumeroCarteIgnoreCaseAndEmploye_Entreprise_Id(numero, entreprise.getId());

            if (optCarte.isEmpty()) {
                echecs++;
                erreurs.add(numero + " : carte introuvable pour cette entreprise");
                continue;
            }

            Carte carte = optCarte.get();
            if (carte.getStatut() != StatutCarte.ACTIVE) {
                echecs++;
                erreurs.add(numero + " : carte " + carte.getStatut() + ", recharge impossible");
                continue;
            }

            try {
                deduireSourceFinancement(carte.getEmploye(), ligne.getMontant());
            } catch (IllegalArgumentException ex) {
                echecs++;
                erreurs.add(numero + " : " + ex.getMessage());
                continue;
            }

            carte.setSolde(carte.getSolde().add(ligne.getMontant()));
            carteRepository.save(carte);

            Recharge recharge = Recharge.builder()
                    .carte(carte)
                    .montant(ligne.getMontant())
                    .effectuePar(admin)
                    .type(TypeRecharge.GROUPEE)
                    .rechargeGroupee(batch)
                    .build();
            rechargeRepository.save(recharge);

            reussies++;
            montantTotal = montantTotal.add(ligne.getMontant());
        }

        batch.setNombreReussies(reussies);
        batch.setNombreEchecs(echecs);
        batch.setMontantTotal(montantTotal);
        batch.setDetailsErreurs(erreurs.isEmpty() ? null : String.join(" ; ", erreurs));
        batch = rechargeGroupeeRepository.save(batch);

        return toRechargeGroupeeResponse(batch);
    }

    @Override
    public List<RechargeGroupeeResponse> getHistoriqueRechargesGroupees(String login) {
        Entreprise entreprise = getEntreprise(login);
        return rechargeGroupeeRepository.findByEntrepriseIdOrderByDateExecutionDesc(entreprise.getId()).stream()
                .map(this::toRechargeGroupeeResponse)
                .toList();
    }

    // Règle métier du renouvellement mensuel (section 5.1 du cahier des charges)
    public void appliquerRenouvellement(Carte carte) {
        if (carte.getMontantDotationMensuelle() == null) {
            return; // RECHARGEABLE_LIBRE : aucun traitement automatique
        }

        // Débite la source de financement du montant de la dotation avant de créditer la carte
        deduireSourceFinancement(carte.getEmploye(), carte.getMontantDotationMensuelle());

        if (carte.getTypeCarte() == TypeCarteEmploye.DOTATION_PLAFONNEE) {
            carte.setSolde(carte.getMontantDotationMensuelle());
        } else if (carte.getTypeCarte() == TypeCarteEmploye.DOTATION_AVEC_REPORT) {
            BigDecimal nouveauSolde = carte.getSolde().add(carte.getMontantDotationMensuelle());
            if (carte.getPlafondCumuleMax() != null && nouveauSolde.compareTo(carte.getPlafondCumuleMax()) > 0) {
                nouveauSolde = carte.getPlafondCumuleMax();
            }
            carte.setSolde(nouveauSolde);
        }

        carte.setDerniereDateRenouvellement(LocalDate.now());

        Recharge recharge = Recharge.builder()
                .carte(carte)
                .montant(carte.getMontantDotationMensuelle())
                .type(TypeRecharge.DOTATION)
                .build();
        rechargeRepository.save(recharge);
    }

    // ===== HELPERS =====

    /**
     * Débite le montant de la source de financement de l'employé :
     * - s'il est rattaché à un département, débite le budget de ce département
     * - sinon, débite directement le solde disponible de l'entreprise
     * Lève IllegalArgumentException si la source n'a pas assez de fonds.
     */
    private void deduireSourceFinancement(Employe employe, BigDecimal montant) {
        Departement departement = employe.getDepartement();

        if (departement != null) {
            BigDecimal budgetActuel = departement.getBudget() != null ? departement.getBudget() : BigDecimal.ZERO;
            if (budgetActuel.compareTo(montant) < 0) {
                throw new IllegalArgumentException(
                        "Budget du département \"" + departement.getNom() + "\" insuffisant : disponible "
                                + budgetActuel + " FCFA, demandé " + montant + " FCFA");
            }
            departement.setBudget(budgetActuel.subtract(montant));
            departementRepository.save(departement);
        } else {
            Entreprise entreprise = employe.getEntreprise();
            BigDecimal soldeActuel = entreprise.getSoldeDisponible() != null ? entreprise.getSoldeDisponible() : BigDecimal.ZERO;
            if (soldeActuel.compareTo(montant) < 0) {
                throw new IllegalArgumentException(
                        "Solde entreprise insuffisant : disponible " + soldeActuel
                                + " FCFA, demandé " + montant + " FCFA");
            }
            entreprise.setSoldeDisponible(soldeActuel.subtract(montant));
            entrepriseRepository.save(entreprise);
        }
    }

    private Entreprise getEntreprise(String login) {
        Utilisateur utilisateur = getUtilisateur(login);
        if (utilisateur.getEntreprise() == null) {
            throw new IllegalStateException("Aucune entreprise associée à cet utilisateur");
        }
        return utilisateur.getEntreprise();
    }

    private Utilisateur getUtilisateur(String login) {
        return utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
    }

    private Carte getCarteDeLEntreprise(String login, Long carteId) {
        Entreprise entreprise = getEntreprise(login);
        Carte carte = carteRepository.findById(carteId)
                .orElseThrow(() -> new EntityNotFoundException("Carte non trouvée"));
        if (!carte.getEmploye().getEntreprise().getId().equals(entreprise.getId())) {
            throw new EntityNotFoundException("Carte non trouvée");
        }
        return carte;
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

    private CarteResponse toResponse(Carte carte) {
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
                .sourceFinancement(departement != null
                        ? "Département : " + departement.getNom()
                        : "Entreprise (direct)")
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

    private RechargeGroupeeResponse toRechargeGroupeeResponse(RechargeGroupee batch) {
        return RechargeGroupeeResponse.builder()
                .id(batch.getId())
                .nomFichier(batch.getNomFichier())
                .dateExecution(batch.getDateExecution())
                .effectuePar(batch.getEffectuePar() != null
                        ? batch.getEffectuePar().getPrenom() + " " + batch.getEffectuePar().getNom()
                        : "Inconnu")
                .nombreReussies(batch.getNombreReussies())
                .nombreEchecs(batch.getNombreEchecs())
                .montantTotal(batch.getMontantTotal())
                .detailsErreurs(batch.getDetailsErreurs())
                .build();
    }
}