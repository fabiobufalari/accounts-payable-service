// Path: src/main/java/com/bufalari/payable/enums/PayableStatus.java
package com.bufalari.payable.enums;

/**
 * Enum representing the possible statuses of an account payable.
 * Enum representando os possíveis status de uma conta a pagar.
 */
public enum PayableStatus {
    PENDING("Pending", "Pendente"), // Waiting for payment / Aguardando pagamento
    PAID("Paid", "Pago"),           // Payment completed / Pagamento concluído
    PARTIALLY_PAID("Partially Paid", "Parcialmente Pago"), // Partial payment made / Pagamento parcial realizado
    OVERDUE("Overdue", "Atrasado"), // Past due date, not fully paid / Vencido, não totalmente pago
    CANCELED("Canceled", "Cancelado"), // Payable canceled before payment / Conta a pagar cancelada antes do pagamento
    IN_NEGOTIATION("In Negotiation", "Em Negociação"); // Payment terms being negotiated / Termos de pagamento sendo negociados

    private final String descriptionEn;
    private final String descriptionPt;

    PayableStatus(String en, String pt) {
        this.descriptionEn = en;
        this.descriptionPt = pt;
    }

    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionPt() { return descriptionPt; }
}