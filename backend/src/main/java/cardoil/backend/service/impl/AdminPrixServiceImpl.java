package cardoil.backend.service.impl;

import cardoil.backend.dto.request.PrixProduitRequestDTO;
import cardoil.backend.dto.request.PrixRequest;
import cardoil.backend.dto.response.PrixJourResponse;
import cardoil.backend.dto.response.PrixProduitDTO;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.PrixJour;
import cardoil.backend.entity.Produit;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.PrixJourRepository;
import cardoil.backend.repository.ProduitRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminPrixService;
import cardoil.backend.service.PrixProduitService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPrixServiceImpl implements AdminPrixService {

    private final ProduitRepository produitRepository;
    private final PrixJourRepository prixJourRepository;
    private final PrixProduitService prixProduitService; // 🆕 remplace l'accès direct à PrixProduitRepository
    private final UtilisateurRepository utilisateurRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    public PrixProduitDTO definirPrix(String login, Long produitId, PrixRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Produit produit = produitRepository.findByIdAndCompagnieId(produitId, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

        LocalDate dateDebut = request.getDateDebut() != null ? request.getDateDebut() : LocalDate.now();

        PrixProduitRequestDTO dtoPrixProduit = new PrixProduitRequestDTO();
        dtoPrixProduit.setPrixTtc(request.getPrixTtc());
        dtoPrixProduit.setPrixHtva(request.getPrixHtva());
        dtoPrixProduit.setPrixHtt(request.getPrixHtt());
        dtoPrixProduit.setDateDebut(dateDebut);
        dtoPrixProduit.setDateFin(request.getDateFin());

        PrixProduitDTO prixProduitCree = prixProduitService.creer(produitId, dtoPrixProduit);

        // 🆕 Si effectif aujourd'hui, on répercute immédiatement dans PrixJour pour un retour visuel
        // instantané — sans attendre le batch de 1h du matin. Contrairement au batch automatique
        // (qui ignore un PrixJour déjà présent), cette action admin PEUT corriger le prix du jour même :
        // l'immuabilité de PrixJour protège l'historique des jours PASSÉS, pas une correction same-day.
        if (prixProduitCree.isEnVigueurAujourdHui()) {
            PrixJour prixJour = prixJourRepository.findByProduitIdAndDatePrix(produitId, LocalDate.now())
                    .orElse(PrixJour.builder()
                            .produit(produit)
                            .datePrix(LocalDate.now())
                            .build());

            prixJour.setPrixTtc(request.getPrixTtc());
            prixJour.setPrixHtva(request.getPrixHtva());
            prixJour.setPrixHtt(request.getPrixHtt());
            prixJourRepository.save(prixJour);
        }

        return prixProduitCree;
    }

    @Override
    public List<PrixJourResponse> getHistorique(String login, Long produitId) {
        Compagnie compagnie = getCompagnie(login);

        produitRepository.findByIdAndCompagnieId(produitId, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

        return prixJourRepository.findByProduitIdOrderByDatePrixDesc(produitId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }

        return utilisateur.getCompagnie();
    }

    private PrixJourResponse toResponse(PrixJour prixJour) {
        return PrixJourResponse.builder()
                .id(prixJour.getId())
                .prixTtc(prixJour.getPrixTtc())
                .prixHtva(prixJour.getPrixHtva())
                .prixHtt(prixJour.getPrixHtt())
                .datePrix(prixJour.getDatePrix().format(FORMATTER))
                .build();
    }

    // 🆕 à ajouter dans AdminPrixServiceImpl, juste avant la méthode privée toResponse()
@Override
public List<PrixProduitDTO> getProgrammation(String login, Long produitId) {
    Compagnie compagnie = getCompagnie(login);

    produitRepository.findByIdAndCompagnieId(produitId, compagnie.getId())
            .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

    return prixProduitService.listerPourProduit(produitId);
}
}