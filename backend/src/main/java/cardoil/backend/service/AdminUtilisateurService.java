package cardoil.backend.service;

import cardoil.backend.dto.response.UtilisateurDetailResponse;
import cardoil.backend.dto.response.UtilisateurListItemResponse;

import java.util.List;

public interface AdminUtilisateurService {
    List<UtilisateurListItemResponse> getAll(String login);
    UtilisateurDetailResponse getDetail(String login, Long id);
}