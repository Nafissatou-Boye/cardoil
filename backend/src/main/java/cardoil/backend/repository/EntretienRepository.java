package cardoil.backend.repository;

import cardoil.backend.entity.Entretien;
import cardoil.backend.entity.StatutEntretien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntretienRepository extends JpaRepository<Entretien, Long> {
    List<Entretien> findByVehiculeId(Long vehiculeId);
    List<Entretien> findByGarageId(Long garageId);
    List<Entretien> findByStatut(StatutEntretien statut);
}