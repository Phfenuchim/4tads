package com.livestock.modules.checkout.payment;

public class PaymentStrategyFactory {

    public static PaymentStrategy createCreditCardStrategy() {
        // Aqui você pode adicionar lógica de configuração, injeção de dependências, etc.
        return new CreditCardPaymentStrategy();
    }

    public static PaymentStrategy createBoletoStrategy() {
        // Configurações específicas para boleto
        return new BoletoPaymentStrategy();
    }

    // Se quiser adicionar PIX no futuro
    //public static PaymentStrategy createPixStrategy() {
        //return new PixPaymentStrategy();
   //}
}

