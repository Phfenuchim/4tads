package com.livestock.modules.checkout.payment;

import java.math.BigDecimal;
import java.util.Map;

public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult process(BigDecimal amount, Map<String, String> paymentDetails) {
        // Lógica para processar pagamento com cartão
        String cardNumber = paymentDetails.get("cardNumber");
        String cardName = paymentDetails.get("cardName");
        String cardCvv = paymentDetails.get("cardCvv");
        String cardExpiry = paymentDetails.get("cardExpiry");
        int installments = Integer.parseInt(paymentDetails.get("installments"));

        // Integração com gateway de pagamento
        String transactionId = processWithGateway(cardNumber, cardName, cardCvv, cardExpiry, amount, installments);

        PaymentResult result = new PaymentResult();
        result.setSuccess(true);
        result.setTransactionId(transactionId);
        result.setMessage("Pagamento aprovado com cartão de crédito");

        return result;
    }

    private String processWithGateway(String cardNumber, String cardName, String cardCvv,
                                      String cardExpiry, BigDecimal amount, int installments) {
        // Simulação de chamada ao gateway
        return "CC-" + System.currentTimeMillis();
    }

    @Override
    public boolean validate(Map<String, String> paymentDetails) {
        return paymentDetails.containsKey("cardNumber") &&
                paymentDetails.containsKey("cardName") &&
                paymentDetails.containsKey("cardCvv") &&
                paymentDetails.containsKey("cardExpiry") &&
                paymentDetails.containsKey("installments");
    }

    @Override
    public String getName() {
        return "Cartão de Crédito";
    }
}
