package cardoil.backend.service;

import cardoil.backend.dto.request.LiaisonRequestDTO;
import cardoil.backend.dto.response.LiaisonDTO;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.EtablissementFinancier;
import cardoil.backend.entity.EtablissementFinancierCompagnie;
import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.exception.CardoilException;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.EtablissementFinancierCompagnieRepository;
import cardoil.backend.repository.EtablissementFinancierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiaisonCompagnieService {

    private final EtablissementFinancierCompagnieRepository liaisonRepository;
    private final EtablissementFinancierRepository etablissementRepository;
    private final CompagnieRepository compagnieRepository;

    @Transactional
    public LiaisonDTO creerLiaison(Long etablissementId, LiaisonRequestDTO dto) {
        if (dto.getCompagnieId() == null) {
            throw new CardoilException("L'identifiant de la compagnie est requis");
        }

        EtablissementFinancier etablissement = etablissementRepository.findById(etablissementId)
                .orElseThrow(() -> new CardoilException("Établissement introuvable"));

        Compagnie compagnie = compagnieRepository.findById(dto.getCompagnieId())
                .orElseThrow(() -> new CardoilException("Compagnie introuvable"));

        if (liaisonRepository.existsByEtablissementFinancierIdAndCompagnieId(etablissementId, dto.getCompagnieId())) {
            throw new CardoilException("Cette compagnie est déjà liée à cet établissement");
        }

        EtablissementFinancierCompagnie.EtablissementFinancierCompagnieBuilder builder =
                EtablissementFinancierCompagnie.builder()
                        .etablissementFinancier(etablissement)
                        .compagnie(compagnie)
                        .statut(StatutEtablissement.ACTIF);

        if (dto.getMontantMinimum() != null) builder.montantMinimum(dto.getMontantMinimum());
        if (dto.getMontantMaximumParTransaction() != null) builder.montantMaximumParTransaction(dto.getMontantMaximumParTransaction());
        if (dto.getPlafondJournalierParClient() != null) builder.plafondJournalierParClient(dto.getPlafondJournalierParClient());

        EtablissementFinancierCompagnie sauvegardee = liaisonRepository.save(builder.build());
        return versDTO(sauvegardee);
    }

    public List<LiaisonDTO> listerPourEtablissement(Long etablissementId) {
        return liaisonRepository.findByEtablissementFinancierId(etablissementId).stream()
                .map(this::versDTO)
                .toList();
    }

    @Transactional
    public LiaisonDTO modifierPlafonds(Long liaisonId, LiaisonRequestDTO dto) {
        // dto.compagnieId est ignoré ici : une liaison existante ne change jamais de compagnie.
        EtablissementFinancierCompagnie liaison = liaisonRepository.findById(liaisonId)
                .orElseThrow(() -> new CardoilException("Liaison introuvable"));

        if (dto.getMontantMinimum() != null) liaison.setMontantMinimum(dto.getMontantMinimum());
        if (dto.getMontantMaximumParTransaction() != null) liaison.setMontantMaximumParTransaction(dto.getMontantMaximumParTransaction());
        if (dto.getPlafondJournalierParClient() != null) liaison.setPlafondJournalierParClient(dto.getPlafondJournalierParClient());

        return versDTO(liaisonRepository.save(liaison));
    }

    @Transactional
    public LiaisonDTO changerStatut(Long liaisonId, StatutEtablissement nouveauStatut) {
        EtablissementFinancierCompagnie liaison = liaisonRepository.findById(liaisonId)
                .orElseThrow(() -> new CardoilException("Liaison introuvable"));
        liaison.setStatut(nouveauStatut);
        return versDTO(liaisonRepository.save(liaison));
    }

    @Transactional
    public void supprimerLiaison(Long liaisonId) {
        if (!liaisonRepository.existsById(liaisonId)) {
            throw new CardoilException("Liaison introuvable");
        }
        liaisonRepository.deleteById(liaisonId);
    }

  private LiaisonDTO versDTO(EtablissementFinancierCompagnie l) {
    String devise = (l.getCompagnie().getPays() != null && l.getCompagnie().getPays().getDevise() != null)
            ? l.getCompagnie().getPays().getDevise()
            : "XOF";

    return LiaisonDTO.builder()
            .id(l.getId())
            .compagnieId(l.getCompagnie().getId())
            .compagnieNom(l.getCompagnie().getNom())
            .devise(devise)
            .statut(l.getStatut())
            .montantMinimum(l.getMontantMinimum())
            .montantMaximumParTransaction(l.getMontantMaximumParTransaction())
            .plafondJournalierParClient(l.getPlafondJournalierParClient())
            .dateActivation(l.getDateActivation())
            .build();
}
}