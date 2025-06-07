package com.bufalari.payable.entity;

import com.bufalari.payable.auditing.AuditableBaseEntity;
import com.bufalari.payable.enums.ApprovalLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an approval step in the payment workflow
 * Entidade representando uma etapa de aprovação no fluxo de trabalho de pagamento
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payable_approvals", indexes = {
    @Index(name = "idx_approval_payable_id", columnList = "payable_id"),
    @Index(name = "idx_approval_level", columnList = "approval_level"),
    @Index(name = "idx_approval_status", columnList = "approval_status")
})
public class PayableApprovalEntity extends AuditableBaseEntity {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payable_id", nullable = false)
    @NotNull
    private PayableEntity payable;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_level", nullable = false, length = 20)
    private ApprovalLevel approvalLevel;
    
    @NotNull
    @Column(name = "approver_user_id", nullable = false)
    private Long approverUserId;
    
    @Column(name = "approver_name", length = 100)
    private String approverName;
    
    @Column(name = "approver_email", length = 150)
    private String approverEmail;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "comments", length = 500)
    private String comments;
    
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;
    
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;
    
    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;
    
    @Column(name = "escalation_date")
    private LocalDateTime escalationDate;
    
    /**
     * Approval status enumeration
     * Enumeração de status de aprovação
     */
    public enum ApprovalStatus {
        PENDING("PENDING", "Pending Approval", "Aprovação Pendente"),
        APPROVED("APPROVED", "Approved", "Aprovado"),
        REJECTED("REJECTED", "Rejected", "Rejeitado"),
        ESCALATED("ESCALATED", "Escalated", "Escalado"),
        SKIPPED("SKIPPED", "Skipped", "Pulado");
        
        private final String code;
        private final String descriptionEn;
        private final String descriptionPt;
        
        ApprovalStatus(String code, String descriptionEn, String descriptionPt) {
            this.code = code;
            this.descriptionEn = descriptionEn;
            this.descriptionPt = descriptionPt;
        }
        
        public String getCode() { return code; }
        public String getDescriptionEn() { return descriptionEn; }
        public String getDescriptionPt() { return descriptionPt; }
    }
    
    /**
     * Mark approval as approved with timestamp and approver details
     * Marcar aprovação como aprovada com timestamp e detalhes do aprovador
     */
    public void approve(String approverName, String approverEmail, String comments) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvalDate = LocalDateTime.now();
        this.approverName = approverName;
        this.approverEmail = approverEmail;
        this.comments = comments;
    }
    
    /**
     * Mark approval as rejected with timestamp and approver details
     * Marcar aprovação como rejeitada com timestamp e detalhes do aprovador
     */
    public void reject(String approverName, String approverEmail, String comments) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvalDate = LocalDateTime.now();
        this.approverName = approverName;
        this.approverEmail = approverEmail;
        this.comments = comments;
    }
    
    /**
     * Mark approval as escalated due to timeout or other reasons
     * Marcar aprovação como escalada devido a timeout ou outras razões
     */
    public void escalate(String reason) {
        this.approvalStatus = ApprovalStatus.ESCALATED;
        this.escalationDate = LocalDateTime.now();
        this.comments = (this.comments != null ? this.comments + " | " : "") + "Escalated: " + reason;
    }
}

