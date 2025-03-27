package com.livestock.modules.order.infra.apis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/frete")
public class FreteController {

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    private static final Map<String, BigDecimal> fretePorEstado = Map.ofEntries(
            Map.entry("SP", BigDecimal.valueOf(150)),

            Map.entry("RJ", BigDecimal.valueOf(200)),
            Map.entry("MG", BigDecimal.valueOf(200)),
            Map.entry("ES", BigDecimal.valueOf(200)),

            Map.entry("PR", BigDecimal.valueOf(250)),
            Map.entry("SC", BigDecimal.valueOf(250)),
            Map.entry("RS", BigDecimal.valueOf(250)),

            Map.entry("MS", BigDecimal.valueOf(300)),
            Map.entry("MT", BigDecimal.valueOf(300)),
            Map.entry("GO", BigDecimal.valueOf(300)),
            Map.entry("DF", BigDecimal.valueOf(300)),

            Map.entry("BA", BigDecimal.valueOf(280)),
            Map.entry("SE", BigDecimal.valueOf(280)),
            Map.entry("AL", BigDecimal.valueOf(280)),
            Map.entry("PE", BigDecimal.valueOf(280)),
            Map.entry("PB", BigDecimal.valueOf(280)),
            Map.entry("RN", BigDecimal.valueOf(280)),
            Map.entry("CE", BigDecimal.valueOf(280)),
            Map.entry("PI", BigDecimal.valueOf(280)),
            Map.entry("MA", BigDecimal.valueOf(280)),

            Map.entry("TO", BigDecimal.valueOf(350)),
            Map.entry("PA", BigDecimal.valueOf(350)),
            Map.entry("AP", BigDecimal.valueOf(350)),
            Map.entry("RR", BigDecimal.valueOf(350)),
            Map.entry("AM", BigDecimal.valueOf(350)),
            Map.entry("AC", BigDecimal.valueOf(350)),
            Map.entry("RO", BigDecimal.valueOf(350))
    );

    @GetMapping("/calcular")
    public ResponseEntity<FreteResponse> calcular(@RequestParam String cep) {
        CepResultDTO dados = consultaCepAPI.consultaCep(cep);
        String uf = dados.getUf();
        String cidade = dados.getLocalidade();
        String estado = dados.getEstado();
        String endereco = dados.getLogradouro() +
                (dados.getComplemento() != null && !dados.getComplemento().isEmpty()
                        ? " - " + dados.getComplemento() : "");

        BigDecimal freteBase = fretePorEstado.getOrDefault(uf, BigDecimal.valueOf(400));

        List<OpcaoFrete> opcoes = List.of(
                new OpcaoFrete("Gado Rápido", "2 a 3 dias úteis", freteBase.add(BigDecimal.valueOf(80))),
                new OpcaoFrete("BoiExpresso", "4 a 6 dias úteis", freteBase.add(BigDecimal.valueOf(50))),
                new OpcaoFrete("TransBoi Econômico", "6 a 9 dias úteis", freteBase)
        );

        FreteResponse response = new FreteResponse(endereco, cidade, estado, uf, opcoes);
        return ResponseEntity.ok(response);
    }
}
