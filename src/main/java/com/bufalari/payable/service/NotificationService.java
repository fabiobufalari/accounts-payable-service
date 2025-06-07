package com.bufalari.payable.service;

import com.bufalari.payable.entity.PayableApprovalEntity;
import com.bufalari.payable.entity.PayableEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Notification service for approval workflow communications
 * Serviço de notificação para comunicações do fluxo de aprovação
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final JavaMailSender mailSender;
    
    /**
     * Send approval request notification to approver
     * Enviar notificação de solicitação de aprovação para o aprovador
     * 
     * @param approval The approval entity / A entidade de aprovação
     */
    public void sendApprovalRequestNotification(PayableApprovalEntity approval) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(approval.getApproverEmail());
            message.setSubject("Payment Approval Required - Aprovação de Pagamento Necessária");
            
            String body = String.format(
                "Dear %s / Caro %s,\\n\\n" +
                "A payment requires your approval:\\n" +
                "Um pagamento requer sua aprovação:\\n\\n" +
                "Payable ID / ID do Pagamento: %s\\n" +
                "Amount / Valor: $%.2f CAD\\n" +
                "Supplier / Fornecedor: %s\\n" +
                "Description / Descrição: %s\\n" +
                "Approval Level / Nível de Aprovação: %s\\n\\n" +
                "Please review and approve in the system.\\n" +
                "Por favor, revise e aprove no sistema.\\n\\n" +
                "Best regards / Atenciosamente,\\n" +
                "Construction Hub Financial System",
                approval.getApproverName(),
                approval.getApproverName(),
                approval.getPayable().getId(),
                approval.getPayable().getAmountDue(),
                approval.getPayable().getSupplierId(), // This would be supplier name in real implementation
                approval.getPayable().getDescription(),
                approval.getApprovalLevel().getDescriptionEn()
            );
            
            message.setText(body);
            mailSender.send(message);
            
            log.info("Approval request notification sent to {} for approval {}", 
                    approval.getApproverEmail(), approval.getId());
            
        } catch (Exception e) {
            log.error("Failed to send approval request notification for approval {}: {}", 
                     approval.getId(), e.getMessage());
        }
    }
    
    /**
     * Send approval completed notification
     * Enviar notificação de aprovação concluída
     * 
     * @param payable The payable entity / A entidade de pagamento
     */
    public void sendApprovalCompletedNotification(PayableEntity payable) {
        try {
            // This would typically send to the requester and finance team
            // Por enquanto, apenas log
            log.info("Approval workflow completed for payable {} - amount: ${}", 
                    payable.getId(), payable.getAmountDue());
            
            // In real implementation, would send email to finance team
            // Na implementação real, enviaria email para a equipe financeira
            
        } catch (Exception e) {
            log.error("Failed to send approval completed notification for payable {}: {}", 
                     payable.getId(), e.getMessage());
        }
    }
    
    /**
     * Send approval rejected notification
     * Enviar notificação de aprovação rejeitada
     * 
     * @param payable The payable entity / A entidade de pagamento
     * @param reason Rejection reason / Motivo da rejeição
     */
    public void sendApprovalRejectedNotification(PayableEntity payable, String reason) {
        try {
            log.info("Approval workflow rejected for payable {} - reason: {}", 
                    payable.getId(), reason);
            
            // In real implementation, would send email to requester
            // Na implementação real, enviaria email para o solicitante
            
        } catch (Exception e) {
            log.error("Failed to send approval rejected notification for payable {}: {}", 
                     payable.getId(), e.getMessage());
        }
    }
    
    /**
     * Send approval escalation notification
     * Enviar notificação de escalação de aprovação
     * 
     * @param approval The approval entity / A entidade de aprovação
     */
    public void sendApprovalEscalationNotification(PayableApprovalEntity approval) {
        try {
            log.warn("Approval escalated for payable {} - approval level: {}", 
                    approval.getPayable().getId(), approval.getApprovalLevel());
            
            // In real implementation, would send email to higher-level approvers
            // Na implementação real, enviaria email para aprovadores de nível superior
            
        } catch (Exception e) {
            log.error("Failed to send approval escalation notification for approval {}: {}", 
                     approval.getId(), e.getMessage());
        }
    }
}

