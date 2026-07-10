package cardoil.backend.service;

import cardoil.backend.dto.request.PrixProduitRequestDTO;
import cardoil.backend.dto.response.PrixProduitDTO;
import cardoil.backend.entity.PrixProduit;
import cardoil.backend.entity.Produit;
import cardoil.backend.exception.CardoilException;
import cardoil.backend.repository.PrixProduitRepository;
import cardoil.backend.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrixProduitService {

    private final PrixProduitRepository prixProduitRepository;
    private final ProduitRepository produitRepository;

    @Transactional
    public PrixProduitDTO creer(Long produitId, PrixProduitRequestDTO dto) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new CardoilException("Produit introuvable"));

        if (dto.getDateFin() != null && dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new CardoilException("La date de fin ne peut pas précéder la date de début");
        }

        // 🆕 Ferme automatiquement toute configuration encore "ouverte" qui chevaucherait celle-ci.
        // Règle ajoutée par moi — le CDC (section 6.2) ne précise pas la gestion du chevauchement.
        fermerConfigurationsChevauchantes(produitId, dto.getDateDebut());

        PrixProduit prixProduit = PrixProduit.builder()
                .produit(produit)
                .prixTtc(dto.getPrixTtc())
                .prixHtva(dto.getPrixHtva())
                .prixHtt(dto.getPrixHtt())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .build();

        return versDTO(prixProduitRepository.save(prixProduit));
    }

    private void fermerConfigurationsChevauchantes(Long produitId, LocalDate nouvelleDateDebut) {
        List<PrixProduit> existantes = prixProduitRepository.findByProduitIdOrderByDateDebutDesc(produitId);
        for (PrixProduit config : existantes) {
            boolean chevauche = config.getDateDebut().isBefore(nouvelleDateDebut)
                    && (config.getDateFin() == null || !config.getDateFin().isBefore(nouvelleDateDebut));
            if (chevauche) {
                config.setDateFin(nouvelleDateDebut.minusDays(1));
                prixProduitRepository.save(config);
            }
        }
    }

    public List<PrixProduitDTO> listerPourProduit(Long produitId) {
        return prixProduitRepository.findByProduitIdOrderByDateDebutDesc(produitId).stream()
                .map(this::versDTO)
                .toList();
    }

    public Optional<PrixProduitDTO> obtenirPrixEnVigueur(Long produitId, LocalDate date) {
        return prixProduitRepository.findPrixEnVigueur(produitId, date).map(this::versDTO);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!prixProduitRepository.existsById(id)) {
            throw new CardoilException("Configuration de prix introuvable");
        }
        prixProduitRepository.deleteById(id);
    }

    private PrixProduitDTO versDTO(PrixProduit p) {
        LocalDate aujourdHui = LocalDate.now();
        boolean enVigueur = !p.getDateDebut().isAfter(aujourdHui)
                && (p.getDateFin() == null || !p.getDateFin().isBefore(aujourdHui));

        return PrixProduitDTO.builder()
                .id(p.getId())
                .produitId(p.getProduit().getId())
                .produitNom(p.getProduit().getNom())
                .prixTtc(p.getPrixTtc())
                .prixHtva(p.getPrixHtva())
                .prixHtt(p.getPrixHtt())
                .dateDebut(p.getDateDebut())
                .dateFin(p.getDateFin())
                .enVigueurAujourdHui(enVigueur)
                .build();
    }
}