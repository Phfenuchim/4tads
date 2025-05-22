package com.livestock.modules.order.controllers;

import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.dto.OrderDetailDTO; // IMPORTAR O NOVO DTO
import com.livestock.modules.order.services.OrderService;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.ClientRepository;
// PaymentMethodRepository já está sendo injetado no OrderService, não precisa aqui se só usado lá.
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ClientRepository clientRepository;

    public OrderController(OrderService orderService, ClientRepository clientRepository) {
        this.orderService = orderService;
        this.clientRepository = clientRepository;
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable("id") UUID orderId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<OrderDetailDTO> orderDetailOptional = orderService.getOrderDetailView(orderId);

        if (orderDetailOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pedido não encontrado.");
            return "redirect:/client/my-orders";
        }

        OrderDetailDTO orderDetail = orderDetailOptional.get();

        // Verificação de segurança (simplificada, assumindo que OrderDetailDTO tem userId ou similar)
        // Para uma verificação robusta, o OrderService poderia já ter feito isso
        // ou você buscaria o Order original para comparar o userId.
        // Por simplicidade, vamos assumir que o serviço já validou ou que o DTO contém o userId.
        // Se o OrderDetailDTO não tiver o userId, você precisaria buscar o Order original aqui.
        // Para este exemplo, vamos buscar o Order original SÓ para a verificação de segurança.
        Optional<Order> orderEntityOptional = orderService.findOrderWithItemsById(orderId); // Rebusca para segurança
        if (orderEntityOptional.isPresent()) {
            Order orderEntity = orderEntityOptional.get();
            Optional<Client> clientOpt = clientRepository.findByEmail(principal.getName());
            if (clientOpt.isEmpty() || !orderEntity.getUserId().equals(clientOpt.get().getId())) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdminOrStocker = authentication.getAuthorities().stream()
                        .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_ESTOQUISTA"));
                if (!isAdminOrStocker) {
                    redirectAttributes.addFlashAttribute("error", "Você não tem permissão para visualizar este pedido.");
                    return "redirect:/client/my-orders";
                }
            }
        } else { // Inconsistência, DTO foi encontrado mas entidade não (improvável com a lógica atual)
            redirectAttributes.addFlashAttribute("error", "Erro ao carregar detalhes do pedido.");
            return "redirect:/client/my-orders";
        }


        model.addAttribute("orderDetail", orderDetail); // Passa o DTO para a view

        return "client/order-details";
    }
}
