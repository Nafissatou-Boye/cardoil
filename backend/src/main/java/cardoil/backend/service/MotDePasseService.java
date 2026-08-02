package cardoil.backend.service;

import cardoil.backend.dto.request.ChangerMotDePasseRequest;

public interface MotDePasseService {
    void changerMotDePasse(String login, ChangerMotDePasseRequest request);
}