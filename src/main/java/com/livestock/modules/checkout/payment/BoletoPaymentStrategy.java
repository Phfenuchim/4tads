package com.livestock.modules.checkout.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class BoletoPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult process(BigDecimal amount, Map<String, String> paymentDetails) {
        // Gerar código de barras do boleto
        String barCode = generateBarCode(amount);
        String dueDate = LocalDate.now().plusDays(3).toString();

        PaymentResult result = new PaymentResult();
        result.setSuccess(true);
        result.setTransactionId("BOL-" + System.currentTimeMillis());
        result.setMessage("Boleto gerado com sucesso");

        // Adicionar informações do boleto aos detalhes para exibição
        paymentDetails.put("barCode", barCode);
        paymentDetails.put("dueDate", dueDate);

        return result;
    }

    private String generateBarCode(BigDecimal amount) {
        // Lógica para gerar código de barras
        String amountStr = amount.toString().replace(".", "");
        return "34191.79001 01043.510047 91020.150008 4 " + amountStr;
    }

    @Override
    public boolean validate(Map<String, String> paymentDetails) {
        // Boleto não precisa de validação específica de campos
        return true;
    }

    @Override
    public String getName() {
        return "Boleto Bancário";
    }
}
