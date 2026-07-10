package cardoil.backend.service.impl;

import cardoil.backend.dto.request.CadeauRequest;
import cardoil.backend.dto.response.CadeauResponse;
import cardoil.backend.entity.Cadeau;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CadeauRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminCadeauService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCadeauServiceImpl implements AdminCadeauService {

    private final CadeauRepository cadeauRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<CadeauResponse> getAll(String login) {
        Compagnie compagnie = getCompagnie(login);
        return cadeauRepository.findByCompagnieIdOrderByNomAsc(compagnie.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public CadeauResponse create(String login, CadeauRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Cadeau cadeau = Cadeau.builder()
                .nom(request.getNom())
                .type(request.getType())
                .coutEnPoints(request.getCoutEnPoints())
                .stockDisponible(request.getStockDisponible())
                .image(request.getImage())
                .descriptionLongue(request.getDescriptionLongue())
                .dateExpiration(request.getDateExpiration())
                .actif(request.isActif())
                .compagnie(compagnie)
                .build();

        return toResponse(cadeauRepository.save(cadeau));
    }

    @Override
    public CadeauResponse update(String login, Long id, CadeauRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Cadeau cadeau = cadeauRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cadeau non trouvé"));

        cadeau.setNom(request.getNom());
        cadeau.setType(request.getType());
        cadeau.setCoutEnPoints(request.getCoutEnPoints());
        cadeau.setStockDisponible(request.getStockDisponible());
        cadeau.setImage(request.getImage());
        cadeau.setDescriptionLongue(request.getDescriptionLongue());
        cadeau.setDateExpiration(request.getDateExpiration());
        cadeau.setActif(request.isActif());

        return toResponse(cadeauRepository.save(cadeau));
    }

    @Override
    public void delete(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);
        Cadeau cadeau = cadeauRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cadeau non trouvé"));
        cadeauRepository.delete(cadeau);
    }

    @Override
    public CadeauResponse toggleActif(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);
        Cadeau cadeau = cadeauRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cadeau non trouvé"));
        cadeau.setActif(!cadeau.isActif());
        return toResponse(cadeauRepository.save(cadeau));
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée");
        }
        return utilisateur.getCompagnie();
    }

    private CadeauResponse toResponse(Cadeau c) {
        return CadeauResponse.builder()
                .id(c.getId())
                .nom(c.getNom())
                .type(c.getType().name())
                .coutEnPoints(c.getCoutEnPoints())
                .stockDisponible(c.getStockDisponible())
                .image(c.getImage())
                .descriptionLongue(c.getDescriptionLongue())
                .dateExpiration(c.getDateExpiration() != null
                        ? c.getDateExpiration().toString() : null)
                .actif(c.isActif())
                .illimite(c.getStockDisponible() == 0)
                .build();
    }
}