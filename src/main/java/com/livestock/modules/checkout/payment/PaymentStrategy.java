package com.livestock.modules.checkout.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentStrategy {
    /**
     * Processa um pagamento
     * @return informações sobre o processamento do pagamento
     */
    PaymentResult process(BigDecimal amount, Map<String, String> paymentDetails);

    /**
     * Valida se os dados de pagamento estão completos
     */
    boolean validate(Map<String, String> paymentDetails);

    /**
     * Retorna o nome amigável do método de pagamento
     */
    String getName();
}
