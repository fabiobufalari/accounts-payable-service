// Path: src/main/java/com/bufalari/payable/repository/PaymentTransactionRepository.java
package com.bufalari.payable.repository;

import com.bufalari.payable.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for PaymentTransaction entities.
 * Repositório Spring Data JPA para entidades PaymentTransaction.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    /**
     * Finds all payment transactions associated with a specific PayableEntity ID, ordered by date descending.
     * Encontra todas as transações de pagamento associadas a um ID de PayableEntity específico, ordenadas por data descendente.
     */
    List<PaymentTransactionEntity> findByPayableIdOrderByTransactionDateDesc(Long payableId);

    /**
     * Finds all payment transactions occurred within a specific date range.
     * Encontra todas as transações de pagamento ocorridas dentro de um intervalo de datas específico.
     */
    List<PaymentTransactionEntity> findByTransactionDateBetweenOrderByTransactionDateAsc(LocalDate startDate, LocalDate endDate);

}