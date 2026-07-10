package cardoil.backend.service;

import cardoil.backend.dto.request.CadeauRequest;
import cardoil.backend.dto.response.CadeauResponse;

import java.util.List;

public interface AdminCadeauService {
    List<CadeauResponse> getAll(String login);
    CadeauResponse create(String login, CadeauRequest request);
    CadeauResponse update(String login, Long id, CadeauRequest request);
    void delete(String login, Long id);
    CadeauResponse toggleActif(String login, Long id);
}