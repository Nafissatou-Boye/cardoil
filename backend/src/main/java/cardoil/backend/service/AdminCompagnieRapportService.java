package cardoil.backend.service;

import cardoil.backend.dto.response.RapportGlobalResponse;


public interface AdminCompagnieRapportService {
    RapportGlobalResponse getRapport(String login, String periode);
}