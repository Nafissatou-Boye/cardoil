package cardoil.backend.service;

import cardoil.backend.dto.response.RapportDepartementResponse;
import cardoil.backend.dto.response.RapportEmployeResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.dto.response.SuiviBudgetResponse;

import java.util.List;

public interface AdminRapportService {
    List<RapportDepartementResponse> getRapportDepartements(String login);
    List<RapportEmployeResponse> getRapportEmployes(String login);
    SuiviBudgetResponse getSuiviBudget(String login);
    List<RechargeResponse> getHistoriqueGlobal(String login);
}