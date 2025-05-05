package com.livestock.modules.checkout.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PaymentProcessor {
    private final Map<String, PaymentStrategy> strategies = new HashMap<>();

    public PaymentProcessor() {
        initializeStrategies();
    }

    private void initializeStrategies() {
        registerStrategy("cartao", PaymentStrategyFactory.createCreditCardStrategy());
        registerStrategy("boleto", PaymentStrategyFactory.createBoletoStrategy());
    }

    private void registerStrategy(String code, PaymentStrategy strategy) {
        strategies.put(code, strategy);
    }

    public PaymentResult processPayment(String strategyCode, BigDecimal amount, Map<String, String> paymentDetails) {
        PaymentStrategy strategy = strategies.get(strategyCode);

        if (strategy == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + strategyCode);
        }

        if (!strategy.validate(paymentDetails)) {
            throw new IllegalArgumentException("Dados de pagamento inválidos para " + strategy.getName());
        }

        return strategy.process(amount, paymentDetails);
    }

    public Set<String> getAvailablePaymentMethods() {
        return strategies.keySet();
    }

    public String getPaymentMethodName(String code) {
        PaymentStrategy strategy = strategies.get(code);
        return strategy != null ? strategy.getName() : null;
    }

    public boolean validatePayment(String strategyCode, Map<String, String> paymentDetails) {
        PaymentStrategy strategy = strategies.get(strategyCode);

        if (strategy == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + strategyCode);
        }

        return strategy.validate(paymentDetails);
    }

}
