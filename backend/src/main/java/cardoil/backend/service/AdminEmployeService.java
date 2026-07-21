package cardoil.backend.service;

import cardoil.backend.dto.request.EmployeRequest;
import cardoil.backend.dto.response.EmployeResponse;

import java.util.List;

public interface AdminEmployeService {
    List<EmployeResponse> getAll(String login);
    EmployeResponse create(String login, EmployeRequest request);
    EmployeResponse update(String login, Long id, EmployeRequest request);
    void delete(String login, Long id);
}