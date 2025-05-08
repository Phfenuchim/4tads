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

    @Transactional
    public void addNewAddress(String email, NewAddressDTO dto) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (dto.isDefault()) {
            client.getAddress().forEach(addr -> addr.setDefaultAddress(false));
        }

        Address newAddress = new Address();
        newAddress.setClient(client);
        newAddress.setCep(dto.getCep());
        newAddress.setStreet(dto.getStreet());
        newAddress.setNumber(dto.getNumber());
        newAddress.setComplement(dto.getComplement());
        newAddress.setDistrict(dto.getDistrict());
        newAddress.setCity(dto.getCity());
        newAddress.setState(dto.getState());
        newAddress.setCountry(dto.getCountry());
        newAddress.setDefaultAddress(dto.isDefault());

        client.getAddress().add(newAddress);
        clientRepository.save(client);
    }

    @PostMapping("/client/addresses")
    public String saveAddress(@Valid @ModelAttribute("newAddressDTO") NewAddressDTO dto,
                              BindingResult result,
                              @RequestParam(value = "redirectTo", required = false) String redirectTo,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            Client client = clientRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
            model.addAttribute("addresses", client.getAddress());
            model.addAttribute("redirectTo", redirectTo);
            return "client/addresses";
        }

        try {
            clientService.addNewAddress(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("success", "Endereço adicionado com sucesso!");
            return redirectTo != null ? "redirect:" + redirectTo : "redirect:/client/addresses";
        } catch (IllegalArgumentException e) {
            result.rejectValue("cep", null, e.getMessage());
            Client client = clientRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
            model.addAttribute("addresses", client.getAddress());
            model.addAttribute("redirectTo", redirectTo);
            return "client/addresses";
        }
    }



    @GetMapping("/client/addresses")
    public String listAddresses(Model model, Principal principal) {
        String email = principal.getName();
        Optional<Client> client = clientRepository.findByEmail(email);

        if (client.isEmpty()) {
            return "redirect:/login";
        }

        List<Address> addresses = addressRepository.findByClientId(client.get().getId());

        boolean temFaturamento = addresses.stream()
                .anyMatch(a -> a.getType().toString().equals("FATURAMENTO"));

        model.addAttribute("addresses", addresses);
        model.addAttribute("newAddressDTO", new NewAddressDTO());
        model.addAttribute("mostrarFaturamento", !temFaturamento);

        return "client/addresses";
    }



    @PostMapping("/client/address/{id}/set-default")
    public String setDefaultAddress(@PathVariable UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            clientService.setDefaultAddress(principal.getName(), id);
            redirectAttributes.addFlashAttribute("success", "Endereço definido como padrão com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/client/addresses";
    }

    @GetMapping("/client/my-orders")
    public String viewMyOrders(Model model, Principal principal) {
        Client client = clientRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

        List<Order> orders = clientService.findOrdersByClient(client.getId());
        model.addAttribute("orders", orders);

        return "client/my-orders";
    }



}
