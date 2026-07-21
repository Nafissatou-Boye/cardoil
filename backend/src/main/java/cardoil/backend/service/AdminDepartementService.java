package cardoil.backend.service;

import cardoil.backend.dto.request.AdminDepartementRequest;
import cardoil.backend.dto.request.DepartementRequest;
import cardoil.backend.dto.response.AdminDepartementInfoResponse;
import cardoil.backend.dto.response.DepartementResponse;
import cardoil.backend.dto.response.EntrepriseInfoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AdminDepartementService {
    List<DepartementResponse> getAll(String login);
    DepartementResponse create(String login, DepartementRequest request);
    DepartementResponse update(String login, Long id, DepartementRequest request);
    void delete(String login, Long id);
    DepartementResponse toggleActif(String login, Long id);
    DepartementResponse crediterBudget(String login, Long id, BigDecimal montant);

    AdminDepartementInfoResponse getAdmin(String login, Long departementId);
    AdminDepartementInfoResponse createAdmin(String login, Long departementId, AdminDepartementRequest request);
    AdminDepartementInfoResponse remplacerAdmin(String login, Long departementId, AdminDepartementRequest request);

    EntrepriseInfoResponse getInfoEntreprise(String login);
}