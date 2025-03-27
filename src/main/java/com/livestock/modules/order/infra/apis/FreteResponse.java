package com.livestock.modules.order.infra.apis;

import java.math.BigDecimal;
import java.util.List;

public class FreteResponse {
    private String endereco;
    private String cidade;
    private String estado;
    private String uf;
    private List<OpcaoFrete> opcoesFrete;

    public FreteResponse(String endereco, String cidade, String estado, String uf, List<OpcaoFrete> opcoesFrete) {
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.uf = uf;
        this.opcoesFrete = opcoesFrete;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public String getUf() {
        return uf;
    }

    public List<OpcaoFrete> getOpcoesFrete() {
        return opcoesFrete;
    }
}
