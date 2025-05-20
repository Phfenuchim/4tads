package com.livestock.modules.user.mappers;

import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UserResponseDTO;

public class UserMapper {
    public static UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cpf(user.getCpf())
                .active(user.isActive())
                .roles(user.getRoles())
                .build();
    }
}
