package com.livestock.modules.user.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UpdateUserDTO;
import com.livestock.modules.user.dto.UpdateUserPasswordDTO;
import com.livestock.modules.user.mappers.UserMapper;
import com.livestock.modules.user.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.UUID;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal=authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles", userDetails.getAuthorities());
        }

        model.addAttribute("user", new User());
        return "home";
    }

    @GetMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateUserForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var roles = this.userService.getAllRoles();

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles_user", userDetails.getAuthorities());
        }

        User user = new User();
        user.setRoles(new HashSet<>()); // Evita NullPointerException

        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        return "create-user";
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("message", "Usuário criado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/home";
        }
        return "redirect:/admin/home";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(@RequestParam(required = false, defaultValue = "0") int pageNumber, @RequestParam(required = false, defaultValue = "10") int pageSize, @RequestParam(required = false) String name, Model model) {
        if (name != null) {
            var usersFilteredByName = userService.findAllUsersByNameFilter(name);

            var usersResponseDto = usersFilteredByName.stream()
                    .map(UserMapper::toUserResponseDTO)
                    .toList();

            model.addAttribute("users", usersResponseDto);

            return "list-users";
        }

        var users = userService.getAllUsersPaginated(pageNumber, pageSize);

        var usersResponseDto = users.stream()
                .map(UserMapper::toUserResponseDTO)
                .toList();

        var pagination = PaginationResponseDTO
                .builder()
                .pageNumber(users.getNumber())
                .pageSize(users.getSize())
                .totalPages(users.getTotalPages())
                .totalItems((int) users.getTotalElements())
                .build();

        model.addAttribute("users", usersResponseDto);
        model.addAttribute("pagination", pagination);

        return "list-users.html";
    }

    @GetMapping("/users/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserActiveStatus(@PathVariable("id") String id, @RequestParam(required = true) boolean active, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserActiveStatus(UUID.fromString(id), active);
            redirectAttributes.addFlashAttribute("message", "Usuário ativado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }


    @GetMapping("/users/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdatePasswordForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("updatePassword", new UpdateUserPasswordDTO());
        return "update-password";
    }

    @GetMapping("/users/{id}/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdateUserForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("updateUser", new UpdateUserDTO());
        return "update-user";
    }

    @GetMapping("users/{id}/edit-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUserById(@PathVariable("id") String id, Model model) {
        var user = userService.getUserById(UUID.fromString(id));
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/users/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdatePasswordForm(@PathVariable("id") String id, @ModelAttribute("updatePassword") UpdateUserPasswordDTO updateUserPasswordDTO, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserPassword(UUID.fromString(id), updateUserPasswordDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("message", "Senha atualizada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(
            @PathVariable("id") UUID id,
            @ModelAttribute("updateUser") UpdateUserDTO updateUserDTO,
            RedirectAttributes redirectAttributes) {

        try {
            userService.updateUser(id, updateUserDTO);
            redirectAttributes.addFlashAttribute("message", "Usuário atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        response.sendRedirect("/home");
    }
}
