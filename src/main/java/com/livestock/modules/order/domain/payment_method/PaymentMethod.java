package com.livestock.modules.order.domain.payment_method;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Table(name = "TB_PAYMENT_METHOD")
@Entity
public class PaymentMethod {

    @Id
    private int id;

    @Column(name = "payment_name", nullable = false)
    private String paymentName;

    // Construtor vazio
    public PaymentMethod() {
    }

    // Construtor com todos os argumentos
    public PaymentMethod(int id, String paymentName) {
        this.id = id;
        this.paymentName = paymentName;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getPaymentName() {
        return paymentName;
    }

    // Setters
    public void setId(int id) {
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
