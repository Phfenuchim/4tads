package com.livestock.modules.client.domain.address;

import com.livestock.modules.client.domain.client.Client;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Client client_id;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    private String country;
    private boolean is_default;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
