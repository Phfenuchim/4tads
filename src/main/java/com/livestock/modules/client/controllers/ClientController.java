package com.livestock.modules.client.controllers;

// Mantive os imports que você já tinha, adicionei/removi o mínimo necessário
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.*;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.client.services.ClientService;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
// Removido: import jakarta.transaction.Transactional; // Não é usado em Controllers
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
// Removido: import org.springframework.security.core.Authentication; // Não usado explicitamente por você
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.List;
// Removido: import java.util.Optional; // Não usado explicitamente por você
import java.util.UUID;

@Controller
public class ClientController {

    // Campos final para as dependências
    private final ClientService clientService;
    private final ConsultaCepAPI consultaCepAPI;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository; // Adicionado ao construtor se for usado pela classe

    // Construtor para injeção de dependências
    public ClientController(ClientService clientService,
                            ConsultaCepAPI consultaCepAPI,
                            ClientRepository clientRepository,
                            AddressRepository addressRepository) { // Adicionada dependência
        this.clientService = clientService;
        this.consultaCepAPI = consultaCepAPI;
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository; // Atribuição da dependência
    }


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
        } catch (IllegalArgumentException e) { // Captura as exceções do service
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("client", createClientDTO); // Devolve o DTO para repopular
            return "redirect:/register";
        }
    }

    @GetMapping("/cep/{cep}")
    @ResponseBody
    public ResponseEntity<AddressResponseDTO> consultaCep(@PathVariable String cep) {
        // Remove caracteres não numéricos do CEP antes de consultar
        CepResultDTO result = consultaCepAPI.consultaCep(cep.replaceAll("\\D", ""));

        if (result == null || result.getCep() == null) {
            // Retorna 400 Bad Request se o CEP for inválido ou não encontrado
            // Você pode querer personalizar o corpo da resposta de erro.
            return ResponseEntity.badRequest().build();
        }

        AddressResponseDTO response = new AddressResponseDTO();
        response.setStreet(result.getLogradouro());
        response.setDistrict(result.getBairro());
        response.setCity(result.getLocalidade());
        response.setState(result.getUf());
        response.setCountry("Brasil");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/profile")
    public String showProfileForm(Model model, Principal principal) {
        // Verifica se o usuário está logado
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }
        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + principal.getName()));

        EditProfileDTO dto = new EditProfileDTO();
        dto.setFullName(client.getFullName());
        dto.setDateBirth(client.getDate_birth());
        dto.setGender(client.getGender());

        // Formata a data para exibição no formato yyyy-MM-dd para o input date
        if (client.getDate_birth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            model.addAttribute("displayDateBirth", sdf.format(client.getDate_birth()));
        }

        model.addAttribute("editProfileDTO", dto);
        return "client/edit-profile";
    }

    @PostMapping("/client/profile")
    public String updateProfile(@Valid @ModelAttribute("editProfileDTO") EditProfileDTO editProfileDTO, // Adicionado @ModelAttribute
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes,
                                Model model) { // Model para repopular a data em caso de erro
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            // Se houver erros, precisa reenviar a data formatada para a view
            // e o DTO com erros já está no modelo.
            Client clientCurrent = clientRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + principal.getName()));
            if (clientCurrent.getDate_birth() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                model.addAttribute("displayDateBirth", sdf.format(clientCurrent.getDate_birth()));
            }
            return "client/edit-profile"; // Volta para o formulário com os erros
        }

        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + principal.getName()));

        // Atualiza os dados do cliente
        String[] parts = editProfileDTO.getFullName().trim().split("\\s+", 2);
        if (parts.length > 0) {
            client.setFirstName(parts[0]);
            if (parts.length > 1) {
                client.setLastName(parts[1]);
            } else {
                client.setLastName(""); // Ou como você preferir tratar sobrenome único
            }
        }
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
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            return "client/change-password"; // Volta para o formulário com os erros
        }

        try {
            clientService.changePassword(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso!");
            return "redirect:/client/change-password"; // Ou para /client/profile
        } catch (IllegalArgumentException e) {
            // Adiciona o erro ao BindingResult para ser exibido (geralmente no topo ou ao lado do campo)
            // Aqui, estou associando a um campo específico, mas pode ser global
            result.rejectValue("currentPassword", "error.currentPassword", e.getMessage());
            return "client/change-password"; // Volta para o formulário com o erro
        }
    }

    @GetMapping("/client/addresses")
    public String listAddresses(Model model, Principal principal,
                                @RequestParam(value = "redirectTo", required = false) String redirectTo) {
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }
        String email = principal.getName();
        Client client = clientService.findClientByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + email));

        List<Address> addresses = clientService.findAddressesByClientId(client.getId());

        if (!model.containsAttribute("newAddressDTO")) {
            model.addAttribute("newAddressDTO", new NewAddressDTO());
        }
        model.addAttribute("addresses", addresses);
        model.addAttribute("redirectTo", redirectTo);

        return "client/addresses";
    }

    @PostMapping("/client/addresses")
    public String saveAddress(@Valid @ModelAttribute("newAddressDTO") NewAddressDTO dto,
                              BindingResult result,
                              @RequestParam(value = "redirectTo", required = false) String redirectTo,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }
        String email = principal.getName();

        if (result.hasErrors()) {
            Client client = clientService.findClientByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
            model.addAttribute("addresses", clientService.findAddressesByClientId(client.getId()));
            model.addAttribute("redirectTo", redirectTo);
            // O DTO com erros ("newAddressDTO") já está no modelo devido ao @ModelAttribute
            return "client/addresses"; // Volta para a view com erros
        }

        try {
            clientService.addNewAddress(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("success", "Novo endereço de entrega adicionado com sucesso!");

            if (redirectTo != null && !redirectTo.trim().isEmpty()) {
                return "redirect:" + redirectTo;
            } else {
                return "redirect:/client/addresses";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("newAddressDTO", dto); // Devolve o DTO para repopular
            if (redirectTo != null && !redirectTo.trim().isEmpty()) {
                redirectAttributes.addAttribute("redirectTo", redirectTo); // Adiciona como param de URL
            }
            return "redirect:/client/addresses";
        }
    }

    @GetMapping("/client/my-orders")
    public String viewMyOrders(Model model, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }
        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + principal.getName()));
        List<Order> orders = clientService.findOrdersByClient(client.getId());
        model.addAttribute("orders", orders);

        return "client/my-orders";
    }

    @PostMapping("/api/client/register")
    @ResponseBody
    public ResponseEntity<?> registerClientApi(@RequestBody @Valid CreateClientDTO dto, BindingResult result) { // Adicionado BindingResult
        if (result.hasErrors()) {
            // Se houver erros de validação do DTO, retorna 400 Bad Request com os erros
            // Você pode querer formatar os erros de uma maneira específica para a API
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        try {
            Client client = clientService.createClient(dto);
            return ResponseEntity.ok(client); // Retorna 201 Created seria mais apropriado com a URI do novo recurso
        } catch (IllegalArgumentException e) {
            // Por exemplo, se for email/cpf duplicado, 409 Conflict seria bom
            // return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // Ou 400 com a mensagem de erro
        }
    }
}
