package com.modulith.ecommerce.user;

import java.util.Optional;

public interface UserModuleAPI {

    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found
     */
    Optional<UserDTO> findUserById(Long id);

    void validateUserExists(Long id);

    UserLoginDTO findUserByEmail(String email);

    /**
     * Register a new user with specified role
     * @param name User name
     * @param email User email
     * @param password Encrypted password
     * @param role User role (USER or ADMIN)
     * @return Created user DTO
     */
    UserDTO registerUser(String name, String email, String password, Role role);
}
