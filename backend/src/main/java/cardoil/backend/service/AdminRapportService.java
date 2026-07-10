package cardoil.backend.service;

import cardoil.backend.dto.response.RapportGlobalResponse;

public interface AdminRapportService {
    RapportGlobalResponse getRapport(String login, String periode);
}