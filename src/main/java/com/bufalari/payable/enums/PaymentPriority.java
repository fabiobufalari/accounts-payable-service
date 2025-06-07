package com.bufalari.payable.enums;

/**
 * Payment priority levels for construction industry operations
 * Níveis de prioridade de pagamento para operações da indústria de construção
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
public enum PaymentPriority {
    
    /**
     * Critical priority - Immediate payment required (safety, legal, emergency)
     * Prioridade crítica - Pagamento imediato necessário (segurança, legal, emergência)
     */
    CRITICAL("CRITICAL", "Critical Priority", "Prioridade Crítica", 0),
    
    /**
     * High priority - Payment within 24 hours (key suppliers, project deadlines)
     * Alta prioridade - Pagamento em 24 horas (fornecedores chave, prazos de projeto)
     */
    HIGH("HIGH", "High Priority", "Alta Prioridade", 1),
    
    /**
     * Medium priority - Payment within 3 days (standard operations)
     * Prioridade média - Pagamento em 3 dias (operações padrão)
     */
    MEDIUM("MEDIUM", "Medium Priority", "Prioridade Média", 3),
    
    /**
     * Low priority - Payment within 7 days (non-critical suppliers)
     * Baixa prioridade - Pagamento em 7 dias (fornecedores não críticos)
     */
    LOW("LOW", "Low Priority", "Baixa Prioridade", 7);
    
    private final String code;
    private final String descriptionEn;
    private final String descriptionPt;
    private final Integer maxDaysToPayment;
    
    PaymentPriority(String code, String descriptionEn, String descriptionPt, Integer maxDaysToPayment) {
        this.code = code;
        this.descriptionEn = descriptionEn;
        this.descriptionPt = descriptionPt;
        this.maxDaysToPayment = maxDaysToPayment;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescriptionEn() {
        return descriptionEn;
    }
    
    public String getDescriptionPt() {
        return descriptionPt;
    }
    
    public Integer getMaxDaysToPayment() {
        return maxDaysToPayment;
    }
}

