package com.livestock.modules.client.controllers;

import com.livestock.modules.client.dto.AddressResponseDTO;
import com.livestock.modules.client.dto.CreateClientDTO;
import com.livestock.modules.client.services.ClientService;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new CreateClientDTO());
        }
        return "client/register";
    }

    @PostMapping("/register")
    public String registerClient(
            @Valid @ModelAttribute("client") CreateClientDTO createClientDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.client", result);
            redirectAttributes.addFlashAttribute("client", createClientDTO);
            return "redirect:/register";
        }

        try {
            clientService.createClient(createClientDTO);
            redirectAttributes.addFlashAttribute("success", "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("client", createClientDTO);
            return "redirect:/register";
        }
    }

    @GetMapping("/cep/{cep}")
    @ResponseBody
    public ResponseEntity<AddressResponseDTO> consultaCep(@PathVariable String cep) {
        CepResultDTO result = consultaCepAPI.consultaCep(cep);

        AddressResponseDTO response = new AddressResponseDTO();
        response.setStreet(result.getLogradouro());
        response.setDistrict(result.getBairro());         // Bairro
        response.setCity(result.getLocalidade());         // Cidade
        response.setState(result.getUf());                // Estado (UF)
        response.setCountry("Brasil");                    // País padrão

        return ResponseEntity.ok(response);
    }

}
