package cardoil.backend.service;

import cardoil.backend.dto.request.TransactionRequest;
import cardoil.backend.dto.response.ProduitResponse;
import cardoil.backend.dto.response.TransactionResponse;

import java.util.List;

public interface GerantTransactionService {
    TransactionResponse create(String login, TransactionRequest request);
    List<TransactionResponse> getRecentes(String login);
    List<ProduitResponse> getProduitsDisponibles(String login);
}