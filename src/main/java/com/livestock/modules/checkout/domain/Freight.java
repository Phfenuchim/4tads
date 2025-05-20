package com.livestock.modules.checkout.domain;

import java.math.BigDecimal;

public enum Freight {
    GADO_RAPIDO("Gado Rápido", new BigDecimal("80.00")), // Apenas o valor ADICIONAL
    BOI_EXPRESSO("Boi Expresso", new BigDecimal("50.00")), // Apenas o valor ADICIONAL
    TRANSBOI("TransBoi Econômico", BigDecimal.ZERO); // Frete "base" (sem adicional)

    private final String nome;
    private final BigDecimal valorAdicional; // Valor ADICIONAL ao frete base do estado

    Freight(String nome, BigDecimal valorAdicional) {
        this.nome = nome;
        this.valorAdicional = valorAdicional;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorAdicional() {
        return valorAdicional;
    }

    // (Opcional) Método para buscar um tipo de frete pelo nome, se precisar
    public static Freight fromNome(String nome) {
        for (Freight freight : values()) {
            if (freight.getNome().equalsIgnoreCase(nome)) {
                return freight;
            }
        }
        throw new IllegalArgumentException("Tipo de frete inválido: " + nome);
    }
}
