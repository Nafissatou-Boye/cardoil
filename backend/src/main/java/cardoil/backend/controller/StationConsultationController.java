package cardoil.backend.controller;

import cardoil.backend.dto.response.StationConsultationResponse;
import cardoil.backend.entity.Station;
import cardoil.backend.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client/stations")
@RequiredArgsConstructor
public class StationConsultationController {

    private final StationRepository stationRepository;

    @GetMapping
    public ResponseEntity<List<StationConsultationResponse>> getStations(
            @RequestParam Long compagnieId) {
        List<Station> stations = stationRepository.findByCompagnieId(compagnieId);
        List<StationConsultationResponse> response = stations.stream()
                .filter(Station::isActif)
                .map(s -> StationConsultationResponse.builder()
                        .id(s.getId())
                        .nom(s.getNom())
                        .adresse(s.getAdresse())
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .telephone(s.getTelephone())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }
}