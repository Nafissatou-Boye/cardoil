package cardoil.backend.service;

import cardoil.backend.dto.request.CarteRequest;
import cardoil.backend.dto.request.RechargeGroupeeRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.response.CarteResponse;
import cardoil.backend.dto.response.RechargeGroupeeResponse;
import cardoil.backend.dto.response.RechargeResponse;
import cardoil.backend.entity.StatutCarte;

import java.util.List;

public interface AdminCarteService {
    List<CarteResponse> getAll(String login);
    CarteResponse create(String login, CarteRequest request);
    CarteResponse changerStatut(String login, Long carteId, StatutCarte nouveauStatut);
    RechargeResponse recharger(String login, Long carteId, RechargeRequest request);
    List<RechargeResponse> getHistoriqueRecharges(String login, Long carteId);
    CarteResponse renouveler(String login, Long carteId);

    RechargeGroupeeResponse rechargerGroupe(String login, RechargeGroupeeRequest request);
    List<RechargeGroupeeResponse> getHistoriqueRechargesGroupees(String login);
}