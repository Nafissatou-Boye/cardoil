package cardoil.backend.service;

import cardoil.backend.dto.request.RechargeRequestDTO;
import cardoil.backend.dto.response.RechargeResponseDTO;
import cardoil.backend.entity.*;
import cardoil.backend.enums.CodeErreurRecharge;
import cardoil.backend.enums.StatutCompteClient;
import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.enums.StatutRecharge;
import cardoil.backend.exception.RechargeException;
import cardoil.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RechargeExterneService {

    private final RechargeExterneRepository rechargeRepository;
    private final EtablissementFinancierCompagnieRepository liaisonRepository;
    private final CompagnieRepository compagnieRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public RechargeResponseDTO traiterRecharge(RechargeRequestDTO requete,
                                                EtablissementFinancier etablissement,
                                                String adresseIp) {

        Optional<RechargeExterne> existante = rechargeRepository
                .findByEtablissementFinancierIdAndReferencePartenaire(
                        etablissement.getId(), requete.getReference());
        if (existante.isPresent()) {
            return construireReponse(existante.get());
        }

        Compagnie compagnie = compagnieRepository.findById(Long.valueOf(requete.getCompanyId()))
                .orElseThrow(() -> new RechargeException(CodeErreurRecharge.ERR_COMPANY_INACTIVE,
                        "Compagnie introuvable ou désactivée", 403));

        EtablissementFinancierCompagnie liaison = liaisonRepository
                .findByEtablissementFinancierIdAndCompagnieId(etablissement.getId(), compagnie.getId())
                .orElseThrow(() -> new RechargeException(CodeErreurRecharge.ERR_PARTNER_INACTIVE,
                        "Établissement non autorisé pour cette compagnie", 403));

        if (liaison.getStatut() != StatutEtablissement.ACTIF) {
            throw new RechargeException(CodeErreurRecharge.ERR_PARTNER_INACTIVE,
                    "Liaison établissement/compagnie désactivée", 403);
        }

        // Devise dérivée du Pays de la Compagnie — jamais codée en dur.
        // Repli sur XOF uniquement si la Compagnie n'a pas de Pays renseigné (ne devrait pas arriver en usage normal).
        String devise = (compagnie.getPays() != null && compagnie.getPays().getDevise() != null)
                ? compagnie.getPays().getDevise()
                : "XOF";

        Client client = clientRepository.findByTelephone(requete.getPhoneNumber())
                .orElse(null);

        RechargeExterne recharge = RechargeExterne.builder()
                .referencePartenaire(requete.getReference())
                .etablissementFinancier(etablissement)
                .compagnie(compagnie)
                .telephoneClient(requete.getPhoneNumber())
                .montant(requete.getAmount())
                .devise(devise)
                .description(requete.getDescription())
                .dateDemande(requete.getTimestamp().toLocalDateTime())
                .adresseIp(adresseIp)
                .build();

        if (client == null) {
            recharge.setStatut(StatutRecharge.FAILED);
            recharge.setCodeErreur(CodeErreurRecharge.ERR_CLIENT_NOT_FOUND);
            recharge.setDateTraitement(LocalDateTime.now());
            rechargeRepository.save(recharge);
            throw new RechargeException(CodeErreurRecharge.ERR_CLIENT_NOT_FOUND,
                    "Aucun compte client trouvé pour ce numéro", 404);
        }

        if (client.getStatutCompte() != StatutCompteClient.ACTIF) {
            recharge.setClient(client);
            recharge.setStatut(StatutRecharge.FAILED);
            recharge.setCodeErreur(CodeErreurRecharge.ERR_ACCOUNT_SUSPENDED);
            recharge.setDateTraitement(LocalDateTime.now());
            rechargeRepository.save(recharge);
            throw new RechargeException(CodeErreurRecharge.ERR_ACCOUNT_SUSPENDED,
                    "Le compte client existe mais est suspendu ou bloqué", 422);
        }

        if (requete.getAmount().compareTo(liaison.getMontantMinimum()) < 0
                || requete.getAmount().compareTo(liaison.getMontantMaximumParTransaction()) > 0) {
            recharge.setClient(client);
            recharge.setStatut(StatutRecharge.FAILED);
            recharge.setCodeErreur(CodeErreurRecharge.ERR_INVALID_AMOUNT);
            recharge.setDateTraitement(LocalDateTime.now());
            rechargeRepository.save(recharge);
            throw new RechargeException(CodeErreurRecharge.ERR_INVALID_AMOUNT,
                    "Montant hors des plafonds autorisés", 400);
        }

        client.setSolde(client.getSolde().add(requete.getAmount()));
        clientRepository.save(client);

        recharge.setClient(client);
        recharge.setStatut(StatutRecharge.SUCCESS);
        recharge.setDateTraitement(LocalDateTime.now());

        RechargeExterne sauvegardee = rechargeRepository.save(recharge);

        return construireReponse(sauvegardee);
    }

    private RechargeResponseDTO construireReponse(RechargeExterne r) {
        RechargeResponseDTO.RechargeResponseDTOBuilder builder = RechargeResponseDTO.builder()
                .transactionId(r.getId())
                .reference(r.getReferencePartenaire())
                .status(r.getStatut().name())
                .devise(r.getDevise())
                .processedAt(r.getDateTraitement());

        if (r.getClient() != null) {
            builder.nouveauSolde(r.getClient().getSolde());
        }
        return builder.build();
    }
}