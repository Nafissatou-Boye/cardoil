package cardoil.backend.service.impl;

import cardoil.backend.dto.request.SuperAdminProfilRequest;
import cardoil.backend.dto.response.SuperAdminProfilResponse;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.SuperAdminParametresService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SuperAdminParametresServiceImpl implements SuperAdminParametresService {

    private final UtilisateurRepository utilisateurRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public SuperAdminProfilResponse getProfil(String login) {
        Utilisateur utilisateur = getUtilisateur(login);
        return toResponse(utilisateur);
    }

    @Override
    public SuperAdminProfilResponse updateProfil(String login, SuperAdminProfilRequest request) {
        Utilisateur utilisateur = getUtilisateur(login);
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    private Utilisateur getUtilisateur(String login) {
        return utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
    }

    private SuperAdminProfilResponse toResponse(Utilisateur u) {
        return SuperAdminProfilResponse.builder()
                .id(u.getId())
                .login(u.getLogin())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .dateCreation(u.getDateCreation() != null
                        ? u.getDateCreation().format(FORMATTER) : null)
                .build();
    }
}