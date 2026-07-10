package cardoil.backend.service.impl;

import cardoil.backend.dto.request.PaysRequest;
import cardoil.backend.dto.response.PaysResponse;
import cardoil.backend.entity.Pays;
import cardoil.backend.repository.PaysRepository;
import cardoil.backend.service.PaysService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaysServiceImpl implements PaysService {

    private final PaysRepository paysRepository;

    @Override
    public List<PaysResponse> getAll() {
        return paysRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaysResponse getById(Long id) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pays non trouvé avec l'id : " + id));
        return toResponse(pays);
    }

    @Override
    public PaysResponse create(PaysRequest request) {
        if (paysRepository.existsByCodeIso(request.getCodeIso())) {
            throw new IllegalArgumentException("Un pays avec ce code ISO existe déjà");
        }

        Pays pays = Pays.builder()
                .codeIso(request.getCodeIso().toUpperCase())
                .nom(request.getNom())
                .devise(request.getDevise())
                .indicatifTel(request.getIndicatifTel())
                .actif(request.isActif())
                .build();

        return toResponse(paysRepository.save(pays));
    }

    @Override
    public PaysResponse update(Long id, PaysRequest request) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pays non trouvé avec l'id : " + id));

        if (!pays.getCodeIso().equalsIgnoreCase(request.getCodeIso())
                && paysRepository.existsByCodeIso(request.getCodeIso())) {
            throw new IllegalArgumentException("Un pays avec ce code ISO existe déjà");
        }

        pays.setCodeIso(request.getCodeIso().toUpperCase());
        pays.setNom(request.getNom());
        pays.setDevise(request.getDevise());
        pays.setIndicatifTel(request.getIndicatifTel());
        pays.setActif(request.isActif());

        return toResponse(paysRepository.save(pays));
    }

    @Override
    public void delete(Long id) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pays non trouvé avec l'id : " + id));

        if (pays.getCompagnies() != null && !pays.getCompagnies().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer ce pays : des compagnies y sont rattachées");
        }

        paysRepository.delete(pays);
    }

    private PaysResponse toResponse(Pays pays) {
        return PaysResponse.builder()
                .id(pays.getId())
                .codeIso(pays.getCodeIso())
                .nom(pays.getNom())
                .devise(pays.getDevise())
                .indicatifTel(pays.getIndicatifTel())
                .actif(pays.isActif())
                .nombreCompagnies(pays.getCompagnies() != null ? pays.getCompagnies().size() : 0)
                .build();
    }
}