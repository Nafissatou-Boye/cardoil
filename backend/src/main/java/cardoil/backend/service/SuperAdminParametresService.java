package cardoil.backend.service;

import cardoil.backend.dto.request.SuperAdminProfilRequest;
import cardoil.backend.dto.response.SuperAdminProfilResponse;

public interface SuperAdminParametresService {
    SuperAdminProfilResponse getProfil(String login);
    SuperAdminProfilResponse updateProfil(String login, SuperAdminProfilRequest request);
}