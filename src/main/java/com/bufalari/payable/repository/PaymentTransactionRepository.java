package com.bufalari.payable.repository;

import com.bufalari.payable.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Spring Data JPA repository for PaymentTransaction entities (with UUID primary key).
 * Repositório Spring Data JPA para entidades PaymentTransaction (com chave primária UUID).
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> { // <<<--- UUID ID Type

    // Methods inherited from JpaRepository (findById, existsById, deleteById, save, findAll, etc.)
    // now work with UUID.

    /**
     * Finds all payment transactions associated with a specific PayableEntity ID (UUID),
     * ordered by transaction date descending.
     * Encontra todas as transações de pagamento associadas a um ID de PayableEntity específico (UUID),
     * ordenadas por data da transação descendente.
     *
     * Note: Spring Data JPA matches 'PayableId' in the method name to the 'payable.id' field
     * in the PaymentTransactionEntity.
     */
    List<PaymentTransactionEntity> findByPayableIdOrderByTransactionDateDesc(UUID payableId); // <<<--- UUID Parameter

    /**
     * Finds all payment transactions that occurred within a specific date range,
     * ordered by transaction date ascending.
     * Encontra todas as transações de pagamento ocorridas dentro de um intervalo de datas específico,
     * ordenadas por data da transação ascendente.
     */
    List<PaymentTransactionEntity> findByTransactionDateBetweenOrderByTransactionDateAsc(LocalDate startDate, LocalDate endDate);

    // Could add more specific queries as needed, e.g., find by payableId and transaction date range.

}