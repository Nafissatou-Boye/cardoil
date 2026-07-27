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
}