package com.livestock.modules.order.infra.apis;

import com.livestock.modules.checkout.domain.Freight;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Arrays; // Avisar: Adicionado para List.of, mas melhor usar java.util.List diretamente.
import java.util.List; // Avisar: Usar este import para List.
import java.util.stream.Collectors; // Avisar: Adicionado para construir a lista de opções de forma mais dinâmica.


@Controller
@RequestMapping("/frete")
public class FreteController {

    private final ConsultaCepAPI consultaCepAPI;
    private final FreteConfig freteConfig;

    // Construtor para injeção de dependências.
    public FreteController(ConsultaCepAPI consultaCepAPI, FreteConfig freteConfig) {
        this.consultaCepAPI = consultaCepAPI;
        this.freteConfig = freteConfig;
    }

    @GetMapping("/calcular")
    @ResponseBody
    public ResponseEntity<FreteResponse> calcular(@RequestParam String cep) {
        CepResultDTO dados = consultaCepAPI.consultaCep(cep.replaceAll("\\D", ""));

        if (dados == null || dados.getUf() == null || dados.getUf().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new FreteResponse(
                    "Endereço não encontrado",
                    "N/A", "N/A", "N/A",
                    List.of(new OpcaoFrete("Erro", "CEP inválido ou não encontrado", BigDecimal.ZERO, "ERRO"))
            ));
        }

        String uf = dados.getUf();
        String cidade = dados.getLocalidade() != null ? dados.getLocalidade() : "N/A";
        String estado = (dados.getEstado() != null && !dados.getEstado().trim().isEmpty()) ? dados.getEstado() : uf;
        String endereco = dados.getLogradouro() != null ? dados.getLogradouro() : "N/A";
        if (dados.getComplemento() != null && !dados.getComplemento().trim().isEmpty()) {
            endereco += " - " + dados.getComplemento();
        }

        BigDecimal freteBase = freteConfig.getFreteBasePorEstado(uf);

        // Avisar: Modificado para usar stream e iterar sobre os valores do enum Freight.
        List<OpcaoFrete> opcoes = Arrays.stream(Freight.values())
                .map(tipoFrete -> criarOpcaoFrete(tipoFrete, freteBase))
                .collect(Collectors.toList());

        FreteResponse response = new FreteResponse(endereco, cidade, estado, uf, opcoes);
        return ResponseEntity.ok(response);
    }

    private OpcaoFrete criarOpcaoFrete(Freight tipoFrete, BigDecimal freteBase) {
        // Usa o getValorAdicional() do enum Freight.
        BigDecimal valorAdicional = tipoFrete.getValorAdicional();
        BigDecimal valorTotal = freteBase.add(valorAdicional);

        return new OpcaoFrete(
                tipoFrete.getNome(),
                getPrazoEntrega(tipoFrete), // Lógica de prazo mantida aqui.
                valorTotal,
                tipoFrete.name() // Usar o nome do enum como identificador.
        );
    }

    private String getPrazoEntrega(Freight tipoFrete) {
        // Lógica de prazo pode ser movida para o enum Freight se preferir.
        switch (tipoFrete) {
            case GADO_RAPIDO:
                return "2 a 3 dias úteis";
            case BOI_EXPRESSO:
                return "4 a 6 dias úteis";
            case TRANSBOI:
                return "6 a 9 dias úteis";
            default:
                return "Prazo não definido";
        }
    }
}
