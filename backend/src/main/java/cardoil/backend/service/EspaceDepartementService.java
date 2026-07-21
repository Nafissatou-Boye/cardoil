package cardoil.backend.service;

import cardoil.backend.dto.request.CarteRequest;
import cardoil.backend.dto.request.EmployeDepartementRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.EmployeResponse;
import cardoil.backend.dto.response.EspaceDepartementInfoResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.entity.StatutCarte;

import java.util.List;

public interface EspaceDepartementService {

    EspaceDepartementInfoResponse getInfo(String login);

    List<EmployeResponse> getEmployes(String login);
    EmployeResponse createEmploye(String login, EmployeDepartementRequest request);
    EmployeResponse updateEmploye(String login, Long employeId, EmployeDepartementRequest request);
    void deleteEmploye(String login, Long employeId);

    List<CarteResponse> getCartes(String login);
    CarteResponse createCarte(String login, CarteRequest request);
    CarteResponse changerStatutCarte(String login, Long carteId, StatutCarte statut);
    RechargeResponse rechargerCarte(String login, Long carteId, RechargeRequest request);
    List<RechargeResponse> getHistoriqueRecharges(String login, Long carteId);
}