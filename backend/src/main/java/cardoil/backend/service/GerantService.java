package cardoil.backend.service;

import cardoil.backend.dto.response.GerantDashboardResponse;

public interface GerantService {
    GerantDashboardResponse getDashboard(String login);
}