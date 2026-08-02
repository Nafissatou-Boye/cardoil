package cardoil.backend.service;

import cardoil.backend.dto.response.PublicStatsResponse;

public interface PublicStatsService {
    PublicStatsResponse getStats();
}