package cardoil.backend.service.impl;

import cardoil.backend.dto.response.DashboardSuperAdminResponse;
import cardoil.backend.dto.response.TopCompanyResponse;
import cardoil.backend.repository.*;
import cardoil.backend.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {

    private final CompagnieRepository compagnieRepository;
    private final StationRepository stationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PaysRepository paysRepository;

    @Override
    public DashboardSuperAdminResponse getDashboard() {

        List<TopCompanyResponse> topCompanies = compagnieRepository.findAll().stream()
                .map(c -> TopCompanyResponse.builder()
                        .id(c.getId())
                        .nom(c.getNom())
                        .code(c.getCode())
                        .paysNom(c.getPays() != null ? c.getPays().getNom() : null)
                        .nombreStations(stationRepository.findByCompagnieId(c.getId()).size())
                        .actif(c.isActif())
                        .build())
                .sorted(Comparator.comparingInt(TopCompanyResponse::getNombreStations).reversed())
                .limit(5)
                .toList();

        return DashboardSuperAdminResponse.builder()
                .totalCompagnies(compagnieRepository.count())
                .compagniesActives(compagnieRepository.findByActif(true).size())
                .compagniesSuspendues(compagnieRepository.findByActif(false).size())
                .totalStations(stationRepository.count())
                .stationsActives(stationRepository.findByActif(true).size())
                .totalUtilisateurs(utilisateurRepository.count())
                .utilisateursActifs(utilisateurRepository.countByActif(true))
                .totalEntreprises(entrepriseRepository.count())
                .totalPays(paysRepository.count())
                .topCompanies(topCompanies)
                .build();
    }
}