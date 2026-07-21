package cardoil.backend.repository;

import cardoil.backend.entity.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechargeRepository extends JpaRepository<Recharge, Long> {
    List<Recharge> findByCarteIdOrderByDateRechargeDesc(Long carteId);
    List<Recharge> findByCarte_Employe_Entreprise_IdOrderByDateRechargeDesc(Long entrepriseId);
}