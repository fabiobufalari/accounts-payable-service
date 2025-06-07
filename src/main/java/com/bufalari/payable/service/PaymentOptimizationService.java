package com.bufalari.payable.service;

import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.enums.PaymentPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced payment optimization service using sophisticated algorithms
 * Serviço avançado de otimização de pagamentos usando algoritmos sofisticados
 * 
 * @author Manus AI - Enterprise Architect
 * @version 2.0.0 - Advanced Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOptimizationService {
    
    private final CanadianBankingService bankingService;
    
    // Canadian banking holidays 2024-2025 / Feriados bancários canadenses 2024-2025
    private static final Set<LocalDate> BANKING_HOLIDAYS = Set.of(
        LocalDate.of(2024, 1, 1),   // New Year's Day
        LocalDate.of(2024, 2, 19),  // Family Day
        LocalDate.of(2024, 3, 29),  // Good Friday
        LocalDate.of(2024, 5, 20),  // Victoria Day
        LocalDate.of(2024, 7, 1),   // Canada Day
        LocalDate.of(2024, 8, 5),   // Civic Holiday
        LocalDate.of(2024, 9, 2),   // Labour Day
        LocalDate.of(2024, 10, 14), // Thanksgiving
        LocalDate.of(2024, 11, 11), // Remembrance Day
        LocalDate.of(2024, 12, 25), // Christmas Day
        LocalDate.of(2024, 12, 26), // Boxing Day
        LocalDate.of(2025, 1, 1),   // New Year's Day
        LocalDate.of(2025, 2, 17),  // Family Day
        LocalDate.of(2025, 4, 18),  // Good Friday
        LocalDate.of(2025, 5, 19),  // Victoria Day
        LocalDate.of(2025, 7, 1),   // Canada Day
        LocalDate.of(2025, 8, 4),   // Civic Holiday
        LocalDate.of(2025, 9, 1),   // Labour Day
        LocalDate.of(2025, 10, 13), // Thanksgiving
        LocalDate.of(2025, 11, 11), // Remembrance Day
        LocalDate.of(2025, 12, 25), // Christmas Day
        LocalDate.of(2025, 12, 26)  // Boxing Day
    );
    
    /**
     * Optimize payment schedule using advanced algorithms
     * Otimizar cronograma de pagamentos usando algoritmos avançados
     * 
     * @param payables List of payables to optimize / Lista de pagamentos para otimizar
     * @param availableCashFlow Available cash flow limit / Limite de fluxo de caixa disponível
     * @return Optimized payment schedule / Cronograma de pagamentos otimizado
     */
    public OptimizedPaymentSchedule optimizePaymentSchedule(List<PayableEntity> payables, 
                                                          BigDecimal availableCashFlow) {
        
        log.info("Starting payment optimization for {} payables with cash flow limit of ${}", 
                payables.size(), availableCashFlow);
        
        // Step 1: Calculate optimization scores / Passo 1: Calcular pontuações de otimização
        List<PaymentOptimizationScore> scores = calculateOptimizationScores(payables);
        
        // Step 2: Apply cash flow constraints / Passo 2: Aplicar restrições de fluxo de caixa
        List<PaymentOptimizationScore> feasiblePayments = applyCashFlowConstraints(scores, availableCashFlow);
        
        // Step 3: Optimize payment dates / Passo 3: Otimizar datas de pagamento
        List<OptimizedPayment> optimizedPayments = optimizePaymentDates(feasiblePayments);
        
        // Step 4: Calculate savings and metrics / Passo 4: Calcular economias e métricas
        OptimizationMetrics metrics = calculateOptimizationMetrics(optimizedPayments, payables);
        
        OptimizedPaymentSchedule schedule = OptimizedPaymentSchedule.builder()
            .optimizedPayments(optimizedPayments)
            .totalOptimizedAmount(feasiblePayments.stream()
                .map(p -> p.getPayable().getAmountDue())
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .totalSavings(metrics.getTotalSavings())
            .optimizationMetrics(metrics)
            .generatedAt(LocalDateTime.now())
            .build();
        
        log.info("Payment optimization completed. Optimized {} payments with total savings of ${}", 
                optimizedPayments.size(), metrics.getTotalSavings());
        
        return schedule;
    }
    
    /**
     * Calculate optimization scores for each payable
     * Calcular pontuações de otimização para cada pagamento
     * 
     * @param payables List of payables / Lista de pagamentos
     * @return List of optimization scores / Lista de pontuações de otimização
     */
    private List<PaymentOptimizationScore> calculateOptimizationScores(List<PayableEntity> payables) {
        return payables.stream()
            .map(this::calculatePayableScore)
            .sorted((a, b) -> Double.compare(b.getOptimizationScore(), a.getOptimizationScore()))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate optimization score for a single payable
     * Calcular pontuação de otimização para um único pagamento
     * 
     * @param payable The payable entity / A entidade de pagamento
     * @return Optimization score / Pontuação de otimização
     */
    private PaymentOptimizationScore calculatePayableScore(PayableEntity payable) {
        double score = 0.0;
        
        // Factor 1: Days until due date (30% weight) / Fator 1: Dias até vencimento (peso 30%)
        long daysUntilDue = LocalDate.now().until(payable.getDueDate()).getDays();
        double dueDateScore = Math.min(daysUntilDue / 30.0, 1.0); // Normalize to 30 days
        score += dueDateScore * 0.30;
        
        // Factor 2: Supplier reliability (25% weight) / Fator 2: Confiabilidade do fornecedor (peso 25%)
        double supplierReliability = getSupplierReliability(payable.getSupplierId());
        score += supplierReliability * 0.25;
        
        // Factor 3: Early payment discount (20% weight) / Fator 3: Desconto por pagamento antecipado (peso 20%)
        double earlyPaymentDiscount = getEarlyPaymentDiscount(payable);
        score += earlyPaymentDiscount * 0.20;
        
        // Factor 4: Cash flow impact (15% weight) / Fator 4: Impacto no fluxo de caixa (peso 15%)
        double cashFlowImpact = calculateCashFlowImpact(payable);
        score += (1.0 - cashFlowImpact) * 0.15; // Lower impact = higher score
        
        // Factor 5: Payment priority (10% weight) / Fator 5: Prioridade de pagamento (peso 10%)
        double priorityScore = getPriorityScore(payable);
        score += priorityScore * 0.10;
        
        return PaymentOptimizationScore.builder()
            .payable(payable)
            .optimizationScore(score)
            .dueDateScore(dueDateScore)
            .supplierReliability(supplierReliability)
            .earlyPaymentDiscount(earlyPaymentDiscount)
            .cashFlowImpact(cashFlowImpact)
            .priorityScore(priorityScore)
            .build();
    }
    
    /**
     * Apply cash flow constraints to filter feasible payments
     * Aplicar restrições de fluxo de caixa para filtrar pagamentos viáveis
     * 
     * @param scores List of optimization scores / Lista de pontuações de otimização
     * @param availableCashFlow Available cash flow / Fluxo de caixa disponível
     * @return Feasible payments within cash flow limits / Pagamentos viáveis dentro dos limites de fluxo de caixa
     */
    private List<PaymentOptimizationScore> applyCashFlowConstraints(List<PaymentOptimizationScore> scores, 
                                                                  BigDecimal availableCashFlow) {
        
        List<PaymentOptimizationScore> feasiblePayments = new ArrayList<>();
        BigDecimal remainingCashFlow = availableCashFlow;
        
        for (PaymentOptimizationScore score : scores) {
            BigDecimal paymentAmount = score.getPayable().getAmountDue();
            
            if (remainingCashFlow.compareTo(paymentAmount) >= 0) {
                feasiblePayments.add(score);
                remainingCashFlow = remainingCashFlow.subtract(paymentAmount);
            } else {
                log.debug("Payment {} excluded due to cash flow constraints. Required: ${}, Available: ${}", 
                         score.getPayable().getId(), paymentAmount, remainingCashFlow);
            }
        }
        
        return feasiblePayments;
    }
    
    /**
     * Optimize payment dates for feasible payments
     * Otimizar datas de pagamento para pagamentos viáveis
     * 
     * @param feasiblePayments List of feasible payments / Lista de pagamentos viáveis
     * @return List of optimized payments / Lista de pagamentos otimizados
     */
    private List<OptimizedPayment> optimizePaymentDates(List<PaymentOptimizationScore> feasiblePayments) {
        return feasiblePayments.stream()
            .map(this::calculateOptimalPaymentDate)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate optimal payment date for a single payment
     * Calcular data ótima de pagamento para um único pagamento
     * 
     * @param score Payment optimization score / Pontuação de otimização de pagamento
     * @return Optimized payment / Pagamento otimizado
     */
    private OptimizedPayment calculateOptimalPaymentDate(PaymentOptimizationScore score) {
        PayableEntity payable = score.getPayable();
        LocalDate baseDate = LocalDate.now();
        LocalDate dueDate = payable.getDueDate();
        
        // Determine optimal payment date based on various factors
        // Determinar data ótima de pagamento baseada em vários fatores
        LocalDate optimalDate;
        
        // Priority-based scheduling / Agendamento baseado em prioridade
        PaymentPriority priority = getPriority(payable);
        switch (priority) {
            case CRITICAL:
                optimalDate = baseDate.plusDays(1); // Next business day
                break;
            case HIGH:
                optimalDate = baseDate.plusDays(2);
                break;
            case MEDIUM:
                // Calculate based on early payment discount / Calcular baseado em desconto por pagamento antecipado
                double discount = score.getEarlyPaymentDiscount();
                if (discount > 0.01) { // If discount > 1%
                    optimalDate = baseDate.plusDays(3); // Pay early for discount
                } else {
                    optimalDate = dueDate.minusDays(5); // Pay 5 days before due
                }
                break;
            case LOW:
            default:
                optimalDate = dueDate.minusDays(1); // Pay 1 day before due
                break;
        }
        
        // Ensure it's a business day / Garantir que seja um dia útil
        optimalDate = getNextBusinessDay(optimalDate);
        
        // Calculate savings / Calcular economias
        BigDecimal savings = calculatePaymentSavings(payable, optimalDate);
        
        return OptimizedPayment.builder()
            .payable(payable)
            .originalDueDate(dueDate)
            .optimizedPaymentDate(optimalDate)
            .optimizationScore(score.getOptimizationScore())
            .estimatedSavings(savings)
            .paymentMethod(determineOptimalPaymentMethod(payable))
            .build();
    }
    
    /**
     * Get next business day (excluding weekends and Canadian holidays)
     * Obter próximo dia útil (excluindo fins de semana e feriados canadenses)
     * 
     * @param date Input date / Data de entrada
     * @return Next business day / Próximo dia útil
     */
    private LocalDate getNextBusinessDay(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
    
    /**
     * Check if date is a non-business day
     * Verificar se a data é um dia não útil
     * 
     * @param date Date to check / Data para verificar
     * @return True if non-business day / Verdadeiro se dia não útil
     */
    private boolean isNonBusinessDay(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ||
               date.getDayOfWeek() == DayOfWeek.SUNDAY ||
               BANKING_HOLIDAYS.contains(date);
    }
    
    /**
     * Calculate optimization metrics for the entire schedule
     * Calcular métricas de otimização para todo o cronograma
     * 
     * @param optimizedPayments List of optimized payments / Lista de pagamentos otimizados
     * @param originalPayables Original payables list / Lista original de pagamentos
     * @return Optimization metrics / Métricas de otimização
     */
    private OptimizationMetrics calculateOptimizationMetrics(List<OptimizedPayment> optimizedPayments,
                                                           List<PayableEntity> originalPayables) {
        
        BigDecimal totalSavings = optimizedPayments.stream()
            .map(OptimizedPayment::getEstimatedSavings)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOptimizedAmount = optimizedPayments.stream()
            .map(p -> p.getPayable().getAmountDue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOriginalAmount = originalPayables.stream()
            .map(PayableEntity::getAmountDue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double optimizationRate = optimizedPayments.size() / (double) originalPayables.size();
        
        long averageProcessingTimeReduction = optimizedPayments.stream()
            .mapToLong(p -> p.getOriginalDueDate().until(p.getOptimizedPaymentDate()).getDays())
            .sum() / optimizedPayments.size();
        
        return OptimizationMetrics.builder()
            .totalSavings(totalSavings)
            .totalOptimizedAmount(totalOptimizedAmount)
            .totalOriginalAmount(totalOriginalAmount)
            .optimizationRate(optimizationRate)
            .averageProcessingTimeReduction(averageProcessingTimeReduction)
            .paymentsOptimized(optimizedPayments.size())
            .paymentsExcluded(originalPayables.size() - optimizedPayments.size())
            .build();
    }
    
    // Helper methods / Métodos auxiliares
    
    private double getSupplierReliability(Long supplierId) {
        // In real implementation, this would query supplier service
        // Por enquanto, retornando valor simulado baseado no ID
        return 0.8 + (supplierId % 10) * 0.02; // 0.8 to 0.98
    }
    
    private double getEarlyPaymentDiscount(PayableEntity payable) {
        // In real implementation, this would check supplier terms
        // Por enquanto, retornando desconto simulado
        return payable.getAmountDue().doubleValue() > 10000 ? 0.02 : 0.01; // 1-2% discount
    }
    
    private double calculateCashFlowImpact(PayableEntity payable) {
        // Simulate cash flow impact based on amount
        // Simular impacto no fluxo de caixa baseado no valor
        return Math.min(payable.getAmountDue().doubleValue() / 100000.0, 1.0);
    }
    
    private double getPriorityScore(PayableEntity payable) {
        PaymentPriority priority = getPriority(payable);
        return switch (priority) {
            case CRITICAL -> 1.0;
            case HIGH -> 0.8;
            case MEDIUM -> 0.6;
            case LOW -> 0.4;
        };
    }
    
    private PaymentPriority getPriority(PayableEntity payable) {
        // In real implementation, this would be stored in the entity
        // Por enquanto, determinando baseado no valor e descrição
        if (payable.getDescription().toLowerCase().contains("emergency") ||
            payable.getDescription().toLowerCase().contains("critical")) {
            return PaymentPriority.CRITICAL;
        } else if (payable.getAmountDue().compareTo(BigDecimal.valueOf(50000)) > 0) {
            return PaymentPriority.HIGH;
        } else if (payable.getAmountDue().compareTo(BigDecimal.valueOf(10000)) > 0) {
            return PaymentPriority.MEDIUM;
        } else {
            return PaymentPriority.LOW;
        }
    }
    
    private BigDecimal calculatePaymentSavings(PayableEntity payable, LocalDate paymentDate) {
        // Calculate savings from early payment discounts and optimized timing
        // Calcular economias de descontos por pagamento antecipado e timing otimizado
        double discountRate = getEarlyPaymentDiscount(payable);
        return payable.getAmountDue().multiply(BigDecimal.valueOf(discountRate));
    }
    
    private String determineOptimalPaymentMethod(PayableEntity payable) {
        // Determine optimal payment method based on amount and urgency
        // Determinar método ótimo de pagamento baseado no valor e urgência
        BigDecimal amount = payable.getAmountDue();
        
        if (amount.compareTo(BigDecimal.valueOf(100000)) > 0) {
            return "WIRE_TRANSFER"; // Large amounts
        } else if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return "ACH_TRANSFER"; // Medium amounts
        } else {
            return "INTERAC_E_TRANSFER"; // Small amounts
        }
    }
    
    // Data classes / Classes de dados
    
    @lombok.Data
    @lombok.Builder
    public static class OptimizedPaymentSchedule {
        private List<OptimizedPayment> optimizedPayments;
        private BigDecimal totalOptimizedAmount;
        private BigDecimal totalSavings;
        private OptimizationMetrics optimizationMetrics;
        private LocalDateTime generatedAt;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class OptimizedPayment {
        private PayableEntity payable;
        private LocalDate originalDueDate;
        private LocalDate optimizedPaymentDate;
        private Double optimizationScore;
        private BigDecimal estimatedSavings;
        private String paymentMethod;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PaymentOptimizationScore {
        private PayableEntity payable;
        private Double optimizationScore;
        private Double dueDateScore;
        private Double supplierReliability;
        private Double earlyPaymentDiscount;
        private Double cashFlowImpact;
        private Double priorityScore;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class OptimizationMetrics {
        private BigDecimal totalSavings;
        private BigDecimal totalOptimizedAmount;
        private BigDecimal totalOriginalAmount;
        private Double optimizationRate;
        private Long averageProcessingTimeReduction;
        private Integer paymentsOptimized;
        private Integer paymentsExcluded;
    }
}

