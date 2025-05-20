package com.livestock.modules.order.infra.apis;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ConsultaCepAPI {

    public CepResultDTO consultaCep(String cep) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<CepResultDTO> responseEntity = restTemplate.getForEntity(String.format("https://viacep.com.br/ws/%s/json", cep), CepResultDTO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            // Logar a exceção (pode ser útil para depuração)
            System.err.println("Erro ao consultar CEP: " + e.getMessage());
            return null; // Retorna null em caso de erro
        }
    }

    public FreteResponse consultaCepViaApiInterna(String cep) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("http://localhost:8080/frete/calcular?cep=%s", cep);
        ResponseEntity<FreteResponse> response = restTemplate.getForEntity(url, FreteResponse.class);
        return response.getBody();
    }


}
