package cardoil.backend.service;

import cardoil.backend.dto.request.EntrepriseRequest;
import cardoil.backend.dto.response.AdminEntrepriseInfoResponse;
import cardoil.backend.dto.response.EntrepriseResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AdminEntrepriseService {
    List<EntrepriseResponse> getAll(String login);
    EntrepriseResponse create(String login, EntrepriseRequest request);
    EntrepriseResponse update(String login, Long id, EntrepriseRequest request);
    void delete(String login, Long id);
    EntrepriseResponse toggleActif(String login, Long id);
    AdminEntrepriseInfoResponse getAdmin(String login, Long entrepriseId);
    EntrepriseResponse crediterSolde(String login, Long id, BigDecimal montant);
}