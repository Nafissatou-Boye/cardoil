package cardoil.backend.service;

import cardoil.backend.dto.response.DashboardAdminCompagnieResponse;

public interface AdminCompagnieService {
    DashboardAdminCompagnieResponse getDashboard(String login);
}