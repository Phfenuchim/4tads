package com.livestock.modules.client.integration;

import com.livestock.modules.client.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void testCreateClientIntegration() throws Exception {
        String jsonPayload = """
        {
            "fullName": "João Gado",
            "cpf": "106.919.480-89",
            "email": "joao@gado.com",
            "phone": "(11) 99999-9999",
            "gender": "M",
            "password": "senha123",
            "dateBirth": "1990-01-01",
            "billingAddress": {
                "cep": "29163-688",
                "street": "Rua João-de-barro",
                "number": "10",
                "district": "Cidade Continental-Setor Ásia",
                "city": "Serra",
                "state": "ES",
                "country": "Brasil",
                "default": true,
                "type": "FATURAMENTO"
            }
        }
        """;

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@gado.com"));

        var client = clientRepository.findByEmail("joao@gado.com");
        assertThat(client).isPresent();
        assertThat(client.get().getCpf()).isEqualTo("106.919.480-89");
    }
}
