package cardoil.backend.service;

import cardoil.backend.dto.request.PromotionRequest;
import cardoil.backend.dto.response.PromotionResponse;

import java.util.List;

public interface AdminPromotionService {
    List<PromotionResponse> getAll(String login);
    PromotionResponse create(String login, PromotionRequest request);
    PromotionResponse update(String login, Long id, PromotionRequest request);
    PromotionResponse changerStatut(String login, Long id, String nouveauStatut);
    void delete(String login, Long id);
}