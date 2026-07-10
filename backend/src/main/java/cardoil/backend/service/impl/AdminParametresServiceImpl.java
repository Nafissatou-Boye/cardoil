package cardoil.backend.service.impl;

import cardoil.backend.dto.request.CompagnieProfilRequest;
import cardoil.backend.dto.response.CompagnieProfilResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminParametresService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AdminParametresServiceImpl implements AdminParametresService {

    private final UtilisateurRepository utilisateurRepository;
    private final CompagnieRepository compagnieRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public CompagnieProfilResponse getProfil(String login) {
        Compagnie compagnie = getCompagnie(login);
        return toResponse(compagnie);
    }

    @Override
    public CompagnieProfilResponse updateProfil(String login, CompagnieProfilRequest request) {
        Compagnie compagnie = getCompagnie(login);

        compagnie.setAdresse(request.getAdresse());
        compagnie.setTelephone(request.getTelephone());
        compagnie.setEmail(request.getEmail());
        compagnie.setLogo(request.getLogo());

        return toResponse(compagnieRepository.save(compagnie));
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }

        return utilisateur.getCompagnie();
    }

    private CompagnieProfilResponse toResponse(Compagnie compagnie) {
        return CompagnieProfilResponse.builder()
                .id(compagnie.getId())
                .nom(compagnie.getNom())
                .code(compagnie.getCode())
                .adresse(compagnie.getAdresse())
                .telephone(compagnie.getTelephone())
                .email(compagnie.getEmail())
                .logo(compagnie.getLogo())
                .paysNom(compagnie.getPays() != null ? compagnie.getPays().getNom() : null)
                .dateCreation(compagnie.getDateCreation() != null
                        ? compagnie.getDateCreation().format(FORMATTER) : null)
                .build();
    }
}