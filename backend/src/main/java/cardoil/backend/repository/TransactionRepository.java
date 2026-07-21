package cardoil.backend.repository;

import cardoil.backend.entity.StatutTransaction;
import cardoil.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // CA total par compagnie et période
    @Query("SELECT COALESCE(SUM(t.montant), 0) FROM Transaction t " +
           "WHERE t.station.compagnie.id = :compagnieId " +
           "AND t.dateTransaction BETWEEN :debut AND :fin " +
           "AND t.statut = cardoil.backend.entity.StatutTransaction.REUSSIE")
    BigDecimal sumCaByCompagnieAndPeriode(@Param("compagnieId") Long compagnieId,
                                           @Param("debut") LocalDateTime debut,
                                           @Param("fin") LocalDateTime fin);

    // Nb transactions par compagnie et période
    long countByStation_Compagnie_IdAndDateTransactionBetween(
            Long compagnieId, LocalDateTime debut, LocalDateTime fin);

    // Nb transactions par statut
    long countByStation_Compagnie_IdAndDateTransactionBetweenAndStatut(
            Long compagnieId, LocalDateTime debut, LocalDateTime fin, StatutTransaction statut);

    // CA par station et période
    @Query("SELECT COALESCE(SUM(t.montant), 0) FROM Transaction t " +
           "WHERE t.station.id = :stationId " +
           "AND t.dateTransaction BETWEEN :debut AND :fin " +
           "AND t.statut = cardoil.backend.entity.StatutTransaction.REUSSIE")
    BigDecimal sumCaByStationAndPeriode(@Param("stationId") Long stationId,
                                         @Param("debut") LocalDateTime debut,
                                         @Param("fin") LocalDateTime fin);

    // Nb transactions par station et période
    long countByStationIdAndDateTransactionBetween(
            Long stationId, LocalDateTime debut, LocalDateTime fin);

    // Dernières transactions par compagnie
    List<Transaction> findByStation_Compagnie_IdAndDateTransactionBetweenOrderByDateTransactionDesc(
            Long compagnieId, LocalDateTime debut, LocalDateTime fin);

            List<Transaction> findTop10ByStationIdOrderByDateTransactionDesc(Long stationId);

long countByStationIdAndDateTransactionBetweenAndStatut(
        Long stationId, LocalDateTime debut, LocalDateTime fin, StatutTransaction statut);

        boolean existsByReference(String reference);
}