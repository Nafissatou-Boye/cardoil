package cardoil.backend.service;

import cardoil.backend.dto.request.StationAdminRequest;
import cardoil.backend.dto.response.StationResponse;

import java.util.List;

public interface AdminStationService {
    List<StationResponse> getMesStations(String login);
    StationResponse create(String login, StationAdminRequest request);
    StationResponse update(String login, Long id, StationAdminRequest request);
    void delete(String login, Long id);
}