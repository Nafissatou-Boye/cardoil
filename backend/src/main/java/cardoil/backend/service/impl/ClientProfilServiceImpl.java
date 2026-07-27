package cardoil.backend.service.impl;

import cardoil.backend.dto.response.ClientProfilResponse;
import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.dto.response.QrCodeResponse;
import cardoil.backend.entity.Client;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.ClientRepository;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.FideliteClientRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.ClientProfilService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientProfilServiceImpl implements ClientProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final CompagnieRepository compagnieRepository;
    private final FideliteClientRepository fideliteClientRepository;

    private static final String CHARS_QR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int DUREE_VALIDITE_MINUTES = 15;

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

    @Override
    public ClientProfilResponse getMonProfil(String login) {
        Client client = getClient(login);

        int pointsDisponibles = 0;
        int pointsTotal = 0;

        if (client.getCompagnie() != null) {
            var fidelite = fideliteClientRepository
                    .findByClientIdAndCompagnieId(client.getId(), client.getCompagnie().getId());
            if (fidelite.isPresent()) {
                pointsDisponibles = fidelite.get().getPointsDisponibles();
                pointsTotal = fidelite.get().getPointsTotal();
            }
        }

        return ClientProfilResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .telephone(client.getTelephone())
                .nomUtilisateur(client.getNomUtilisateur())
                .solde(client.getSolde())
                .telephoneVerifie(client.isTelephoneVerifie())
                .actif(client.isActif())
                .compagnieId(client.getCompagnie() != null ? client.getCompagnie().getId() : null)
                .compagnieNom(client.getCompagnie() != null ? client.getCompagnie().getNom() : null)
                .pointsFideliteDisponibles(pointsDisponibles)
                .pointsFideliteTotal(pointsTotal)
                .build();
    }

    @Override
    public QrCodeResponse genererQrCode(String login) {
        Client client = getClient(login);

        String code = genererCodeUnique();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES);

        client.setQrCode(code);
        client.setQrCodeExpiration(expiration);
        utilisateurRepository.save(client);

        return QrCodeResponse.builder()
                .code(code)
                .expiration(expiration)
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

    private String genererCodeUnique() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(20);
            for (int i = 0; i < 20; i++) {
                sb.append(CHARS_QR.charAt(random.nextInt(CHARS_QR.length())));
            }
            code = sb.toString();
        } while (clientRepository.findByQrCode(code).isPresent());
        return code;
    }
}