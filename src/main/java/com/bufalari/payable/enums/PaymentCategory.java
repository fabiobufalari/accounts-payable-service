package com.bufalari.payable.enums;

/**
 * Payment categories specific to Canadian construction industry
 * Categorias de pagamento específicas para a indústria de construção canadense
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
public enum PaymentCategory {
    
    /**
     * Construction materials (lumber, concrete, steel, etc.)
     * Materiais de construção (madeira, concreto, aço, etc.)
     */
    MATERIALS("MATERIALS", "Construction Materials", "Materiais de Construção", 1.0),
    
    /**
     * Labor costs (wages, benefits, overtime)
     * Custos de mão de obra (salários, benefícios, horas extras)
     */
    LABOR("LABOR", "Labor Costs", "Custos de Mão de Obra", 1.2),
    
    /**
     * Equipment rental and purchase (heavy machinery, tools)
     * Aluguel e compra de equipamentos (máquinas pesadas, ferramentas)
     */
    EQUIPMENT("EQUIPMENT", "Equipment", "Equipamentos", 0.8),
    
    /**
     * Subcontractor payments (electrical, plumbing, HVAC)
     * Pagamentos de subempreiteiros (elétrica, encanamento, HVAC)
     */
    SUBCONTRACTOR("SUBCONTRACTOR", "Subcontractor", "Subempreiteiro", 1.5),
    
    /**
     * Professional services (architects, engineers, consultants)
     * Serviços profissionais (arquitetos, engenheiros, consultores)
     */
    PROFESSIONAL_SERVICES("PROFESSIONAL_SERVICES", "Professional Services", "Serviços Profissionais", 1.1),
    
    /**
     * Permits and regulatory fees
     * Licenças e taxas regulamentares
     */
    PERMITS("PERMITS", "Permits & Fees", "Licenças e Taxas", 2.0),
    
    /**
     * Insurance and bonding
     * Seguros e garantias
     */
    INSURANCE("INSURANCE", "Insurance & Bonding", "Seguros e Garantias", 1.3),
    
    /**
     * Emergency repairs and urgent work
     * Reparos de emergência e trabalho urgente
     */
    EMERGENCY("EMERGENCY", "Emergency", "Emergência", 2.0),
    
    /**
     * Utilities (electricity, water, gas)
     * Serviços públicos (eletricidade, água, gás)
     */
    UTILITIES("UTILITIES", "Utilities", "Serviços Públicos", 1.4),
    
    /**
     * Other miscellaneous expenses
     * Outras despesas diversas
     */
    OTHER("OTHER", "Other", "Outros", 1.0);
    
    private final String code;
    private final String descriptionEn;
    private final String descriptionPt;
    private final Double categoryMultiplier; // Used for approval threshold adjustments
    
    PaymentCategory(String code, String descriptionEn, String descriptionPt, Double categoryMultiplier) {
        this.code = code;
        this.descriptionEn = descriptionEn;
        this.descriptionPt = descriptionPt;
        this.categoryMultiplier = categoryMultiplier;
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
    
    public Double getCategoryMultiplier() {
        return categoryMultiplier;
    }
}

