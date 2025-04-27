// Path: src/main/java/com/bufalari/payable/repository/PayableRepository.java
package com.bufalari.payable.repository;

import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.enums.PayableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Spring Data JPA repository for Payable entities.
 * Repositório Spring Data JPA para entidades Payable.
 */
@Repository
public interface PayableRepository extends JpaRepository<PayableEntity, Long> {

    List<PayableEntity> findByProjectId(Long projectId);

    List<PayableEntity> findBySupplierId(Long supplierId);

    /**
     * Finds all payables with a specific status.
     * Encontra todas as contas a pagar com um status específico.
     */
    List<PayableEntity> findByStatus(PayableStatus status); // Added / Adicionado

    /**
     * Finds all payables with a due date before a certain date and not in the specified statuses.
     * Encontra todas as contas a pagar com data de vencimento anterior a uma certa data e que não estão nos status especificados.
     */
    List<PayableEntity> findByDueDateBeforeAndStatusNotIn(LocalDate date, Collection<PayableStatus> excludedStatuses);

    /**
     * Finds payables that are pending (or partially paid/overdue/in negotiation) and are due within the specified date range.
     * Encontra contas a pagar que estão pendentes (ou parcialmente pagas/atrasadas/em negociação) e vencem dentro do intervalo de datas especificado.
     */
    List<PayableEntity> findByDueDateBetweenAndStatusIn(LocalDate startDate, LocalDate endDate, Collection<PayableStatus> pendingStatuses);

    /**
     * Checks if any active payable exists for a given supplier ID.
     * Verifica se existe alguma conta a pagar ativa para um determinado ID de fornecedor.
     */
     @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PayableEntity p " +
            "WHERE p.supplierId = :supplierId AND p.status NOT IN :inactiveStatuses")
     boolean existsBySupplierIdAndStatusNotIn(@Param("supplierId") Long supplierId, @Param("inactiveStatuses") Collection<PayableStatus> inactiveStatuses);

    // Note: Methods for summary/sum queries can also be added here if complex logic is needed
    // Nota: Métodos para queries de resumo/soma também podem ser adicionados aqui se lógica complexa for necessária
    // Example:
    // @Query("SELECT new com.bufalari.payable.dto.PayableSummaryDTO(p.id, p.dueDate, p.amountDue, SUM(pt.amountPaid), p.status, MAX(pt.transactionDate)) " +
    //        "FROM PayableEntity p LEFT JOIN p.paymentTransactions pt " +
    //        "WHERE pt.transactionDate BETWEEN :startDate AND :endDate " + // Or filter based on p.paymentDate if you decide to keep it updated
    //        "GROUP BY p.id, p.dueDate, p.amountDue, p.status")
    // List<PayableSummaryDTO> findPaidSummariesInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}