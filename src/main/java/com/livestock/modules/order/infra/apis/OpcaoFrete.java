package com.livestock.modules.order.infra.apis;

import java.math.BigDecimal;

public class OpcaoFrete {
    private String nome;
    private String prazo;
    private BigDecimal valor;
    private String enumName;

    public OpcaoFrete(String nome, String prazo, BigDecimal valor, String enumName) {
        this.nome = nome;
        this.prazo = prazo;
        this.valor = valor;
        this.enumName = enumName;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrazo() {
        return prazo;
    }

    public void setPrazo(String prazo) {
        this.prazo = prazo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }
}
