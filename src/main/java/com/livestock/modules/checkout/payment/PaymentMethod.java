package com.livestock.modules.checkout.payment;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Table(name = "TB_PAYMENT_METHOD")
@Entity
public class PaymentMethod {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_name", nullable = false)
    private String paymentName;

    // Construtor vazio
    public PaymentMethod() {
    }

    // Construtor com todos os argumentos
    public PaymentMethod(UUID id, String paymentName) {
        this.id = id;
        this.paymentName = paymentName;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getPaymentName() {
        return paymentName;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    // Implementação de equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Implementação de toString
    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id=" + id +
                ", paymentName='" + paymentName + '\'' +
                '}';
    }
}
