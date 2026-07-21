package cardoil.backend.service.impl;

import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.dto.response.UtilisateurDetailResponse;
import cardoil.backend.dto.response.UtilisateurListItemResponse;
import cardoil.backend.entity.Carte;
import cardoil.backend.entity.Departement;
import cardoil.backend.entity.Employe;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Recharge;
import cardoil.backend.entity.Role;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.EmployeRepository;
import cardoil.backend.repository.RechargeRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminUtilisateurService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUtilisateurServiceImpl implements AdminUtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final CarteRepository carteRepository;
    private final RechargeRepository rechargeRepository;
    private final EmployeRepository employeRepository;

    private static final List<Role> ROLES_LISTABLES = List.of(Role.EMPLOYE, Role.ADMIN_DEPARTEMENT);

    @Override
    public List<UtilisateurListItemResponse> getAll(String login) {
        Entreprise entreprise = getEntreprise(login);

        return utilisateurRepository.findByEntrepriseIdAndRoleIn(entreprise.getId(), ROLES_LISTABLES).stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public UtilisateurDetailResponse getDetail(String login, Long id) {
        Entreprise entreprise = getEntreprise(login);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .filter(u -> u.getEntreprise() != null && u.getEntreprise().getId().equals(entreprise.getId()))
                .filter(u -> ROLES_LISTABLES.contains(u.getRole()))
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        UtilisateurDetailResponse.UtilisateurDetailResponseBuilder builder = UtilisateurDetailResponse.builder()
                .id(utilisateur.getId())
                .login(utilisateur.getLogin())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .actif(utilisateur.isActif())
                .dateCreation(utilisateur.getDateCreation())
                .derniereConnexion(utilisateur.getDerniereConnexion());

        if (utilisateur instanceof Employe employe) {
            builder.matricule(employe.getMatricule());
            builder.departementNom(employe.getDepartement() != null ? employe.getDepartement().getNom() : null);

            carteRepository.findByEmployeId(employe.getId()).ifPresent(carte -> {
                builder.carte(toCarteResponse(carte));
                List<RechargeResponse> historique = rechargeRepository
                        .findByCarteIdOrderByDateRechargeDesc(carte.getId()).stream()
                        .map(this::toRechargeResponse)
                        .toList();
                builder.historiqueRecharges(historique);
            });
        }

        if (utilisateur.getRole() == Role.ADMIN_DEPARTEMENT && utilisateur.getDepartementGere() != null) {
            Departement dep = utilisateur.getDepartementGere();
            builder.departementGereNom(dep.getNom());
            builder.nombreEmployesDepartementGere(employeRepository.findByDepartementId(dep.getId()).size());
        }

        return builder.build();
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

    private UtilisateurListItemResponse toListItem(Utilisateur utilisateur) {
        String departementNom = null;
        String matricule = null;
        boolean possedeUneCarte = false;

        if (utilisateur instanceof Employe employe) {
            matricule = employe.getMatricule();
            departementNom = employe.getDepartement() != null ? employe.getDepartement().getNom() : null;
            possedeUneCarte = carteRepository.existsByEmployeId(employe.getId());
        } else if (utilisateur.getRole() == Role.ADMIN_DEPARTEMENT && utilisateur.getDepartementGere() != null) {
            departementNom = utilisateur.getDepartementGere().getNom();
        }

        return UtilisateurListItemResponse.builder()
                .id(utilisateur.getId())
                .login(utilisateur.getLogin())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .actif(utilisateur.isActif())
                .departementNom(departementNom)
                .matricule(matricule)
                .possedeUneCarte(possedeUneCarte)
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