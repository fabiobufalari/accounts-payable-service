package com.bufalari.payable.enums;

/**
 * Advanced approval levels for payment authorization in Canadian construction industry
 * Níveis avançados de aprovação para autorização de pagamentos na indústria de construção canadense
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
public enum ApprovalLevel {
    
    /**
     * Automatic approval for small amounts (< $1,000 CAD)
     * Aprovação automática para valores pequenos (< $1.000 CAD)
     */
    AUTOMATIC("AUTOMATIC", "Automatic Approval", "Aprovação Automática", 1000.00),
    
    /**
     * Supervisor approval required ($1,000 - $10,000 CAD)
     * Aprovação de supervisor necessária ($1.000 - $10.000 CAD)
     */
    SUPERVISOR("SUPERVISOR", "Supervisor Approval", "Aprovação de Supervisor", 10000.00),
    
    /**
     * Manager approval required ($10,000 - $50,000 CAD)
     * Aprovação de gerente necessária ($10.000 - $50.000 CAD)
     */
    MANAGER("MANAGER", "Manager Approval", "Aprovação de Gerente", 50000.00),
    
    /**
     * Director approval required ($50,000 - $100,000 CAD)
     * Aprovação de diretor necessária ($50.000 - $100.000 CAD)
     */
    DIRECTOR("DIRECTOR", "Director Approval", "Aprovação de Diretor", 100000.00),
    
    /**
     * CFO approval required ($100,000 - $500,000 CAD)
     * Aprovação de CFO necessária ($100.000 - $500.000 CAD)
     */
    CFO("CFO", "CFO Approval", "Aprovação de CFO", 500000.00),
    
    /**
     * CEO approval required (> $500,000 CAD)
     * Aprovação de CEO necessária (> $500.000 CAD)
     */
    CEO("CEO", "CEO Approval", "Aprovação de CEO", Double.MAX_VALUE);
    
    private final String code;
    private final String descriptionEn;
    private final String descriptionPt;
    private final Double thresholdCAD;
    
    ApprovalLevel(String code, String descriptionEn, String descriptionPt, Double thresholdCAD) {
        this.code = code;
        this.descriptionEn = descriptionEn;
        this.descriptionPt = descriptionPt;
        this.thresholdCAD = thresholdCAD;
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
    
    public Double getThresholdCAD() {
        return thresholdCAD;
    }
    
    /**
     * Determine required approval level based on amount in CAD
     * Determinar nível de aprovação necessário baseado no valor em CAD
     * 
     * @param amountCAD Amount in Canadian Dollars / Valor em Dólares Canadenses
     * @return Required approval level / Nível de aprovação necessário
     */
    public static ApprovalLevel determineLevel(Double amountCAD) {
        if (amountCAD == null || amountCAD <= 0) {
            return AUTOMATIC;
        }
        
        for (ApprovalLevel level : values()) {
            if (amountCAD <= level.getThresholdCAD()) {
                return level;
            }
        }
        
        return CEO; // Fallback for extremely large amounts
    }
}

