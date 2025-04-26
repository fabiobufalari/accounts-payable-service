// Path: src/main/java/com/bufalari/payable/repository/PayableRepository.java
package com.bufalari.payable.repository;

import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.enums.PayableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Payable entities.
 * Repositório Spring Data JPA para entidades Payable.
 */
@Repository
public interface PayableRepository extends JpaRepository<PayableEntity, Long> {

    /**
     * Finds all payables due within a specific date range and with certain statuses.
     * Encontra todas as contas a pagar vencendo em um intervalo de datas específico e com certos status.
     * @param startDate Start of the due date range. / Início do intervalo de vencimento.
     * @param endDate End of the due date range. / Fim do intervalo de vencimento.
     * @param statuses List of statuses to include. / Lista de status a incluir.
     * @return List of matching payables. / Lista de contas a pagar correspondentes.
     */
    List<PayableEntity> findByDueDateBetweenAndStatusIn(LocalDate startDate, LocalDate endDate, List<PayableStatus> statuses);

    /**
     * Finds all payables associated with a specific project ID.
     * Encontra todas as contas a pagar associadas a um ID de projeto específico.
     * @param projectId The project ID. / O ID do projeto.
     * @return List of payables for the project. / Lista de contas a pagar do projeto.
     */
    List<PayableEntity> findByProjectId(Long projectId);

    /**
     * Finds all payables associated with a specific supplier ID.
     * Encontra todas as contas a pagar associadas a um ID de fornecedor específico.
     * @param supplierId The supplier ID. / O ID do fornecedor.
     * @return List of payables for the supplier. / Lista de contas a pagar do fornecedor.
     */
    List<PayableEntity> findBySupplierId(Long supplierId);

    /**
     * Finds all payables with a due date before a certain date and not in the specified statuses (e.g., find overdue).
     * Encontra todas as contas a pagar com data de vencimento anterior a uma certa data e que não estão nos status especificados (ex: encontrar atrasadas).
     * @param date The date to compare the due date against. / A data para comparar o vencimento.
     * @param excludedStatuses List of statuses to exclude (e.g., PAID, CANCELED). / Lista de status a excluir (ex: PAGO, CANCELADO).
     * @return List of overdue payables. / Lista de contas a pagar atrasadas.
     */
    List<PayableEntity> findByDueDateBeforeAndStatusNotIn(LocalDate date, List<PayableStatus> excludedStatuses);

    /**
     * Checks if any active payable exists for a given supplier ID.
     * Verifica se existe alguma conta a pagar ativa para um determinado ID de fornecedor.
     * 'Active' typically means not PAID or CANCELED.
     * 'Ativa' tipicamente significa não PAGA ou CANCELADA.
     * @param supplierId The supplier ID. / O ID do fornecedor.
     * @param inactiveStatuses The list of statuses considered inactive. / A lista de status considerados inativos.
     * @return true if at least one active payable exists, false otherwise. / true se existir pelo menos uma conta a pagar ativa, false caso contrário.
     */
     @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PayableEntity p " +
            "WHERE p.supplierId = :supplierId AND p.status NOT IN :inactiveStatuses")
     boolean existsBySupplierIdAndStatusNotIn(@Param("supplierId") Long supplierId, @Param("inactiveStatuses") List<PayableStatus> inactiveStatuses);

}