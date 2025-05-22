package com.livestock.modules.order.repositories;

import com.livestock.modules.order.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId); // Você já deve ter algo assim para "meus pedidos"
    List<Order> findAllByOrderByCreatedAtDesc(); // NOVO: Para listar todos os pedidos para o admin/estoquista
}
