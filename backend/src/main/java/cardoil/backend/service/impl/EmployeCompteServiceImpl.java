package cardoil.backend.service.impl;

import cardoil.backend.dto.response.CompteEmployeResponse;
import cardoil.backend.dto.response.QrCodeResponse;
import cardoil.backend.entity.Carte;
import cardoil.backend.entity.Employe;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.EmployeCompteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeCompteServiceImpl implements EmployeCompteService {

    private final UtilisateurRepository utilisateurRepository;
    private final CarteRepository carteRepository;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int DUREE_VALIDITE_MINUTES = 5;

    @Override
    public CompteEmployeResponse getMonCompte(String loginEmploye) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(loginEmploye)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!(utilisateur instanceof Employe employe)) {
            throw new IllegalStateException("Cette action est réservée aux employés");
        }

        Carte carte = carteRepository.findByEmployeId(employe.getId())
                .orElseThrow(() -> new EntityNotFoundException("Aucune carte associée à cet employé"));

        return CompteEmployeResponse.builder()
                .nomComplet(employe.getPrenom() + " " + employe.getNom())
                .matricule(employe.getMatricule())
                .entrepriseId(employe.getEntreprise().getId())
                .entrepriseNom(employe.getEntreprise().getNom())
                .compagnieId(employe.getEntreprise().getCompagnie().getId())
                .compagnieNom(employe.getEntreprise().getCompagnie().getNom())
                .departementId(employe.getDepartement() != null ? employe.getDepartement().getId() : null)
                .departementNom(employe.getDepartement() != null ? employe.getDepartement().getNom() : null)
                .numeroCarte(carte.getNumeroCarte())
                .typeCarte(carte.getTypeCarte().name())
                .solde(carte.getSolde())
                .statut(carte.getStatut().name())
                .montantDotationMensuelle(carte.getMontantDotationMensuelle())
                .dateRenouvellement(carte.getDateRenouvellement())
                .plafondCumuleMax(carte.getPlafondCumuleMax())
                .build();
    }


    @Override
    public QrCodeResponse genererQrCode(String loginEmploye) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(loginEmploye)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!(utilisateur instanceof Employe employe)) {
            throw new IllegalStateException("Cette action est réservée aux employés");
        }

        Carte carte = carteRepository.findByEmployeId(employe.getId())
                .orElseThrow(() -> new EntityNotFoundException("Aucune carte associée à cet employé"));

        String code = genererCodeUnique();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES);

        carte.setCodeQr(code);
        carte.setCodeQrExpiration(expiration);
        carteRepository.save(carte);

        return QrCodeResponse.builder()
                .code(code)
                .expiration(expiration)
                .build();
    }

    private String genererCodeUnique() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(20);
            for (int i = 0; i < 20; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (carteRepository.findByCodeQr(code).isPresent());
        return code;
    }
}