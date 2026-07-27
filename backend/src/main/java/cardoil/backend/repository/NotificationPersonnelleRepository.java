// NotificationPersonnelleRepository.java
package cardoil.backend.repository;

import cardoil.backend.entity.NotificationPersonnelle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPersonnelleRepository extends JpaRepository<NotificationPersonnelle, Long> {
    List<NotificationPersonnelle> findByDestinataireIdOrderByDateCreationDesc(Long destinataireId);
    long countByDestinataireIdAndLu(Long destinataireId, boolean lu);
    Optional<NotificationPersonnelle> findByIdAndDestinataireId(Long id, Long destinataireId);
    List<NotificationPersonnelle> findByDestinataireIdAndLu(Long destinataireId, boolean lu);
}