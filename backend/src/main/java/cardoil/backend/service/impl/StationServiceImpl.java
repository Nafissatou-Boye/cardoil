package cardoil.backend.service.impl;

import cardoil.backend.dto.request.StationRequest;
import cardoil.backend.dto.response.StationResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Station;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.StationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final CompagnieRepository compagnieRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<StationResponse> getAll() {
        return stationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StationResponse getById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station non trouvée avec l'id : " + id));
        return toResponse(station);
    }

    @Override
    public StationResponse create(StationRequest request) {
        Compagnie compagnie = compagnieRepository.findById(request.getCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée avec l'id : " + request.getCompagnieId()));

        Utilisateur gerant = null;
        if (request.getGerantId() != null) {
            gerant = utilisateurRepository.findById(request.getGerantId())
                    .orElseThrow(() -> new EntityNotFoundException("Gérant non trouvé avec l'id : " + request.getGerantId()));
        }

        Station station = Station.builder()
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .telephone(request.getTelephone())
                .actif(request.isActif())
                .compagnie(compagnie)
                .gerant(gerant)
                .build();

        return toResponse(stationRepository.save(station));
    }

    @Override
    public StationResponse update(Long id, StationRequest request) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station non trouvée avec l'id : " + id));

        Compagnie compagnie = compagnieRepository.findById(request.getCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Compagnie non trouvée avec l'id : " + request.getCompagnieId()));

        Utilisateur gerant = null;
        if (request.getGerantId() != null) {
            gerant = utilisateurRepository.findById(request.getGerantId())
                    .orElseThrow(() -> new EntityNotFoundException("Gérant non trouvé avec l'id : " + request.getGerantId()));
        }

        station.setNom(request.getNom());
        station.setAdresse(request.getAdresse());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setTelephone(request.getTelephone());
        station.setActif(request.isActif());
        station.setCompagnie(compagnie);
        station.setGerant(gerant);

        return toResponse(stationRepository.save(station));
    }

    @Override
    public void delete(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station non trouvée avec l'id : " + id));

        stationRepository.delete(station);
    }

    private StationResponse toResponse(Station station) {
        return StationResponse.builder()
                .id(station.getId())
                .nom(station.getNom())
                .adresse(station.getAdresse())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .telephone(station.getTelephone())
                .actif(station.isActif())
                .dateCreation(station.getDateCreation())
                .compagnie(StationResponse.CompagnieInfo.builder()
                        .id(station.getCompagnie().getId())
                        .nom(station.getCompagnie().getNom())
                        .code(station.getCompagnie().getCode())
                        .build())
                .gerantNom(station.getGerant() != null
                        ? station.getGerant().getPrenom() + " " + station.getGerant().getNom()
                        : null)
                .build();
    }
}