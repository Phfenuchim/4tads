package com.livestock.modules.checkout.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public enum Freight {
    GADO_RAPIDO("Gado Rápido", new BigDecimal("230.00")),
    BOI_EXPRESSO("Boi Expresso", new BigDecimal("200.00")),
    TRANSBOI("TransBoi Econômico", new BigDecimal("150.00"));


    private final String nome;
    private final BigDecimal valor;

    Freight(String nome, BigDecimal valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public static Freight fromNome(String nome) {
        try {
            return Freight.valueOf(nome);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de frete inválido: " + nome);
        }
    }

}
