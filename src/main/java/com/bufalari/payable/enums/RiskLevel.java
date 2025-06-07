package com.bufalari.payable.enums;

/**
 * Risk assessment levels for suppliers in construction industry
 * Níveis de avaliação de risco para fornecedores na indústria de construção
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
public enum RiskLevel {
    
    /**
     * Low risk - Established suppliers with excellent payment history
     * Baixo risco - Fornecedores estabelecidos com excelente histórico de pagamento
     */
    LOW("LOW", "Low Risk", "Baixo Risco", 1.0),
    
    /**
     * Medium risk - Regular suppliers with good payment history
     * Risco médio - Fornecedores regulares com bom histórico de pagamento
     */
    MEDIUM("MEDIUM", "Medium Risk", "Risco Médio", 0.7),
    
    /**
     * High risk - New suppliers or those with payment issues
     * Alto risco - Novos fornecedores ou aqueles com problemas de pagamento
     */
    HIGH("HIGH", "High Risk", "Alto Risco", 0.5),
    
    /**
     * Critical risk - Suppliers with significant payment or legal issues
     * Risco crítico - Fornecedores com problemas significativos de pagamento ou legais
     */
    CRITICAL("CRITICAL", "Critical Risk", "Risco Crítico", 0.3);
    
    private final String code;
    private final String descriptionEn;
    private final String descriptionPt;
    private final Double riskMultiplier; // Used for approval threshold adjustments
    
    RiskLevel(String code, String descriptionEn, String descriptionPt, Double riskMultiplier) {
        this.code = code;
        this.descriptionEn = descriptionEn;
        this.descriptionPt = descriptionPt;
        this.riskMultiplier = riskMultiplier;
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
    
    public Double getRiskMultiplier() {
        return riskMultiplier;
    }
}

