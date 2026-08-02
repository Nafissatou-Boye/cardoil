package cardoil.backend.service.impl;

import cardoil.backend.dto.request.ChangerMotDePasseRequest;
import cardoil.backend.entity.Role;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.MotDePasseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MotDePasseServiceImpl implements MotDePasseService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void changerMotDePasse(String login, ChangerMotDePasseRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IllegalStateException("Ancien mot de passe incorrect");
        }

        validerFormat(utilisateur.getRole(), request.getNouveauMotDePasse());

        if (passwordEncoder.matches(request.getNouveauMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IllegalStateException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    // Contrainte de format par rôle : CLIENT = 4 chiffres, GERANT/POMPISTE = 6
    // chiffres (confirmé). EMPLOYE : pas de règle confirmée — son mot de
    // passe initial (AdminEmployeServiceImpl.genererMotDePasse) est un
    // alphanumérique 8 caractères, pas un PIN numérique, donc la contrainte
    // 4/6 chiffres ne s'applique probablement pas. Contrainte minimale de
    // bon sens en attendant confirmation : au moins 4 caractères.
    private void validerFormat(Role role, String motDePasse) {
        switch (role) {
            case CLIENT -> {
                if (!motDePasse.matches("^\\d{4}$")) {
                    throw new IllegalStateException("Le mot de passe doit contenir exactement 4 chiffres");
                }
            }
            case GERANT, POMPISTE -> {
                if (!motDePasse.matches("^\\d{6}$")) {
                    throw new IllegalStateException("Le mot de passe doit contenir exactement 6 chiffres");
                }
            }
            default -> {
                if (motDePasse.length() < 4) {
                    throw new IllegalStateException("Le mot de passe doit contenir au moins 4 caractères");
                }
            }
        }
    }
}