package com.modulith.ecommerce.user;

public record UserLoginDTO(
    String email,
    String password,
    Role role
) {
    static UserLoginDTO fromEntity(User user) {
        return new UserLoginDTO(
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }
}
