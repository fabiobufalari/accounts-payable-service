// Path: src/main/java/com/bufalari/payable/enums/PaymentMethod.java
package com.bufalari.payable.enums;

/**
 * Enum representing different methods of payment or receiving funds.
 * Enum representando diferentes métodos de pagamento ou recebimento de fundos.
 */
public enum PaymentMethod {
    CASH("Cash", "Dinheiro"),
    DEBIT_CARD("Debit Card", "Cartão de Débito"),
    CREDIT_CARD("Credit Card", "Cartão de Crédito"),
    E_TRANSFER("E-Transfer", "Transferência Eletrônica (Ex: Interac)"),
    BANK_TRANSFER("Bank Transfer / Wire", "Transferência Bancária / TED / DOC"),
    CHECK("Check", "Cheque"),
    VOUCHER("Voucher", "Vale / Voucher"),
    ONLINE_PAYMENT("Online Payment", "Pagamento Online (Ex: PayPal, Stripe)"),
    OTHER("Other", "Outro");

    private final String descriptionEn;
    private final String descriptionPt;

    PaymentMethod(String en, String pt) {
        this.descriptionEn = en;
        this.descriptionPt = pt;
    }

    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionPt() { return descriptionPt; }

    // Optional: Find enum by description (useful if receiving text from external systems)
    // Opcional: Encontrar enum pela descrição (útil se receber texto de sistemas externos)
    public static PaymentMethod fromDescription(String text) {
        for (PaymentMethod b : PaymentMethod.values()) {
            if (b.descriptionEn.equalsIgnoreCase(text) || b.descriptionPt.equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Return OTHER or throw exception if not found
        // Retorna OTHER ou lança exceção se não encontrado
        return OTHER;
        // throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}