package cardoil.backend.repository;

import cardoil.backend.entity.RechargeGroupee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechargeGroupeeRepository extends JpaRepository<RechargeGroupee, Long> {
    List<RechargeGroupee> findByEntrepriseIdOrderByDateExecutionDesc(Long entrepriseId);
}