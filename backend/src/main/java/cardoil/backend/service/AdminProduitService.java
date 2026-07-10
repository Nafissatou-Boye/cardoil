package cardoil.backend.service;

import cardoil.backend.dto.request.ProduitRequest;
import cardoil.backend.dto.response.ProduitResponse;

import java.util.List;

public interface AdminProduitService {
    List<ProduitResponse> getAll(String login);
    ProduitResponse create(String login, ProduitRequest request);
    ProduitResponse update(String login, Long id, ProduitRequest request);
    ProduitResponse changerStatut(String login, Long id, String nouveauStatut);
    void delete(String login, Long id);
}