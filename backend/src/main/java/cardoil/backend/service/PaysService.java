package cardoil.backend.service;

import cardoil.backend.dto.request.PaysRequest;
import cardoil.backend.dto.response.PaysResponse;

import java.util.List;

public interface PaysService {
    List<PaysResponse> getAll();
    PaysResponse getById(Long id);
    PaysResponse create(PaysRequest request);
    PaysResponse update(Long id, PaysRequest request);
    void delete(Long id);
}