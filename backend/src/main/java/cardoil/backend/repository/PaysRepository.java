package cardoil.backend.repository;

import cardoil.backend.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaysRepository extends JpaRepository<Pays, Long> {
    Optional<Pays> findByCodeIso(String codeIso);
    boolean existsByCodeIso(String codeIso);
    
}