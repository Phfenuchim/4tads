package com.livestock.modules.user.controllers; // Mantendo seu pacote original

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.domain.order_status.OrderStatus;
import com.livestock.modules.order.services.OrderService;
import com.livestock.modules.user.domain.role.Role;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UpdateUserDTO;
import com.livestock.modules.user.dto.UpdateUserPasswordDTO;
import com.livestock.modules.user.mappers.UserMapper;
import com.livestock.modules.user.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Removida a importação de PaymentMethod e PaymentMethodRepository se não for mais usada neste controller
// import com.livestock.modules.checkout.payment.PaymentMethod;
// import com.livestock.modules.checkout.payment.PaymentMethodRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional; // Pode ser removido se não usado em outros métodos
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    // Removido PaymentMethodRepository se viewAdminOrderDetails foi removido e não era usado em outro lugar aqui
    // private final PaymentMethodRepository paymentMethodRepository;

    public AdminController(UserService userService,
                           OrderService orderService
            /*, PaymentMethodRepository paymentMethodRepository // Removido da injeção se não usado */) {
        this.userService = userService;
        this.orderService = orderService;
        // this.paymentMethodRepository = paymentMethodRepository; // Removido
    }

    // --- MÉTODOS DE GERENCIAMENTO DE USUÁRIOS (EXISTENTES - sem alteração) ---
    @GetMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateUserForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Role> roles = this.userService.getAllRoles();
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
        } catch (IllegalArgumentException | com.livestock.modules.user.exceptions.UserInputException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
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
        pageNumber = Math.max(0, pageNumber);
        if (name != null && !name.trim().isEmpty()) {
            var usersFilteredByName = userService.findAllUsersByNameFilter(name);
            var usersResponseDto = usersFilteredByName.stream()
                    .map(UserMapper::toUserResponseDTO)
                    .toList();
            model.addAttribute("users", usersResponseDto);
            model.addAttribute("isFiltered", true);
            model.addAttribute("currentFilterName", name);
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
            model.addAttribute("isFiltered", false);
        }
        return "admin/list-users";
    }

    @GetMapping("/users/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserActiveStatus(@PathVariable("id") String id, @RequestParam(required = true) boolean active, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserActiveStatus(UUID.fromString(id), active);
            String action = active ? "ativado" : "desativado";
            redirectAttributes.addFlashAttribute("message", "Usuário " + action + " com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdatePasswordForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("updatePassword", new UpdateUserPasswordDTO());
        model.addAttribute("userId", id);
        return "admin/update-password";
    }

    @PostMapping("/users/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String processUpdatePasswordForm(@PathVariable("id") String id,
                                            @ModelAttribute("updatePassword") UpdateUserPasswordDTO updateUserPasswordDTO,
                                            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserPassword(UUID.fromString(id), updateUserPasswordDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("message", "Senha atualizada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdateUserForm(@PathVariable("id") String id, Model model) {
        User user = userService.getUserById(UUID.fromString(id));
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName(user.getName());
        updateUserDTO.setCpf(user.getCpf());
        UUID currentRoleId = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            currentRoleId = user.getRoles().iterator().next().getId();
            updateUserDTO.setRoleId(currentRoleId);
        }
        List<Role> roles = userService.getAllRoles();
        model.addAttribute("userId", id);
        model.addAttribute("updateUser", updateUserDTO);
        model.addAttribute("roles", roles);
        model.addAttribute("currentRoleId", currentRoleId);
        return "admin/update-user";
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
        }
        return "redirect:/admin/users";
    }

    // --- MÉTODOS DE GERENCIAMENTO DE PEDIDOS ---

    @GetMapping("/orders") // Rota: /admin/orders
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String listAllAdminOrders(Model model) {
        List<Order> orders = orderService.getAllOrdersSortedByDateDesc();
        model.addAttribute("orders", orders);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/list-orders";
    }

    // MÉTODO REMOVIDO: viewAdminOrderDetails
    /*
    @GetMapping("/orders/{id}/details") // Rota: /admin/orders/{id}/details
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String viewAdminOrderDetails(@PathVariable("id") UUID orderId, Model model, RedirectAttributes redirectAttributes) {
        // ... lógica removida ...
        return "admin/order-details-admin";
    }
    */

    @PostMapping("/orders/{orderId}/update-status") // Rota: /admin/orders/{orderId}/update-status
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String updateAdminOrderStatus(
            @PathVariable("orderId") UUID orderId,
            @RequestParam("newStatus") OrderStatus newStatus,
            RedirectAttributes redirectAttributes) {

        boolean updated = orderService.updateOrderStatus(orderId, newStatus);

        if (updated) {
            redirectAttributes.addFlashAttribute("message", "Status do pedido #" + orderId + " atualizado para " + newStatus.getDescricao() + ".");
        } else {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar o status do pedido #" + orderId + ". Pedido não encontrado.");
        }
        return "redirect:/admin/orders";
    }
}
