package cardoil.backend.controller;

import cardoil.backend.dto.response.PompisteResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gerant/personnel")
@RequiredArgsConstructor
public class PersonnelController {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;

    @GetMapping
    @PreAuthorize("hasRole('GERANT')")
    public ResponseEntity<List<PompisteResponse>> monPersonnel(Authentication authentication) {
        Utilisateur operateur = utilisateurRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));

        Station station = stationRepository.findByGerantId(operateur.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune station assignée à ce gérant"));

        List<Utilisateur> pompistes = utilisateurRepository.findByStationIdAndRole(station.getId(), Role.POMPISTE);

        List<PompisteResponse> response = pompistes.stream()
                .map(p -> PompisteResponse.builder()
                        .id(p.getId())
                        .nom(p.getNom())
                        .prenom(p.getPrenom())
                        .telephone(p.getTelephone())
                        .role(p.getRole().name())
                        .actif(p.isActif())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }
}