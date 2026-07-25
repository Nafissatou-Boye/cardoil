package cardoil.backend.service;

import cardoil.backend.dto.response.CompagnieOptionResponse;

public interface ClientProfilService {
    void changerCompagnie(String login, Long compagnieId);
    CompagnieOptionResponse getMaCompagnie(String login);
}