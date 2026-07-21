package cardoil.backend.service.impl;

import cardoil.backend.dto.response.RapportDepartementResponse;
import cardoil.backend.dto.response.RapportEmployeResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.dto.response.SuiviBudgetResponse;
import cardoil.backend.entity.Carte;
import cardoil.backend.entity.Employe;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Recharge;
import cardoil.backend.entity.StatutCarte;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.DepartementRepository;
import cardoil.backend.repository.EmployeRepository;
import cardoil.backend.repository.RechargeRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminRapportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminRapportServiceImpl implements AdminRapportService {

    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;
    private final CarteRepository carteRepository;
    private final RechargeRepository rechargeRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<RapportDepartementResponse> getRapportDepartements(String login) {
        Entreprise entreprise = getEntreprise(login);

        return departementRepository.findByEntrepriseId(entreprise.getId()).stream()
                .map(dep -> {
                    List<Employe> employes = employeRepository.findByDepartementId(dep.getId());
                    BigDecimal soldeCumule = employes.stream()
                            .map(emp -> carteRepository.findByEmployeId(emp.getId()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(c -> c.getStatut() == StatutCarte.ACTIVE)
                            .map(Carte::getSolde)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return RapportDepartementResponse.builder()
                            .departementId(dep.getId())
                            .departementNom(dep.getNom())
                            .budgetDisponible(dep.getBudget() != null ? dep.getBudget() : BigDecimal.ZERO)
                            .nombreEmployes(employes.size())
                            .soldeCumuleCartes(soldeCumule)
                            .build();
                })
                .toList();
    }

    @Override
    public List<RapportEmployeResponse> getRapportEmployes(String login) {
        Entreprise entreprise = getEntreprise(login);

        return employeRepository.findByEntrepriseId(entreprise.getId()).stream()
                .map(employe -> {
                    Optional<Carte> optCarte = carteRepository.findByEmployeId(employe.getId());

                    BigDecimal totalCredite = optCarte
                            .map(c -> rechargeRepository.findByCarteIdOrderByDateRechargeDesc(c.getId()).stream()
                                    .map(Recharge::getMontant)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .orElse(BigDecimal.ZERO);

                    return RapportEmployeResponse.builder()
                            .employeId(employe.getId())
                            .nomComplet(employe.getPrenom() + " " + employe.getNom())
                            .matricule(employe.getMatricule())
                            .departementNom(employe.getDepartement() != null ? employe.getDepartement().getNom() : "Direct")
                            .numeroCarte(optCarte.map(Carte::getNumeroCarte).orElse(null))
                            .typeCarte(optCarte.map(Carte::getTypeCarte).orElse(null))
                            .statutCarte(optCarte.map(Carte::getStatut).orElse(null))
                            .soldeActuel(optCarte.map(Carte::getSolde).orElse(BigDecimal.ZERO))
                            .totalCredite(totalCredite)
                            .build();
                })
                .toList();
    }

    @Override
    public SuiviBudgetResponse getSuiviBudget(String login) {
        Entreprise entreprise = getEntreprise(login);

        BigDecimal totalBudgetDepartements = departementRepository.findByEntrepriseId(entreprise.getId()).stream()
                .map(dep -> dep.getBudget() != null ? dep.getBudget() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSoldeCartes = carteRepository.findByEmploye_Entreprise_Id(entreprise.getId()).stream()
                .filter(c -> c.getStatut() == StatutCarte.ACTIVE)
                .map(Carte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SuiviBudgetResponse.builder()
                .soldeDisponibleEntreprise(entreprise.getSoldeDisponible())
                .totalBudgetAlloueDepartements(totalBudgetDepartements)
                .totalSoldeCartesActives(totalSoldeCartes)
                .build();
    }

    @Override
    public List<RechargeResponse> getHistoriqueGlobal(String login) {
        Entreprise entreprise = getEntreprise(login);

        return rechargeRepository.findByCarte_Employe_Entreprise_IdOrderByDateRechargeDesc(entreprise.getId()).stream()
                .map(this::toRechargeResponse)
                .toList();
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