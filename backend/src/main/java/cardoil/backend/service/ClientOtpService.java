package cardoil.backend.service;

import cardoil.backend.entity.Client;
import cardoil.backend.enums.StatutCompteClient;
import cardoil.backend.exception.CardoilException;
import cardoil.backend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientOtpService {

    private final ClientRepository clientRepository;
    private final OrangeSmsService orangeSmsService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    private static final int DUREE_VALIDITE_MINUTES = 5;

    @Transactional
    public void demanderOtp(String telephone) {
        Client client = clientRepository.findByTelephone(telephone)
                .orElseGet(() -> creerClientNonVerifie(telephone));

        String code = genererCode();
        client.setCodeOtp(code);
        client.setCodeOtpExpiration(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES));
        clientRepository.save(client);

        boolean envoye = orangeSmsService.envoyerCodeOtp(telephone, code);
if (!envoye) {
    throw new CardoilException("Impossible d'envoyer le SMS pour le moment");
}
    }

    @Transactional
    public void verifierOtp(String telephone, String code) {
        Client client = clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new CardoilException("Aucune demande OTP pour ce numéro"));

        if (client.getCodeOtp() == null || client.getCodeOtpExpiration() == null) {
            throw new CardoilException("Aucun code en attente pour ce numéro");
        }
        if (LocalDateTime.now().isAfter(client.getCodeOtpExpiration())) {
            throw new CardoilException("Code expiré, redemandez-en un nouveau");
        }
        if (!client.getCodeOtp().equals(code)) {
            throw new CardoilException("Code incorrect");
        }

        client.setTelephoneVerifie(true);
        client.setCodeOtp(null);
        client.setCodeOtpExpiration(null);
        clientRepository.save(client);
    }

    private Client creerClientNonVerifie(String telephone) {
        // compagnie volontairement absente ici : un client particulier s'inscrit lui-même,
        // sans compagnie connue au moment de l'inscription (colonne nullable depuis la migration JOINED).
        Client client = Client.builder()
                .login(telephone)
                .motDePasse(encoder.encode(genererCode())) // valeur inutilisable, jamais destinée à servir de mot de passe
                .role(cardoil.backend.entity.Role.CLIENT)
                .telephone(telephone)
                .statutCompte(StatutCompteClient.ACTIF)
                .actif(true)
                .build();
        return client;
    }

    private String genererCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}