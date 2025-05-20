package com.livestock.modules.order.infra.apis;

import com.livestock.modules.checkout.domain.Freight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
// O mapa local foi removido daqui

@Controller
@RequestMapping("/frete")
public class FreteController {

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    // REMOVIDO o mapa 'fretePorEstado' daqui, pois agora usaremos FreteConfig

    @GetMapping("/calcular")
    @ResponseBody
    public ResponseEntity<FreteResponse> calcular(@RequestParam String cep) {
        CepResultDTO dados = consultaCepAPI.consultaCep(cep);

        if (dados == null || dados.getUf() == null) {
            return ResponseEntity.badRequest().body(new FreteResponse(
                    "Endereço não encontrado",
                    "N/A", "N/A", "N/A",
                    List.of(new OpcaoFrete("Erro", "CEP inválido ou não encontrado", BigDecimal.ZERO, "ERRO"))
            ));
        }

        String uf = dados.getUf();
        String cidade = dados.getLocalidade() != null ? dados.getLocalidade() : "N/A";
        String estado = dados.getEstado() != null && !dados.getEstado().isEmpty() ? dados.getEstado() : uf;
        String endereco = dados.getLogradouro() != null ? dados.getLogradouro() : "N/A";
        if (dados.getComplemento() != null && !dados.getComplemento().isEmpty()) {
            endereco += " - " + dados.getComplemento();
        }

        // USANDO FRETECONFIG: Obtém o valor base do frete para o estado
        BigDecimal freteBase = FreteConfig.getFreteBasePorEstado(uf);

        List<OpcaoFrete> opcoes = List.of(
                criarOpcaoFrete(Freight.GADO_RAPIDO, freteBase),
                criarOpcaoFrete(Freight.BOI_EXPRESSO, freteBase),
                criarOpcaoFrete(Freight.TRANSBOI, freteBase)
        );

        FreteResponse response = new FreteResponse(endereco, cidade, estado, uf, opcoes);
        return ResponseEntity.ok(response);
    }

    private OpcaoFrete criarOpcaoFrete(Freight tipoFrete, BigDecimal freteBase) {
        // Mantendo a lógica de valor adicional como estava no seu FreteController.
        // Se você moveu isso para o enum Freight como 'getValorAdicional()', ajuste aqui.
        BigDecimal valorAdicional;
        switch (tipoFrete) {
            case GADO_RAPIDO:
                valorAdicional = BigDecimal.valueOf(80);
                break;
            case BOI_EXPRESSO:
                valorAdicional = BigDecimal.valueOf(50);
                break;
            case TRANSBOI:
                valorAdicional = BigDecimal.ZERO;
                break;
            default:
                valorAdicional = BigDecimal.valueOf(100); // Valor padrão para tipos não mapeados
        }
        BigDecimal valorTotal = freteBase.add(valorAdicional);

        return new OpcaoFrete(
                tipoFrete.getNome(),
                getPrazoEntrega(tipoFrete),
                valorTotal,
                tipoFrete.name()
        );
    }

    private String getPrazoEntrega(Freight tipoFrete) {
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
