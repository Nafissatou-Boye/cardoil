package cardoil.backend.service;

import cardoil.backend.dto.request.AdminCompagnieRequest;
import cardoil.backend.dto.response.AdminCompagnieResponse;

import java.util.List;

public interface SuperAdminUtilisateurService {
    List<AdminCompagnieResponse> getAll();
    AdminCompagnieResponse create(AdminCompagnieRequest request);
    AdminCompagnieResponse update(Long id, AdminCompagnieRequest request);
    void delete(Long id);
    AdminCompagnieResponse toggleActif(Long id);
}