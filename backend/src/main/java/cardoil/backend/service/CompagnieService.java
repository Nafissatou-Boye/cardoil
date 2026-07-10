package cardoil.backend.service;

import cardoil.backend.dto.request.CompagnieRequest;
import cardoil.backend.dto.response.CompagnieResponse;

import java.util.List;

public interface CompagnieService {
    List<CompagnieResponse> getAll();
    CompagnieResponse getById(Long id);
    CompagnieResponse create(CompagnieRequest request);
    CompagnieResponse update(Long id, CompagnieRequest request);
    void delete(Long id);
}