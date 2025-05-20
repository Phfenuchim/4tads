package com.livestock.modules.checkout.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Processa pagamentos usando diferentes estratégias (Strategy Pattern).
 * Delega a criação das estratégias para a PaymentStrategyFactory (Factory Pattern).
 */
public class PaymentProcessor {

    // Mapeia um código (ex: "cartao") para sua respectiva estratégia de pagamento.
    private final Map<String, PaymentStrategy> strategies = new HashMap<>();

    /**
     * Construtor: Inicializa as estratégias de pagamento ao criar o processador.
     */
    public PaymentProcessor() {
        initializeStrategies();
    }

    /**
     * Carrega e registra as estratégias de pagamento suportadas usando a Factory.
     */
    private void initializeStrategies() {
        // Obtém e registra a estratégia de cartão de crédito da Factory.
        registerStrategy("cartao", PaymentStrategyFactory.createCreditCardStrategy());
        // Obtém e registra a estratégia de boleto da Factory.
        registerStrategy("boleto", PaymentStrategyFactory.createBoletoStrategy());
        // Exemplo: registerStrategy("pix", PaymentStrategyFactory.createPixStrategy());
    }

    /**
     * Adiciona uma estratégia ao mapa de estratégias.
     * code: Identificador da estratégia (ex: "cartao").
     * strategy: Instância da estratégia.
     */
    private void registerStrategy(String code, PaymentStrategy strategy) {
        strategies.put(code.toLowerCase(), strategy); // Usa minúsculas para consistência.
    }

    /**
     * Processa o pagamento usando a estratégia correta.
     * strategyCode: Código do método de pagamento (ex: "cartao").
     * amount: Valor a ser pago.
     * paymentDetails: Detalhes específicos do pagamento (ex: dados do cartão).
     * Retorna PaymentResult com o resultado.
     * Lança IllegalArgumentException se método não suportado ou dados inválidos.
     */
    public PaymentResult processPayment(String strategyCode, BigDecimal amount, Map<String, String> paymentDetails) {
        // 1. Obtém a estratégia pelo código.
        PaymentStrategy strategy = strategies.get(strategyCode.toLowerCase());

        // 2. Valida se a estratégia existe.
        if (strategy == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + strategyCode);
        }

        // 3. Valida os detalhes do pagamento com a própria estratégia.
        if (!strategy.validate(paymentDetails)) {
            throw new IllegalArgumentException("Dados de pagamento inválidos para " + strategy.getName());
        }

        // 4. Delega o processamento para a estratégia escolhida.
        return strategy.process(amount, paymentDetails);
    }

    /**
     * Retorna os códigos dos métodos de pagamento disponíveis.
     * Útil para exibir opções na UI.
     * Retorna um Set de Strings (ex: "cartao", "boleto").
     */
    public Set<String> getAvailablePaymentMethods() {
        return strategies.keySet();
    }

    /**
     * Retorna o nome amigável do método de pagamento.
     * code: Código do método.
     * Retorna o nome (ex: "Cartão de Crédito") ou null.
     */
    public String getPaymentMethodName(String code) {
        PaymentStrategy strategy = strategies.get(code.toLowerCase());
        return strategy != null ? strategy.getName() : null;
    }

    /**
     * Valida os detalhes de pagamento para uma estratégia específica.
     * strategyCode: Código do método.
     * paymentDetails: Detalhes a validar.
     * Retorna true se válido, false caso contrário.
     * Lança IllegalArgumentException se método não suportado.
     */
    public boolean validatePayment(String strategyCode, Map<String, String> paymentDetails) {
        PaymentStrategy strategy = strategies.get(strategyCode.toLowerCase());

        if (strategy == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + strategyCode);
        }

        return strategy.validate(paymentDetails);
    }
}
