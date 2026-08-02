package cardoil.backend.service;

import cardoil.backend.dto.response.CompteEmployeResponse;
import cardoil.backend.dto.response.QrCodeResponse;

public interface EmployeCompteService {
    CompteEmployeResponse getMonCompte(String loginEmploye);
    QrCodeResponse genererQrCode(String loginEmploye);
}