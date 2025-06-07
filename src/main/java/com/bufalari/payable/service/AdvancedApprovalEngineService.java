package com.bufalari.payable.service;

import com.bufalari.payable.entity.PayableApprovalEntity;
import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.enums.ApprovalLevel;
import com.bufalari.payable.enums.PaymentCategory;
import com.bufalari.payable.enums.PaymentPriority;
import com.bufalari.payable.enums.RiskLevel;
import com.bufalari.payable.repository.PayableApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Advanced approval engine service for multi-level payment authorization
 * Serviço de motor de aprovação avançado para autorização de pagamento multinível
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdvancedApprovalEngineService {
    
    private final PayableApprovalRepository approvalRepository;
    private final NotificationService notificationService;
    
    // Approval thresholds in CAD / Limites de aprovação em CAD
    private static final Map<ApprovalLevel, Double> APPROVAL_THRESHOLDS = Map.of(
        ApprovalLevel.AUTOMATIC, 1000.0,
        ApprovalLevel.SUPERVISOR, 10000.0,
        ApprovalLevel.MANAGER, 50000.0,
        ApprovalLevel.DIRECTOR, 100000.0,
        ApprovalLevel.CFO, 500000.0,
        ApprovalLevel.CEO, Double.MAX_VALUE
    );
    
    // Category-specific multipliers / Multiplicadores específicos por categoria
    private static final Map<PaymentCategory, Double> CATEGORY_MULTIPLIERS = Map.of(
        PaymentCategory.MATERIALS, 1.0,
        PaymentCategory.LABOR, 1.2,
        PaymentCategory.EQUIPMENT, 0.8,
        PaymentCategory.SUBCONTRACTOR, 1.5,
        PaymentCategory.PROFESSIONAL_SERVICES, 1.1,
        PaymentCategory.PERMITS, 2.0,
        PaymentCategory.INSURANCE, 1.3,
        PaymentCategory.EMERGENCY, 2.0,
        PaymentCategory.UTILITIES, 1.4,
        PaymentCategory.OTHER, 1.0
    );
    
    /**
     * Determine required approval level based on amount, risk, category, and priority
     * Determinar nível de aprovação necessário baseado em valor, risco, categoria e prioridade
     * 
     * @param payable The payable entity / A entidade de pagamento
     * @param category Payment category / Categoria de pagamento
     * @param riskLevel Supplier risk level / Nível de risco do fornecedor
     * @param priority Payment priority / Prioridade de pagamento
     * @return Required approval level / Nível de aprovação necessário
     */
    public ApprovalLevel determineApprovalLevel(PayableEntity payable, 
                                              PaymentCategory category, 
                                              RiskLevel riskLevel, 
                                              PaymentPriority priority) {
        
        BigDecimal amount = payable.getAmountDue();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ApprovalLevel.AUTOMATIC;
        }
        
        // Apply risk adjustment / Aplicar ajuste de risco
        double adjustedAmount = amount.doubleValue() / riskLevel.getRiskMultiplier();
        
        // Apply category-specific adjustments / Aplicar ajustes específicos por categoria
        double categoryMultiplier = CATEGORY_MULTIPLIERS.getOrDefault(category, 1.0);
        double finalAmount = adjustedAmount * categoryMultiplier;
        
        // Priority adjustments / Ajustes de prioridade
        if (priority == PaymentPriority.CRITICAL) {
            finalAmount *= 2.0; // Critical payments require higher approval
        } else if (priority == PaymentPriority.HIGH) {
            finalAmount *= 1.5;
        }
        
        // Determine approval level / Determinar nível de aprovação
        for (ApprovalLevel level : ApprovalLevel.values()) {
            Double threshold = APPROVAL_THRESHOLDS.get(level);
            if (threshold != null && finalAmount <= threshold) {
                log.info("Determined approval level {} for payable {} with adjusted amount ${}", 
                        level, payable.getId(), finalAmount);
                return level;
            }
        }
        
        return ApprovalLevel.CEO; // Fallback for extremely large amounts
    }
    
    /**
     * Create complete approval workflow for a payable
     * Criar fluxo de trabalho de aprovação completo para um pagamento
     * 
     * @param payable The payable entity / A entidade de pagamento
     * @param requiredLevel Required approval level / Nível de aprovação necessário
     * @return List of approval entities / Lista de entidades de aprovação
     */
    public List<PayableApprovalEntity> createApprovalWorkflow(PayableEntity payable, 
                                                            ApprovalLevel requiredLevel) {
        
        List<PayableApprovalEntity> approvals = new ArrayList<>();
        
        // Get approval workflow sequence / Obter sequência do fluxo de aprovação
        List<ApprovalLevel> workflow = getApprovalWorkflow(requiredLevel);
        
        for (int i = 0; i < workflow.size(); i++) {
            ApprovalLevel level = workflow.get(i);
            
            PayableApprovalEntity approval = PayableApprovalEntity.builder()
                .payable(payable)
                .approvalLevel(level)
                .approvalStatus(PayableApprovalEntity.ApprovalStatus.PENDING)
                .sequenceOrder(i + 1)
                .isRequired(true)
                .notificationSent(false)
                .approverUserId(getApproverForLevel(level))
                .build();
            
            approvals.add(approval);
        }
        
        // Save all approvals / Salvar todas as aprovações
        List<PayableApprovalEntity> savedApprovals = approvalRepository.saveAll(approvals);
        
        // Send notification for first approval / Enviar notificação para primeira aprovação
        if (!savedApprovals.isEmpty()) {
            sendApprovalNotification(savedApprovals.get(0));
        }
        
        log.info("Created approval workflow with {} steps for payable {}", 
                savedApprovals.size(), payable.getId());
        
        return savedApprovals;
    }
    
    /**
     * Get the complete approval workflow for a given level
     * Obter o fluxo de aprovação completo para um determinado nível
     * 
     * @param approvalLevel Required approval level / Nível de aprovação necessário
     * @return List of approval levels in sequence / Lista de níveis de aprovação em sequência
     */
    private List<ApprovalLevel> getApprovalWorkflow(ApprovalLevel approvalLevel) {
        List<ApprovalLevel> workflow = new ArrayList<>();
        
        switch (approvalLevel) {
            case AUTOMATIC:
                // No approvals needed / Nenhuma aprovação necessária
                break;
            case SUPERVISOR:
                workflow.add(ApprovalLevel.SUPERVISOR);
                break;
            case MANAGER:
                workflow.add(ApprovalLevel.SUPERVISOR);
                workflow.add(ApprovalLevel.MANAGER);
                break;
            case DIRECTOR:
                workflow.add(ApprovalLevel.SUPERVISOR);
                workflow.add(ApprovalLevel.MANAGER);
                workflow.add(ApprovalLevel.DIRECTOR);
                break;
            case CFO:
                workflow.add(ApprovalLevel.SUPERVISOR);
                workflow.add(ApprovalLevel.MANAGER);
                workflow.add(ApprovalLevel.DIRECTOR);
                workflow.add(ApprovalLevel.CFO);
                break;
            case CEO:
                workflow.add(ApprovalLevel.SUPERVISOR);
                workflow.add(ApprovalLevel.MANAGER);
                workflow.add(ApprovalLevel.DIRECTOR);
                workflow.add(ApprovalLevel.CFO);
                workflow.add(ApprovalLevel.CEO);
                break;
        }
        
        return workflow;
    }
    
    /**
     * Process approval decision (approve or reject)
     * Processar decisão de aprovação (aprovar ou rejeitar)
     * 
     * @param approvalId Approval entity ID / ID da entidade de aprovação
     * @param approved Whether approved or rejected / Se aprovado ou rejeitado
     * @param approverUserId ID of the approver / ID do aprovador
     * @param approverName Name of the approver / Nome do aprovador
     * @param approverEmail Email of the approver / Email do aprovador
     * @param comments Approval comments / Comentários da aprovação
     * @return Updated approval entity / Entidade de aprovação atualizada
     */
    public PayableApprovalEntity processApprovalDecision(String approvalId, 
                                                       boolean approved,
                                                       Long approverUserId,
                                                       String approverName,
                                                       String approverEmail,
                                                       String comments) {
        
        PayableApprovalEntity approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + approvalId));
        
        // Verify approver authorization / Verificar autorização do aprovador
        if (!approval.getApproverUserId().equals(approverUserId)) {
            throw new IllegalArgumentException("User not authorized to approve this request");
        }
        
        // Process decision / Processar decisão
        if (approved) {
            approval.approve(approverName, approverEmail, comments);
            log.info("Approval {} approved by user {}", approvalId, approverUserId);
            
            // Check if this completes the workflow / Verificar se isso completa o fluxo
            processNextApprovalStep(approval.getPayable());
            
        } else {
            approval.reject(approverName, approverEmail, comments);
            log.info("Approval {} rejected by user {}", approvalId, approverUserId);
            
            // Reject entire workflow / Rejeitar todo o fluxo
            rejectApprovalWorkflow(approval.getPayable(), "Rejected at " + approval.getApprovalLevel());
        }
        
        return approvalRepository.save(approval);
    }
    
    /**
     * Process next step in approval workflow
     * Processar próxima etapa no fluxo de aprovação
     * 
     * @param payable The payable entity / A entidade de pagamento
     */
    private void processNextApprovalStep(PayableEntity payable) {
        List<PayableApprovalEntity> approvals = approvalRepository.findByPayableOrderBySequenceOrder(payable);
        
        // Find next pending approval / Encontrar próxima aprovação pendente
        PayableApprovalEntity nextApproval = approvals.stream()
            .filter(a -> a.getApprovalStatus() == PayableApprovalEntity.ApprovalStatus.PENDING)
            .findFirst()
            .orElse(null);
        
        if (nextApproval != null) {
            // Send notification for next approval / Enviar notificação para próxima aprovação
            sendApprovalNotification(nextApproval);
        } else {
            // All approvals completed / Todas as aprovações concluídas
            completeApprovalWorkflow(payable);
        }
    }
    
    /**
     * Complete approval workflow and mark payable as approved
     * Completar fluxo de aprovação e marcar pagamento como aprovado
     * 
     * @param payable The payable entity / A entidade de pagamento
     */
    private void completeApprovalWorkflow(PayableEntity payable) {
        // Update payable status to approved / Atualizar status do pagamento para aprovado
        // This would typically update a status field in PayableEntity
        
        log.info("Approval workflow completed for payable {}", payable.getId());
        
        // Send completion notification / Enviar notificação de conclusão
        notificationService.sendApprovalCompletedNotification(payable);
    }
    
    /**
     * Reject entire approval workflow
     * Rejeitar todo o fluxo de aprovação
     * 
     * @param payable The payable entity / A entidade de pagamento
     * @param reason Rejection reason / Motivo da rejeição
     */
    private void rejectApprovalWorkflow(PayableEntity payable, String reason) {
        List<PayableApprovalEntity> approvals = approvalRepository.findByPayableOrderBySequenceOrder(payable);
        
        // Mark all pending approvals as skipped / Marcar todas as aprovações pendentes como puladas
        approvals.stream()
            .filter(a -> a.getApprovalStatus() == PayableApprovalEntity.ApprovalStatus.PENDING)
            .forEach(a -> {
                a.setApprovalStatus(PayableApprovalEntity.ApprovalStatus.SKIPPED);
                a.setComments("Workflow rejected: " + reason);
            });
        
        approvalRepository.saveAll(approvals);
        
        log.info("Approval workflow rejected for payable {}: {}", payable.getId(), reason);
        
        // Send rejection notification / Enviar notificação de rejeição
        notificationService.sendApprovalRejectedNotification(payable, reason);
    }
    
    /**
     * Send approval notification to approver
     * Enviar notificação de aprovação para o aprovador
     * 
     * @param approval The approval entity / A entidade de aprovação
     */
    private void sendApprovalNotification(PayableApprovalEntity approval) {
        if (!approval.getNotificationSent()) {
            notificationService.sendApprovalRequestNotification(approval);
            approval.setNotificationSent(true);
            approvalRepository.save(approval);
        }
    }
    
    /**
     * Get approver user ID for a given approval level
     * Obter ID do usuário aprovador para um determinado nível de aprovação
     * 
     * @param level Approval level / Nível de aprovação
     * @return Approver user ID / ID do usuário aprovador
     */
    private Long getApproverForLevel(ApprovalLevel level) {
        // This would typically query a user management service or database
        // For now, returning mock IDs / Por enquanto, retornando IDs fictícios
        return switch (level) {
            case SUPERVISOR -> 1001L;
            case MANAGER -> 1002L;
            case DIRECTOR -> 1003L;
            case CFO -> 1004L;
            case CEO -> 1005L;
            default -> 1000L; // Default approver
        };
    }
    
    /**
     * Check for escalation of pending approvals
     * Verificar escalação de aprovações pendentes
     * 
     * This method should be called by a scheduled job
     * Este método deve ser chamado por um job agendado
     */
    public void checkApprovalEscalations() {
        LocalDateTime escalationThreshold = LocalDateTime.now().minusHours(24);
        
        List<PayableApprovalEntity> pendingApprovals = approvalRepository
            .findPendingApprovalsOlderThan(escalationThreshold);
        
        for (PayableApprovalEntity approval : pendingApprovals) {
            approval.escalate("24-hour timeout exceeded");
            approvalRepository.save(approval);
            
            // Send escalation notification / Enviar notificação de escalação
            notificationService.sendApprovalEscalationNotification(approval);
            
            log.warn("Escalated approval {} due to timeout", approval.getId());
        }
    }
}

