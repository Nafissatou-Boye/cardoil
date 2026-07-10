package cardoil.backend.repository;

import cardoil.backend.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    List<Vehicule> findByUtilisateurId(Long utilisateurId);
    List<Vehicule> findByEmployeId(Long employeId);
    Optional<Vehicule> findByImmatriculation(String immatriculation);
}