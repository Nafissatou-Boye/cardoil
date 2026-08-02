// TransactionService.java — interface complète mise à jour
package cardoil.backend.service;

import cardoil.backend.dto.request.*;
import cardoil.backend.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    InitierTransactionResponse initierTransaction(String loginOperateur, InitierTransactionRequest request);
    ConfirmerTransactionResponse confirmerTransaction(String loginClient, ConfirmerTransactionRequest request);
    StatutTransactionResponse getStatut(String loginOperateur, Long transactionId);
    RechargeClientResponse rechargerClient(String loginOperateur, String telephone, RechargeRequest request);
    AchatCarteResponse payerParCarte(String loginOperateur, AchatCarteRequest request);
    StatsJourResponse getStatsDuJour(String loginOperateur);
    List<TransactionRecenteResponse> getTransactionsRecentes(String loginOperateur);
    AnnulerTransactionResponse annulerTransaction(String loginOperateur, Long transactionId, AnnulerTransactionRequest request);
    StatsJourResponse getStatsPeriode(String loginOperateur, LocalDate debut, LocalDate fin);

    // Historique complet pour un client particulier (écran "Historique" app Client)
    List<HistoriqueTransactionResponse> getTransactionsClient(String loginClient);

    // Confirmation de vente pour un employé (scan du QR généré par
    // initierTransaction) — débite Carte.solde, pas Utilisateur/Client.solde.
    ConfirmerTransactionResponse confirmerTransactionEmploye(String loginEmploye, ConfirmerTransactionRequest request);

    // Résolution du QR d'IDENTITÉ (Client.qrCode ou Carte.codeQr) scanné par
    // le pompiste/gérant — sens inverse de initierTransaction/confirmer :
    // ici c'est le porteur qui montre son identité, le pompiste qui scanne
    // et débite en une seule étape (comme payerParCarte, mais résolu par
    // code temporaire au lieu d'un numeroCarte fixe).
    PayerParQrResponse payerParQr(String loginOperateur, PayerParQrRequest request);

    // ✅ Nouveau — résolution EN LECTURE SEULE du QR d'identité, sans débiter
    // ni invalider le code. Permet d'afficher une confirmation ("vous allez
    // débiter [porteurNom]") avant que payerParQr ne soit réellement appelé.
    ResoudreQrResponse resoudreQr(String code);

    // ✅ Nouveau — symétrique de payerParQr mais en sens inverse (crédit, pas
    // débit) et limité aux Clients — les Employés ne sont jamais rechargés
    // par un gérant de station (recharge via l'entreprise uniquement).
    // resoudreQr reste utilisable tel quel pour la confirmation préalable,
    // il ne distingue pas le type d'opération à venir.
    RechargeClientResponse rechargerParQr(String loginOperateur, RechargeParQrRequest request);
}