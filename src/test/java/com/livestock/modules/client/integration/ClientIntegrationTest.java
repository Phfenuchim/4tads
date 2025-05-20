package com.livestock.modules.client.integration;

import com.livestock.modules.client.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
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
@Transactional // Garante que cada teste rode em sua própria transação e seja revertido ao final.
class ClientIntegrationTest {

    private final MockMvc mockMvc;
    private final ClientRepository clientRepository;

    // Construtor para injeção de dependências.
    // O Spring Test irá injetar os beans MockMvc e ClientRepository aqui.
    public ClientIntegrationTest(MockMvc mockMvc, ClientRepository clientRepository) {
        this.mockMvc = mockMvc;
        this.clientRepository = clientRepository;
    }

    @Test
    void testCreateClientIntegration() throws Exception {
        // Avisar: Nome da propriedade no JSON é 'billingAddress', mas seu CreateClientDTO agora usa 'initialAddress'.
        // O JSON payload precisa ser ajustado para refletir o nome correto da propriedade no DTO.
        // Se CreateClientDTO espera 'initialAddress', o JSON deve ser:
        String jsonPayload = """
        {
            "fullName": "João Gado",
            "cpf": "106.919.480-89",
            "email": "joao@gado.com",
            "phone": "(11) 99999-9999",
            "gender": "M",
            "password": "senha123",
            "dateBirth": "1990-01-01",
            "initialAddress": { 
                "cep": "29163-688",
                "street": "Rua João-de-barro",
                "number": "10",
                "district": "Cidade Continental-Setor Ásia",
                "city": "Serra",
                "state": "ES",
                "country": "Brasil"
            }
        }
        """;
        // Avisar: Removidos "default": true e "type": "FATURAMENTO" do JSON do endereço,
        // pois o AddressDTO dentro de CreateClientDTO.initialAddress não tem esses campos.
        // A lógica de tipo e se é padrão é tratada no backend.

        mockMvc.perform(post("/api/client/register") // Avisar: Verifique se este endpoint API existe e está correto.
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk()) // Avisar: Se a criação for bem-sucedida, um status 201 Created é mais comum para POSTs que criam recursos.
                // Se seu controller retorna 200 OK, então está correto.
                .andExpect(jsonPath("$.email").value("joao@gado.com"));

        var client = clientRepository.findByEmail("joao@gado.com");
        assertThat(client).isPresent();
        // Avisar: Verifique se o CPF é salvo com ou sem a máscara no banco de dados.
        // Se for salvo sem máscara, a asserção precisa comparar com "10691948089".
        assertThat(client.get().getCpf()).isEqualTo("106.919.480-89"); // Assumindo que é salvo com máscara.
    }
}
