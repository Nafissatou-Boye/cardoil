package cardoil.backend.service.impl;

import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.entity.Client;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.ClientProfilService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientProfilServiceImpl implements ClientProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final CompagnieRepository compagnieRepository;

    @Override
    public void changerCompagnie(String login, Long compagnieId) {
        Client client = getClient(login);

        Compagnie compagnie = compagnieRepository.findById(compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée"));

        // Changer la compagnie "active" ne touche jamais aux points de fidélité déjà acquis :
        // FideliteClient est scopé par (client_id, compagnie_id), donc chaque compagnie
        // garde son propre solde de points indépendamment de la compagnie active actuelle.
        client.setCompagnie(compagnie);
        utilisateurRepository.save(client);
    }

    @Override
    public CompagnieOptionResponse getMaCompagnie(String login) {
        Client client = getClient(login);

        if (client.getCompagnie() == null) {
            return null;
        }

        return CompagnieOptionResponse.builder()
                .id(client.getCompagnie().getId())
                .nom(client.getCompagnie().getNom())
                .build();
    }

    private Client getClient(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!(utilisateur instanceof Client client)) {
            throw new IllegalStateException("Cette action est réservée aux clients particuliers");
        }

        return client;
    }
}