package com.livestock.modules.order.domain.payment_method;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "TB_PAYMENT_METHOD")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymentMethod {

    @Id
    private int id;

    @Column(name = "payment_name", nullable = false)
    private String paymentName;


}
