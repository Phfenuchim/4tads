package com.livestock.modules.client.controllers;

import com.livestock.common.GadusUserDetails;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.*;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.client.services.ClientService;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AddressRepository addressRepository;


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

    @GetMapping("/client/profile")
    public String showProfileForm(Model model, Principal principal) {
        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

        EditProfileDTO dto = new EditProfileDTO();
        dto.setFullName(client.getFullName());
        dto.setDateBirth(client.getDate_birth());
        dto.setGender(client.getGender());

        // Converter a data para o formato dd/MM/yyyy
        if (client.getDate_birth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            model.addAttribute("displayDateBirth", sdf.format(client.getDate_birth()));
        }

        model.addAttribute("editProfileDTO", dto);
        return "client/edit-profile";
    }

    @PostMapping("/client/profile")
    public String updateProfile(@Valid @ModelAttribute EditProfileDTO editProfileDTO,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "client/edit-profile";
        }

        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

        String[] parts = editProfileDTO.getFullName().trim().split("\\s+");
        client.setFirstName(parts[0]);
        client.setLastName(String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)));
        client.setFullName(editProfileDTO.getFullName());
        client.setDate_birth(editProfileDTO.getDateBirth());
        client.setGender(editProfileDTO.getGender());

        clientRepository.save(client);
        redirectAttributes.addFlashAttribute("success", "Perfil atualizado com sucesso!");
        return "redirect:/client/profile";
    }

    @GetMapping("/client/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "client/change-password";
    }

    @PostMapping("/client/change-password")
    public String processPasswordChange(@Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto,
                                        BindingResult result,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "client/change-password";
        }

        try {
            clientService.changePassword(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso!");
            return "redirect:/client/change-password";
        } catch (IllegalArgumentException e) {
            result.rejectValue("currentPassword", null, e.getMessage());
            return "client/change-password";
        }
    }


    @PostMapping("/client/addresses")
    public String saveAddress(@Valid @ModelAttribute("newAddressDTO") NewAddressDTO dto,
                              BindingResult result,
                              @RequestParam(value = "redirectTo", required = false) String redirectTo,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (result.hasErrors()) {
            // ... (lógica para retornar à view com erros, como já discutido)
            String email = principal.getName();
            Client client = clientService.findClientByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
            model.addAttribute("addresses", clientService.findAddressesByClientId(client.getId()));
            model.addAttribute("redirectTo", redirectTo);
            return "client/addresses";
        }

        try {
            // A CHAMADA AO SERVIÇO QUE DEVE SER TRANSACIONAL
            clientService.addNewAddress(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("success", "Novo endereço de entrega adicionado com sucesso!");

            if (redirectTo != null && !redirectTo.trim().isEmpty()) {
                return "redirect:" + redirectTo;
            } else {
                return "redirect:/client/addresses";
            }
        } catch (IllegalArgumentException e) {
            // ... (tratamento de erro, como já discutido)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("newAddressDTO", dto);
            // ...
            return "redirect:/client/addresses";
        }
    }





    @GetMapping("/client/addresses")
    public String listAddresses(Model model, Principal principal,
                                @RequestParam(value = "redirectTo", required = false) String redirectTo) {
        String email = principal.getName();
        // Usar o método do serviço que você criou para buscar o cliente
        Client client = clientService.findClientByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + email));

        // Usar o método do serviço para buscar os endereços
        List<Address> addresses = clientService.findAddressesByClientId(client.getId());

        if (!model.containsAttribute("newAddressDTO")) {
            model.addAttribute("newAddressDTO", new NewAddressDTO());
        }
        model.addAttribute("addresses", addresses);
        // REMOVIDO: model.addAttribute("mostrarFaturamento", !hasBillingAddress);
        model.addAttribute("redirectTo", redirectTo);

        return "client/addresses";
    }








    @GetMapping("/client/my-orders")
    public String viewMyOrders(Model model, Principal principal) {
        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

        List<Order> orders = clientService.findOrdersByClient(client.getId());
        model.addAttribute("orders", orders);

        return "client/my-orders";
    }

    @PostMapping("/api/client/register")
    @ResponseBody
    public ResponseEntity<Client> registerClientApi(@RequestBody @Valid CreateClientDTO dto) {
        Client client = clientService.createClient(dto);
        return ResponseEntity.ok(client);
    }




}
