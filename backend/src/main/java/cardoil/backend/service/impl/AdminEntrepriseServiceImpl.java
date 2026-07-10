package cardoil.backend.service.impl;

import cardoil.backend.dto.request.EntrepriseRequest;
import cardoil.backend.dto.response.AdminEntrepriseInfoResponse;
import cardoil.backend.dto.response.EntrepriseResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.EntrepriseRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminEntrepriseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEntrepriseServiceImpl implements AdminEntrepriseService {

    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Override
public AdminEntrepriseInfoResponse getAdmin(String login, Long entrepriseId) {
    Compagnie compagnie = getCompagnie(login);

    entrepriseRepository.findByIdAndCompagnieId(entrepriseId, compagnie.getId())
            .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));

    return utilisateurRepository.findByEntrepriseIdAndActif(entrepriseId, true)
            .map(u -> AdminEntrepriseInfoResponse.builder()
                    .id(u.getId())
                    .login(u.getLogin())
                    .nom(u.getNom())
                    .prenom(u.getPrenom())
                    .email(u.getEmail())
                    .actif(u.isActif())
                    .build())
            .orElse(null);
}

    @Override
    public List<EntrepriseResponse> getAll(String login) {
        Compagnie compagnie = getCompagnie(login);
        return entrepriseRepository.findByCompagnieIdOrderByNomAsc(compagnie.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public EntrepriseResponse create(String login, EntrepriseRequest request) {
        Compagnie compagnie = getCompagnie(login);

        if (entrepriseRepository.existsByCodeAndCompagnieId(request.getCode(), compagnie.getId())) {
            throw new IllegalArgumentException("Ce code est déjà utilisé");
        }

        Entreprise entreprise = Entreprise.builder()
                .nom(request.getNom())
                .code(request.getCode().toUpperCase())
                .secteurActivite(request.getSecteurActivite())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .actif(request.isActif())
                .compagnie(compagnie)
                .build();

        return toResponse(entrepriseRepository.save(entreprise));
    }

    @Override
    public EntrepriseResponse update(String login, Long id, EntrepriseRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Entreprise entreprise = entrepriseRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));

        if (!entreprise.getCode().equals(request.getCode().toUpperCase())
                && entrepriseRepository.existsByCodeAndCompagnieId(
                        request.getCode().toUpperCase(), compagnie.getId())) {
            throw new IllegalArgumentException("Ce code est déjà utilisé");
        }

        entreprise.setNom(request.getNom());
        entreprise.setCode(request.getCode().toUpperCase());
        entreprise.setSecteurActivite(request.getSecteurActivite());
        entreprise.setAdresse(request.getAdresse());
        entreprise.setTelephone(request.getTelephone());
        entreprise.setEmail(request.getEmail());
        entreprise.setActif(request.isActif());

        return toResponse(entrepriseRepository.save(entreprise));
    }

    @Override
    public void delete(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);
        Entreprise entreprise = entrepriseRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));
        entrepriseRepository.delete(entreprise);
    }

    @Override
    public EntrepriseResponse toggleActif(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);
        Entreprise entreprise = entrepriseRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));
        entreprise.setActif(!entreprise.isActif());
        return toResponse(entrepriseRepository.save(entreprise));
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée");
        }
        return utilisateur.getCompagnie();
    }

    private EntrepriseResponse toResponse(Entreprise e) {
        return EntrepriseResponse.builder()
                .id(e.getId())
                .nom(e.getNom())
                .code(e.getCode())
                .secteurActivite(e.getSecteurActivite())
                .adresse(e.getAdresse())
                .telephone(e.getTelephone())
                .email(e.getEmail())
                .actif(e.isActif())
                .dateCreation(e.getDateCreation() != null
                        ? e.getDateCreation().format(FORMATTER) : null)
                .compagnieNom(e.getCompagnie().getNom())
                .build();
    }
}