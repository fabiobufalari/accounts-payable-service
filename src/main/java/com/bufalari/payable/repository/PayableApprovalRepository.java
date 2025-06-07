package com.bufalari.payable.repository;

import com.bufalari.payable.entity.PayableApprovalEntity;
import com.bufalari.payable.entity.PayableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for PayableApprovalEntity operations
 * Repositório para operações de PayableApprovalEntity
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
@Repository
public interface PayableApprovalRepository extends JpaRepository<PayableApprovalEntity, String> {
    
    /**
     * Find all approvals for a payable ordered by sequence
     * Encontrar todas as aprovações para um pagamento ordenadas por sequência
     * 
     * @param payable The payable entity / A entidade de pagamento
     * @return List of approvals / Lista de aprovações
     */
    List<PayableApprovalEntity> findByPayableOrderBySequenceOrder(PayableEntity payable);
    
    /**
     * Find pending approvals older than specified date for escalation
     * Encontrar aprovações pendentes mais antigas que a data especificada para escalação
     * 
     * @param threshold Date threshold / Limite de data
     * @return List of pending approvals / Lista de aprovações pendentes
     */
    @Query("SELECT a FROM PayableApprovalEntity a WHERE a.approvalStatus = 'PENDING' " +
           "AND a.createdAt < :threshold AND a.escalationDate IS NULL")
    List<PayableApprovalEntity> findPendingApprovalsOlderThan(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Find approvals by approver user ID and status
     * Encontrar aprovações por ID do usuário aprovador e status
     * 
     * @param approverUserId Approver user ID / ID do usuário aprovador
     * @param status Approval status / Status da aprovação
     * @return List of approvals / Lista de aprovações
     */
    List<PayableApprovalEntity> findByApproverUserIdAndApprovalStatus(
        Long approverUserId, 
        PayableApprovalEntity.ApprovalStatus status
    );
    
    /**
     * Count pending approvals for a specific approver
     * Contar aprovações pendentes para um aprovador específico
     * 
     * @param approverUserId Approver user ID / ID do usuário aprovador
     * @return Count of pending approvals / Contagem de aprovações pendentes
     */
    @Query("SELECT COUNT(a) FROM PayableApprovalEntity a WHERE a.approverUserId = :approverUserId " +
           "AND a.approvalStatus = 'PENDING'")
    Long countPendingApprovalsByApprover(@Param("approverUserId") Long approverUserId);
    
    /**
     * Find approvals requiring escalation
     * Encontrar aprovações que requerem escalação
     * 
     * @return List of approvals requiring escalation / Lista de aprovações que requerem escalação
     */
    @Query("SELECT a FROM PayableApprovalEntity a WHERE a.approvalStatus = 'PENDING' " +
           "AND a.createdAt < :threshold AND a.escalationDate IS NULL")
    List<PayableApprovalEntity> findApprovalsRequiringEscalation(@Param("threshold") LocalDateTime threshold);
}

