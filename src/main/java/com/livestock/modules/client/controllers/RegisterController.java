package com.livestock.modules.client.controllers;

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
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("client", new CreateClientDTO());
        return "register";
    }

    @PostMapping
    public String registerClient(@Valid @ModelAttribute("client") CreateClientDTO createClientDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            clientService.createClient(createClientDTO);
            redirectAttributes.addFlashAttribute("success", "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/cep/{cep}")
    @ResponseBody
    public ResponseEntity<CepResultDTO> consultaCep(@PathVariable String cep) {
        CepResultDTO result = consultaCepAPI.consultaCep(cep);
        return ResponseEntity.ok(result);
    }
}
