package cardoil.backend.service;

import cardoil.backend.dto.request.CompagnieProfilRequest;
import cardoil.backend.dto.response.CompagnieProfilResponse;

public interface AdminParametresService {
    CompagnieProfilResponse getProfil(String login);
    CompagnieProfilResponse updateProfil(String login, CompagnieProfilRequest request);
}