package cardoil.backend.service.impl;

import cardoil.backend.dto.request.StationAdminRequest;
import cardoil.backend.dto.response.StationResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Station;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminStationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStationServiceImpl implements AdminStationService {

    private final StationRepository stationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<StationResponse> getMesStations(String login) {
        Compagnie compagnie = getCompagnie(login);
        return stationRepository.findByCompagnieId(compagnie.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StationResponse create(String login, StationAdminRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Station station = Station.builder()
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .telephone(request.getTelephone())
                .actif(request.isActif())
                .compagnie(compagnie)
                .build();

        return toResponse(stationRepository.save(station));
    }

    @Override
    public StationResponse update(String login, Long id, StationAdminRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Station station = stationRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Station non trouvée ou n'appartenant pas à votre compagnie"));

        station.setNom(request.getNom());
        station.setAdresse(request.getAdresse());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setTelephone(request.getTelephone());
        station.setActif(request.isActif());

        return toResponse(stationRepository.save(station));
    }

    @Override
    public void delete(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);

        Station station = stationRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Station non trouvée ou n'appartenant pas à votre compagnie"));

        stationRepository.delete(station);
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }

        return utilisateur.getCompagnie();
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