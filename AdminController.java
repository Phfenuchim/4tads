package com.livestock.modules.user.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.user.domain.role.Role;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UpdateUserDTO;
import com.livestock.modules.user.dto.UpdateUserPasswordDTO;
// import com.livestock.modules.user.exceptions.UserNotFoundException; // Removido se não usado diretamente
import com.livestock.modules.user.mappers.UserMapper;
import com.livestock.modules.user.services.UserService;
// Removido: import jakarta.servlet.http.HttpServletRequest;
// Removido: import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
// Removido: import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
// Removido: import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    // Construtor para injeção de dependência.
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateUserForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Role> roles = this.userService.getAllRoles(); // Avisar: Boa prática tipar a variável 'roles'.

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles_user", userDetails.getAuthorities());
        }
        User user = new User();
        user.setRoles(new HashSet<>());

        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        return "admin/create-user";
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("message", "Usuário criado com sucesso!");
        } catch (IllegalArgumentException | com.livestock.modules.user.exceptions.UserInputException e) { // Avisar: Capturar UserInputException também.
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Avisar: Redirecionar para /admin/users pode não ser ideal em caso de erro na criação.
            // Considerar redirecionar de volta para /admin/create-user com os dados e o erro.
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                            @RequestParam(required = false, defaultValue = "10") int pageSize,
                            @RequestParam(required = false) String name,
                            Model model) {
        pageNumber = Math.max(0, pageNumber); // Avisar: Boa prática para evitar pageNumber < 0.

        if (name != null && !name.trim().isEmpty()) {
            var usersFilteredByName = userService.findAllUsersByNameFilter(name);
            var usersResponseDto = usersFilteredByName.stream()
                    .map(UserMapper::toUserResponseDTO)
                    .toList();
            model.addAttribute("users", usersResponseDto);
            model.addAttribute("isFiltered", true); // Avisar: Adicionado para a view.
            model.addAttribute("currentFilterName", name); // Avisar: Adicionado para a view.
        } else {
            var usersPage = userService.getAllUsersPaginated(pageNumber, pageSize);
            var usersResponseDto = usersPage.getContent().stream()
                    .map(UserMapper::toUserResponseDTO)
                    .toList();

            var pagination = PaginationResponseDTO.builder()
                    .pageNumber(usersPage.getNumber())
                    .pageSize(usersPage.getSize())
                    .totalPages(usersPage.getTotalPages())
                    .totalItems((int) usersPage.getTotalElements())
                    .build();

            model.addAttribute("users", usersResponseDto);
            model.addAttribute("pagination", pagination);
            model.addAttribute("isFiltered", false); // Avisar: Adicionado para a view.
        }
        return "admin/list-users";
    }

    @GetMapping("/users/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserActiveStatus(@PathVariable("id") String id, @RequestParam(required = true) boolean active, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserActiveStatus(UUID.fromString(id), active);
            // Avisar: Mensagem de sucesso deveria refletir a ação (ativado/desativado).
            redirectAttributes.addFlashAttribute("message", "Status do usuário alterado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Avisar: Não há return explícito em caso de sucesso aqui, o que pode ser um bug se o try falhar e não cair no catch.
            // O return "redirect:/admin/users" fora do try/catch cobre isso.
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdatePasswordForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("updatePassword", new UpdateUserPasswordDTO());
        model.addAttribute("userId", id); // Avisar: Enviar userId para o formulário usar no action.
        return "admin/update-password";
    }

    @GetMapping("/users/{id}/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdateUserForm(@PathVariable("id") String id, Model model) {
        User user = userService.getUserById(UUID.fromString(id));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName(user.getName());
        updateUserDTO.setCpf(user.getCpf());

        UUID currentRoleId = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) { // Avisar: Adicionada verificação de nulidade para user.getRoles().
            currentRoleId = user.getRoles().iterator().next().getId();
            updateUserDTO.setRoleId(currentRoleId);
        }

        List<Role> roles = userService.getAllRoles();

        model.addAttribute("id", id); // Avisar: Pode ser redundante se userId já está sendo enviado e é o mesmo.
        model.addAttribute("userId", id); // Avisar: Enviar userId para o formulário.
        model.addAttribute("updateUser", updateUserDTO);
        model.addAttribute("roles", roles);
        model.addAttribute("currentRoleId", currentRoleId);

        return "admin/update-user";
    }

    // Este método não estava no seu trecho original, mas é um exemplo comum.
    // Se ele não existe, ignore.
    @GetMapping("/users/{id}/edit-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUserById(@PathVariable("id") String id, Model model) {
        User user = userService.getUserById(UUID.fromString(id));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName(user.getName());
        updateUserDTO.setCpf(user.getCpf());
        // Se roles for editável aqui, adicione a lógica para popular o roleId no DTO.
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            updateUserDTO.setRoleId(user.getRoles().iterator().next().getId());
        }


        model.addAttribute("userId", id); // Avisar: Enviar o ID para a view.
        model.addAttribute("updateUser", updateUserDTO);
        model.addAttribute("allRoles", userService.getAllRoles()); // Avisar: Enviar todas as roles para seleção.
        return "admin/edit-user"; // Avisar: Nome da view.
    }

    @PostMapping("/users/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String processUpdatePasswordForm(@PathVariable("id") String id, @ModelAttribute("updatePassword") UpdateUserPasswordDTO updateUserPasswordDTO, RedirectAttributes redirectAttributes) { // Avisar: Nome do método ajustado para clareza.
        try {
            userService.updateUserPassword(UUID.fromString(id), updateUserPasswordDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("message", "Senha atualizada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Avisar: Redirecionar de volta para o formulário de alteração de senha com o erro seria melhor.
            // return "redirect:/admin/users/" + id + "/update-password";
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(
            @PathVariable("id") UUID id, // Avisar: Mantido UUID aqui, já que updateUser do service espera UUID.
            @ModelAttribute("updateUser") UpdateUserDTO updateUserDTO,
            RedirectAttributes redirectAttributes) {

        try {
            userService.updateUser(id, updateUserDTO);
            redirectAttributes.addFlashAttribute("message", "Usuário atualizado com sucesso!");
        } catch (Exception e) { // Avisar: Capturar exceções mais específicas (ex: IllegalArgumentException, UserInputException).
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Avisar: Redirecionar de volta para o formulário de edição com o erro seria melhor.
            // return "redirect:/admin/users/" + id + "/update-user";
        }
        return "redirect:/admin/users";
    }
}
