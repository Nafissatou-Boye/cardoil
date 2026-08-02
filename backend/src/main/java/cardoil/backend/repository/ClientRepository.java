package cardoil.backend.repository;

import cardoil.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByTelephone(String telephone);
    boolean existsByTelephone(String telephone);

    // ✅ Versions insensibles à la casse — remplacent findByNomUtilisateur/
    // existsByNomUtilisateur (instruction de remplacement déjà donnée,
    // appliquée ici plutôt que de laisser les deux versions coexister).
    Optional<Client> findByNomUtilisateurIgnoreCase(String nomUtilisateur);
    boolean existsByNomUtilisateurIgnoreCase(String nomUtilisateur);

    // ✅ Nouveau — résolution du QR d'identité scanné par le pompiste/gérant
    // (TransactionServiceImpl.payerParQr).
    Optional<Client> findByQrCode(String qrCode);
}