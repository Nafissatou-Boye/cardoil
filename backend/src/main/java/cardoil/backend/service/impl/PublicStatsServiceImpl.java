package cardoil.backend.service.impl;

import cardoil.backend.dto.response.PublicStatsResponse;
import cardoil.backend.repository.CompagnieRepository;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.TransactionRepository;
import cardoil.backend.service.PublicStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// count() : méthode de base JpaRepository, sûre sur les deux repositories
// sans avoir besoin de connaître leur contenu complet.
@Service
@RequiredArgsConstructor
public class PublicStatsServiceImpl implements PublicStatsService {

    private final CompagnieRepository compagnieRepository;
    private final StationRepository stationRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public PublicStatsResponse getStats() {
        return PublicStatsResponse.builder()
                .totalCompagnies(compagnieRepository.count())
                .totalStations(stationRepository.count())
                .volumeTraite(transactionRepository.sumCaGlobal())
                .build();
    }
}