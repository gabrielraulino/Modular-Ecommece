package com.modulith.ecommerce.user;

import com.modulith.ecommerce.common.Role;
import com.modulith.ecommerce.user.domain.User;

public record UserLoginDTO(
    String email,
    String password,
    Role role
) {
    public static UserLoginDTO fromEntity(User user) {
        return new UserLoginDTO(
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }
}
