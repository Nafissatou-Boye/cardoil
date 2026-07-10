package cardoil.backend.service.impl;

import cardoil.backend.dto.request.CompagnieRequest;
import cardoil.backend.dto.response.CompagnieResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Pays;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.PaysRepository;
import cardoil.backend.service.CompagnieService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompagnieServiceImpl implements CompagnieService {

    private final CompagnieRepository compagnieRepository;
    private final PaysRepository paysRepository;

    @Override
    public List<CompagnieResponse> getAll() {
        return compagnieRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CompagnieResponse getById(Long id) {
        Compagnie compagnie = compagnieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée avec l'id : " + id));
        return toResponse(compagnie);
    }

    @Override
    public CompagnieResponse create(CompagnieRequest request) {
        if (compagnieRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Une compagnie avec ce code existe déjà");
        }

        Pays pays = paysRepository.findById(request.getPaysId())
                .orElseThrow(() -> new EntityNotFoundException("Pays non trouvé avec l'id : " + request.getPaysId()));

        Compagnie compagnie = Compagnie.builder()
                .nom(request.getNom())
                .code(request.getCode().toUpperCase())
                .logo(request.getLogo())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .actif(request.isActif())
                .pays(pays)
                .build();

        return toResponse(compagnieRepository.save(compagnie));
    }

    @Override
    public CompagnieResponse update(Long id, CompagnieRequest request) {
        Compagnie compagnie = compagnieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée avec l'id : " + id));

        if (!compagnie.getCode().equalsIgnoreCase(request.getCode())
                && compagnieRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Une compagnie avec ce code existe déjà");
        }

        Pays pays = paysRepository.findById(request.getPaysId())
                .orElseThrow(() -> new EntityNotFoundException("Pays non trouvé avec l'id : " + request.getPaysId()));

        compagnie.setNom(request.getNom());
        compagnie.setCode(request.getCode().toUpperCase());
        compagnie.setLogo(request.getLogo());
        compagnie.setAdresse(request.getAdresse());
        compagnie.setTelephone(request.getTelephone());
        compagnie.setEmail(request.getEmail());
        compagnie.setActif(request.isActif());
        compagnie.setPays(pays);

        return toResponse(compagnieRepository.save(compagnie));
    }

    @Override
    public void delete(Long id) {
        Compagnie compagnie = compagnieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée avec l'id : " + id));

        if (compagnie.getStations() != null && !compagnie.getStations().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer cette compagnie : des stations y sont rattachées");
        }

        compagnieRepository.delete(compagnie);
    }

    private CompagnieResponse toResponse(Compagnie compagnie) {
        return CompagnieResponse.builder()
                .id(compagnie.getId())
                .nom(compagnie.getNom())
                .code(compagnie.getCode())
                .logo(compagnie.getLogo())
                .adresse(compagnie.getAdresse())
                .telephone(compagnie.getTelephone())
                .email(compagnie.getEmail())
                .actif(compagnie.isActif())
                .dateCreation(compagnie.getDateCreation())
                .pays(CompagnieResponse.PaysInfo.builder()
                        .id(compagnie.getPays().getId())
                        .nom(compagnie.getPays().getNom())
                        .codeIso(compagnie.getPays().getCodeIso())
                        .build())
                .nombreStations(compagnie.getStations() != null ? compagnie.getStations().size() : 0)
                .adminNom(compagnie.getAdmin() != null
                        ? compagnie.getAdmin().getPrenom() + " " + compagnie.getAdmin().getNom()
                        : null)
                .build();
    }
}