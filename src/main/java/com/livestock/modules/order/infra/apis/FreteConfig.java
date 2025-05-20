package com.livestock.modules.order.infra.apis;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class FreteConfig {

    private static final Map<String, BigDecimal> FRETE_POR_ESTADO = Map.ofEntries(
            Map.entry("AC", BigDecimal.valueOf(350)),
            Map.entry("AL", BigDecimal.valueOf(280)),
            Map.entry("AP", BigDecimal.valueOf(350)),
            Map.entry("AM", BigDecimal.valueOf(350)),
            Map.entry("BA", BigDecimal.valueOf(280)),
            Map.entry("CE", BigDecimal.valueOf(280)),
            Map.entry("DF", BigDecimal.valueOf(300)),
            Map.entry("ES", BigDecimal.valueOf(200)),
            Map.entry("GO", BigDecimal.valueOf(300)),
            Map.entry("MA", BigDecimal.valueOf(280)),
            Map.entry("MT", BigDecimal.valueOf(300)),
            Map.entry("MS", BigDecimal.valueOf(300)),
            Map.entry("MG", BigDecimal.valueOf(200)),
            Map.entry("PA", BigDecimal.valueOf(350)),
            Map.entry("PB", BigDecimal.valueOf(280)),
            Map.entry("PR", BigDecimal.valueOf(250)),
            Map.entry("PE", BigDecimal.valueOf(280)),
            Map.entry("PI", BigDecimal.valueOf(280)),
            Map.entry("RJ", BigDecimal.valueOf(200)),
            Map.entry("RN", BigDecimal.valueOf(280)),
            Map.entry("RS", BigDecimal.valueOf(250)),
            Map.entry("RO", BigDecimal.valueOf(350)),
            Map.entry("RR", BigDecimal.valueOf(350)),
            Map.entry("SC", BigDecimal.valueOf(250)),
            Map.entry("SP", BigDecimal.valueOf(150)),
            Map.entry("SE", BigDecimal.valueOf(280)),
            Map.entry("TO", BigDecimal.valueOf(350))
    );

    public BigDecimal getFreteBasePorEstado(String uf) {
        return FRETE_POR_ESTADO.getOrDefault(uf.toUpperCase(), BigDecimal.valueOf(400));
    }
}
