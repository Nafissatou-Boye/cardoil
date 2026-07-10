package cardoil.backend.service;

import cardoil.backend.dto.request.PrixRequest;
import cardoil.backend.dto.response.PrixJourResponse;
import cardoil.backend.dto.response.PrixProduitDTO;

import java.util.List;

public interface AdminPrixService {
    PrixProduitDTO definirPrix(String login, Long produitId, PrixRequest request);
    List<PrixJourResponse> getHistorique(String login, Long produitId);
    List<PrixProduitDTO> getProgrammation(String login, Long produitId); // 🆕
}