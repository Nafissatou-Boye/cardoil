package cardoil.backend.service;

import cardoil.backend.dto.request.InscriptionClientRequest;
import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.entity.Client;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Role;
import cardoil.backend.enums.StatutCompteClient;
import cardoil.backend.exception.CardoilException;
import cardoil.backend.repository.ClientRepository;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientOtpService {

    private final ClientRepository clientRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CompagnieRepository compagnieRepository;
    private final OrangeSmsService orangeSmsService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    private static final int DUREE_VALIDITE_MINUTES = 5;

    // ===== Inscription complète (formulaire rempli avant l'envoi de l'OTP) =====

    @Transactional
    public void inscrire(InscriptionClientRequest request) {

        if (utilisateurRepository.existsByTelephone(request.getTelephone())) {
    throw new CardoilException("Ce numéro de téléphone est déjà utilisé");
}
        if (!request.getMotDePasse().equals(request.getConfirmerMotDePasse())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        if (utilisateurRepository.findByTelephone(request.getTelephone()).isPresent()) {
            throw new IllegalArgumentException("Ce numéro est déjà utilisé");
        }

        Compagnie compagnie = compagnieRepository.findById(request.getCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée"));

        String[] parties = request.getNomComplet().trim().split("\\s+", 2);
        String prenom = parties[0];
        String nom = parties.length > 1 ? parties[1] : "";

        Client client = Client.builder()
                .login(request.getTelephone())
                .motDePasse(encoder.encode(request.getMotDePasse()))
                .role(Role.CLIENT)
                .nom(nom)
                .prenom(prenom)
                .telephone(request.getTelephone())
                .compagnie(compagnie)
                .nomUtilisateur(genererNomUtilisateurUnique(prenom, nom))
                .statutCompte(StatutCompteClient.ACTIF)
                .actif(false) // devient actif une fois le téléphone vérifié
                .telephoneVerifie(false)
                .build();

        String code = genererCode();
        client.setCodeOtp(code);
        client.setCodeOtpExpiration(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES));

        clientRepository.save(client);

        boolean envoye = orangeSmsService.envoyerCodeOtp(request.getTelephone(), code);
        if (!envoye) {
            throw new CardoilException("Impossible d'envoyer le SMS pour le moment");
        }
    }

    // ===== Renvoi du code (compte déjà créé, pas encore vérifié) =====

    @Transactional
    public void renvoyerOtp(String telephone) {
        Client client = clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new EntityNotFoundException("Aucun compte trouvé pour ce numéro"));

        if (client.isTelephoneVerifie()) {
            throw new IllegalStateException("Ce numéro est déjà vérifié");
        }

        String code = genererCode();
        client.setCodeOtp(code);
        client.setCodeOtpExpiration(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES));
        clientRepository.save(client);

        boolean envoye = orangeSmsService.envoyerCodeOtp(telephone, code);
        if (!envoye) {
            throw new CardoilException("Impossible d'envoyer le SMS pour le moment");
        }
    }

    // ===== Vérification (finalise l'inscription, le mot de passe est déjà défini) =====

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
        client.setActif(true);
        client.setCodeOtp(null);
        client.setCodeOtpExpiration(null);
        clientRepository.save(client);
    }

    public List<CompagnieOptionResponse> getCompagniesDisponibles() {
        return compagnieRepository.findAll().stream()
                .map(c -> CompagnieOptionResponse.builder()
                        .id(c.getId())
                        .nom(c.getNom())
                        .build())
                .toList();
    }

    // ===== HELPERS =====

    private String genererNomUtilisateurUnique(String prenom, String nom) {
        String base = normaliser(prenom) + normaliser(nom);
        if (base.isBlank()) {
            base = "Client";
        }
        String candidat = base;
        int suffixe = 1;
        while (clientRepository.existsByNomUtilisateurIgnoreCase(candidat)) {
            suffixe++;
            candidat = base + suffixe;
        }
        return candidat;
    }

    private String normaliser(String texte) {
        if (texte == null || texte.isBlank()) return "";
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // Garde uniquement lettres et chiffres, préserve la casse d'origine (ex: "Iba Faye" -> "IbaFaye")
        return sansAccents.replaceAll("[^a-zA-Z0-9]", "");
    }

    private String genererCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    // ===== MOT DE PASSE OUBLIÉ (client déjà inscrit et vérifié) =====

    @Transactional
    public void demanderResetMotDePasse(String telephone) {
        Client client = clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new EntityNotFoundException("Aucun compte trouvé pour ce numéro"));

        if (!client.isTelephoneVerifie()) {
            throw new IllegalStateException("Ce compte n'est pas encore vérifié, terminez d'abord l'inscription");
        }

        String code = genererCode();
        client.setCodeOtp(code);
        client.setCodeOtpExpiration(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES));
        clientRepository.save(client);

        boolean envoye = orangeSmsService.envoyerCodeOtp(telephone, code);
        if (!envoye) {
            throw new CardoilException("Impossible d'envoyer le SMS pour le moment");
        }
    }

    // Vérification "souple" : confirme juste que le code est valide, sans le consommer.
    // La vraie validation finale a lieu dans reinitialiserMotDePasse (défense en profondeur :
    // impossible de changer le mot de passe sans repasser par le bon code, même en appelant
    // directement l'API sans passer par cette étape intermédiaire).
    public void verifierCodeReset(String telephone, String code) {
        Client client = clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new CardoilException("Aucune demande en cours pour ce numéro"));

        if (client.getCodeOtp() == null || client.getCodeOtpExpiration() == null) {
            throw new CardoilException("Aucun code en attente pour ce numéro");
        }
        if (LocalDateTime.now().isAfter(client.getCodeOtpExpiration())) {
            throw new CardoilException("Code expiré, redemandez-en un nouveau");
        }
        if (!client.getCodeOtp().equals(code)) {
            throw new CardoilException("Code incorrect");
        }
    }

    @Transactional
    public void reinitialiserMotDePasse(String telephone, String code, String nouveauMotDePasse) {
        Client client = clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new CardoilException("Aucune demande en cours pour ce numéro"));

        if (client.getCodeOtp() == null || client.getCodeOtpExpiration() == null) {
            throw new CardoilException("Aucun code en attente pour ce numéro");
        }
        if (LocalDateTime.now().isAfter(client.getCodeOtpExpiration())) {
            throw new CardoilException("Code expiré, redemandez-en un nouveau");
        }
        if (!client.getCodeOtp().equals(code)) {
            throw new CardoilException("Code incorrect");
        }

        client.setMotDePasse(encoder.encode(nouveauMotDePasse));
        client.setCodeOtp(null);
        client.setCodeOtpExpiration(null);
        clientRepository.save(client);
    }
}