package com.bufalari.payable.repository;

import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.enums.PayableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Removed unused BigDecimal import
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Spring Data JPA repository for Payable entities (with UUID primary key).
 * Repositório Spring Data JPA para entidades Payable (com chave primária UUID).
 */
@Repository
public interface PayableRepository extends JpaRepository<PayableEntity, UUID> { // <<<--- UUID ID Type

    // Methods inherited from JpaRepository (findById, existsById, deleteById, save, findAll, etc.)
    // now work with UUID.

    /**
     * Finds all payables associated with a specific project ID.
     * Assume project ID is still Long.
     */
    List<PayableEntity> findByProjectId(Long projectId);

    /**
     * Finds all payables associated with a specific supplier ID.
     * Assume supplier ID is still Long.
     */
    List<PayableEntity> findBySupplierId(Long supplierId);

    /**
     * Finds all payables with a specific status.
     * Encontra todas as contas a pagar com um status específico.
     */
    List<PayableEntity> findByStatus(PayableStatus status); // Status is an Enum, not related to ID type

    /**
     * Finds all payables with a due date before a certain date and not having one of the specified statuses.
     * Encontra todas as contas a pagar com data de vencimento anterior a uma certa data e que não estão nos status especificados.
     */
    List<PayableEntity> findByDueDateBeforeAndStatusNotIn(LocalDate date, Collection<PayableStatus> excludedStatuses);

    /**
     * Finds payables that have a status within the specified collection and are due within the specified date range.
     * Encontra contas a pagar que têm um status dentro da coleção especificada e vencem dentro do intervalo de datas especificado.
     */
    List<PayableEntity> findByDueDateBetweenAndStatusIn(LocalDate startDate, LocalDate endDate, Collection<PayableStatus> statuses);

    /**
     * Checks if any payable exists for a given supplier ID (Long) with a status NOT IN the specified collection.
     * Useful for checking if a supplier has "active" payables.
     * Verifica se existe alguma conta a pagar para um determinado ID de fornecedor (Long) com um status que NÃO ESTÁ na coleção especificada.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PayableEntity p " +
            "WHERE p.supplierId = :supplierId AND p.status NOT IN :inactiveStatuses")
    boolean existsBySupplierIdAndStatusNotIn(@Param("supplierId") Long supplierId,
                                             @Param("inactiveStatuses") Collection<PayableStatus> inactiveStatuses);


    // --- Potential future optimization queries ---
    // Consider adding queries that calculate sums directly in the database
    // if performance with loading entities becomes an issue for reporting.

    /**
     * Example: Directly calculates the sum of balance due for payables in specific statuses.
     * This avoids loading full entities into memory just for summation.
     */
    /*
    @Query("SELECT COALESCE(SUM(p.amountDue - COALESCE((SELECT SUM(pt.amountPaid) FROM PaymentTransactionEntity pt WHERE pt.payable = p), 0)), 0) " +
           "FROM PayableEntity p " +
           "WHERE p.status IN :statuses")
    BigDecimal sumBalanceDueByStatusIn(@Param("statuses") Collection<PayableStatus> statuses);
    */

    /**
     * Example: Directly calculates the sum of balance due for overdue payables.
     */
    /*
    @Query("SELECT COALESCE(SUM(p.amountDue - COALESCE((SELECT SUM(pt.amountPaid) FROM PaymentTransactionEntity pt WHERE pt.payable = p), 0)), 0) " +
           "FROM PayableEntity p " +
           "WHERE p.dueDate < :today AND p.status IN :unsettledStatuses")
    BigDecimal sumBalanceDueForOverdue(@Param("today") LocalDate today,
                                       @Param("unsettledStatuses") Collection<PayableStatus> unsettledStatuses);
    */

}