package cardoil.backend.service;

import cardoil.backend.dto.response.RapportTransactionResponse;

import java.util.List;

public interface AdminTransactionService {
    List<RapportTransactionResponse> getTransactions(String login, String periode, Long stationId);
}