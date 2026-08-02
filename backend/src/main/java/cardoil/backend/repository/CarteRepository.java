package cardoil.backend.repository;

import cardoil.backend.entity.Carte;
import cardoil.backend.entity.StatutCarte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarteRepository extends JpaRepository<Carte, Long> {
    Optional<Carte> findByEmployeId(Long employeId);
    boolean existsByEmployeId(Long employeId);
    boolean existsByNumeroCarte(String numeroCarte);
    List<Carte> findByEmploye_Entreprise_Id(Long entrepriseId);
    Optional<Carte> findByNumeroCarteIgnoreCaseAndEmploye_Entreprise_Id(String numeroCarte, Long entrepriseId);

    List<Carte> findByMontantDotationMensuelleIsNotNullAndDateRenouvellementAndStatut(
            Integer dateRenouvellement, StatutCarte statut);

    List<Carte> findByStatutAndSoldeLessThan(StatutCarte statut, BigDecimal seuil);

    // Recherche globale (non scopée à une entreprise) : utilisée par le Gérant en station,
    // où un employé de n'importe quelle entreprise partenaire peut se présenter.
    Optional<Carte> findByNumeroCarteIgnoreCase(String numeroCarte);

    List<Carte> findByDateRenouvellementAndStatut(Integer dateRenouvellement, StatutCarte statut);

    // ✅ Nouveau — résolution du QR d'identité scanné par le pompiste/gérant
    // (TransactionServiceImpl.payerParQr), et vérification d'unicité lors de
    // la génération (EmployeCompteServiceImpl.genererQrCode).
    Optional<Carte> findByCodeQr(String codeQr);
}