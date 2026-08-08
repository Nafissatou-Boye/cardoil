package cardoil.backend.service;

import cardoil.backend.dto.response.RapportTransactionEmployeResponse;

import java.util.List;

public interface EntrepriseTransactionService {
    List<RapportTransactionEmployeResponse> getTransactions(String login, String periode);
}