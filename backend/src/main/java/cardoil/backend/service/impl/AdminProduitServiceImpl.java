package cardoil.backend.service.impl;

import cardoil.backend.dto.request.ProduitRequest;
import cardoil.backend.dto.response.ProduitResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.PrixJourRepository;
import cardoil.backend.repository.PrixProduitRepository;
import cardoil.backend.repository.ProduitRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminProduitService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminProduitServiceImpl implements AdminProduitService {

    private final ProduitRepository produitRepository;
    private final PrixJourRepository prixJourRepository;
    private final PrixProduitRepository prixProduitRepository; // 🆕
    private final UtilisateurRepository utilisateurRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public List<ProduitResponse> getAll(String login) {
        Compagnie compagnie = getCompagnie(login);
        return produitRepository.findByCompagnieId(compagnie.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProduitResponse create(String login, ProduitRequest request) {
        Compagnie compagnie = getCompagnie(login);
        validerCommission(request.getType(), request.getCommissionFixe(), request.getCommissionPourcentage());

        Produit produit = Produit.builder()
                .nom(request.getNom())
                .type(request.getType())
                .statut(StatutProduit.BROUILLON)
                .description(request.getDescription())
                .obligatoire(request.isObligatoire())
                .categorie(request.getCategorie())
                .unite(request.getUnite())
                .commissionFixe(request.getCommissionFixe())
                .commissionPourcentage(request.getCommissionPourcentage())
                .compagnie(compagnie)
                .build();

        return toResponse(produitRepository.save(produit));
    }

    @Override
    public ProduitResponse update(String login, Long id, ProduitRequest request) {
        Compagnie compagnie = getCompagnie(login);
        validerCommission(request.getType(), request.getCommissionFixe(), request.getCommissionPourcentage());

        Produit produit = produitRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

        produit.setNom(request.getNom());
        produit.setType(request.getType());
        produit.setDescription(request.getDescription());
        produit.setObligatoire(request.isObligatoire());
        produit.setCategorie(request.getCategorie());
        produit.setUnite(request.getUnite());
        produit.setCommissionFixe(request.getCommissionFixe());
        produit.setCommissionPourcentage(request.getCommissionPourcentage());

        return toResponse(produitRepository.save(produit));
    }

    @Override
    public ProduitResponse changerStatut(String login, Long id, String nouveauStatutStr) {
        Compagnie compagnie = getCompagnie(login);

        Produit produit = produitRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

        StatutProduit nouveauStatut;
        try {
            nouveauStatut = StatutProduit.valueOf(nouveauStatutStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide");
        }

        if (produit.getStatut() == StatutProduit.ARCHIVE) {
            throw new IllegalStateException("Un produit archivé ne peut plus changer de statut");
        }

        if (produit.isObligatoire() &&
                (nouveauStatut == StatutProduit.INACTIF || nouveauStatut == StatutProduit.ARCHIVE)) {
            throw new IllegalStateException("Un produit obligatoire ne peut pas être désactivé ou archivé");
        }

        produit.setStatut(nouveauStatut);
        return toResponse(produitRepository.save(produit));
    }

    @Override
    @Transactional // 🆕 nécessaire vu que delete() touche maintenant 3 tables — soit tout passe, soit rien
    public void delete(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);

        Produit produit = produitRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

        if (produit.getStatut() != StatutProduit.BROUILLON) {
            throw new IllegalStateException("Seul un produit en brouillon peut être supprimé. Archivez-le sinon.");
        }

        // 🆕 Un produit encore en BROUILLON n'a jamais été activé/vendable — tout prix associé
        // n'est que du paramétrage, pas une donnée métier à préserver. On nettoie avant de supprimer
        // le produit, pour éviter la violation de contrainte de clé étrangère.
        prixJourRepository.deleteAll(prixJourRepository.findByProduitIdOrderByDatePrixDesc(id));
        prixProduitRepository.deleteAll(prixProduitRepository.findByProduitIdOrderByDateDebutDesc(id));

        produitRepository.delete(produit);
    }

    private void validerCommission(TypeProduit type, BigDecimal commissionFixe, BigDecimal commissionPourcentage) {
        if (type == TypeProduit.LIQUIDE && commissionPourcentage != null) {
            throw new IllegalArgumentException(
                    "Un produit liquide utilise une commission fixe par litre, pas un pourcentage.");
        }
        if (type == TypeProduit.NON_LIQUIDE && commissionFixe != null) {
            throw new IllegalArgumentException(
                    "Un produit non liquide utilise une commission en pourcentage, pas un montant fixe.");
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

    private ProduitResponse toResponse(Produit produit) {
        Optional<PrixJour> dernierPrix = prixJourRepository
                .findByProduitIdOrderByDatePrixDesc(produit.getId())
                .stream()
                .findFirst();

        ProduitResponse.ProduitResponseBuilder builder = ProduitResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .type(produit.getType().name())
                .statut(produit.getStatut().name())
                .description(produit.getDescription())
                .obligatoire(produit.isObligatoire())
                .categorie(produit.getCategorie())
                .unite(produit.getUnite())
                .commissionFixe(produit.getCommissionFixe())
                .commissionPourcentage(produit.getCommissionPourcentage());

        dernierPrix.ifPresent(p -> builder
                .prixTtcActuel(p.getPrixTtc())
                .prixHtvaActuel(p.getPrixHtva())
                .prixHttActuel(p.getPrixHtt())
                .datePrixActuel(p.getDatePrix().format(FORMATTER)));

        return builder.build();
    }
}