package cardoil.backend.service;

import cardoil.backend.dto.response.ClientProfilResponse;
import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.dto.response.QrCodeResponse;

public interface ClientProfilService {
    void changerCompagnie(String login, Long compagnieId);
    CompagnieOptionResponse getMaCompagnie(String login);
    ClientProfilResponse getMonProfil(String login);
    QrCodeResponse genererQrCode(String login);
}