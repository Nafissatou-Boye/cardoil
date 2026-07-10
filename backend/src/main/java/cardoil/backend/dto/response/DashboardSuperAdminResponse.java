package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSuperAdminResponse {

    // Compagnies
    private long totalCompagnies;
    private long compagniesActives;
    private long compagniesSuspendues;

    // Stations
    private long totalStations;
    private long stationsActives;

    // Utilisateurs
    private long totalUtilisateurs;
    private long utilisateursActifs;

    // Entreprises
    private long totalEntreprises;

    // Pays
    private long totalPays;

    // Classement compagnies
    private List<TopCompanyResponse> topCompanies;
}