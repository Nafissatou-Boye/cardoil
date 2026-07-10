package cardoil.backend.service;

import cardoil.backend.dto.request.StationRequest;
import cardoil.backend.dto.response.StationResponse;

import java.util.List;

public interface StationService {
    List<StationResponse> getAll();
    StationResponse getById(Long id);
    StationResponse create(StationRequest request);
    StationResponse update(Long id, StationRequest request);
    void delete(Long id);
}