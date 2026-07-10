package cardoil.backend.service.impl;

import cardoil.backend.dto.response.DashboardAdminCompagnieResponse;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminCompagnieService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCompagnieServiceImpl implements AdminCompagnieService {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;

    @Override
    public DashboardAdminCompagnieResponse getDashboard(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }

        Long compagnieId = utilisateur.getCompagnie().getId();

        return DashboardAdminCompagnieResponse.builder()
                .nomCompagnie(utilisateur.getCompagnie().getNom())
                .codeCompagnie(utilisateur.getCompagnie().getCode())
                .totalStations(stationRepository.countByCompagnieId(compagnieId))
                .stationsActives(stationRepository.countByCompagnieIdAndActif(compagnieId, true))
                .totalEmployes(utilisateurRepository.countByCompagnieId(compagnieId))
                .build();
    }
}