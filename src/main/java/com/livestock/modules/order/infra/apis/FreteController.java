package com.livestock.modules.order.infra.apis;

import com.livestock.modules.checkout.domain.Freight; // Certifique-se que esta classe existe e está correta
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Importar Objects

@Controller
@RequestMapping("/frete") // Mapeamento a nível de classe
public class FreteController {

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    // O Map em si está ok
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

    @GetMapping("/calcular") // Alterado de @GetMapping("/{cep}") para @GetMapping("/calcular") e usar @RequestParam
    @ResponseBody // Para garantir que retorne JSON diretamente
    public ResponseEntity<FreteResponse> calcular(@RequestParam String cep) {
        CepResultDTO dados = consultaCepAPI.consultaCep(cep);

        // Verificação crucial: E se dados for null ou a UF não for encontrada?
        if (dados == null) {
            // Você pode retornar um erro ou um FreteResponse com erro/padrão
            return ResponseEntity.badRequest().body(new FreteResponse(null, null, null, null,
                    List.of(new OpcaoFrete("Erro", "CEP não encontrado ou inválido", BigDecimal.ZERO, "ERRO"))
            ));
        }

        String uf = dados.getUf();
        String cidade = dados.getLocalidade();
        // Ajustado para obter o nome completo do estado, se disponível, ou usar UF.
        String estado = dados.getEstado() != null && !dados.getEstado().isEmpty() ? dados.getEstado() : uf;
        String endereco = dados.getLogradouro() != null ? dados.getLogradouro() : ""; // Evitar NullPointerException aqui
        if (dados.getComplemento() != null && !dados.getComplemento().isEmpty()) {
            endereco += " - " + dados.getComplemento();
        }

        BigDecimal freteBase;
        if (uf != null && fretePorEstado.containsKey(uf.toUpperCase())) { // Verifica se a chave existe e uf não é null
            freteBase = fretePorEstado.get(uf.toUpperCase());
        } else {
            freteBase = BigDecimal.valueOf(400); // Valor padrão se UF for nula ou não estiver no mapa
        }

        // Certifique-se que seu enum Freight e o método getNome() existem
        List<OpcaoFrete> opcoes = List.of(
                new OpcaoFrete(
                        Freight.GADO_RAPIDO.getNome(), // Assumindo que Freight.GADO_RAPIDO.getNome() existe
                        "2 a 3 dias úteis",
                        freteBase.add(BigDecimal.valueOf(80)),
                        Freight.GADO_RAPIDO.name() // Para identificar o tipo de frete
                ),
                new OpcaoFrete(
                        Freight.BOI_EXPRESSO.getNome(),
                        "4 a 6 dias úteis",
                        freteBase.add(BigDecimal.valueOf(50)),
                        Freight.BOI_EXPRESSO.name()
                ),
                new OpcaoFrete(
                        Freight.TRANSBOI.getNome(),
                        "6 a 9 dias úteis",
                        freteBase,
                        Freight.TRANSBOI.name()
                )
        );

        FreteResponse response = new FreteResponse(endereco, cidade, estado, uf, opcoes);
        return ResponseEntity.ok(response);
    }
}
