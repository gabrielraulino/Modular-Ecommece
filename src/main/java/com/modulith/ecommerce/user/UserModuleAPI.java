package com.modulith.ecommerce.user;

import com.modulith.ecommerce.common.Role;

import java.util.Optional;

public interface UserModuleAPI {

    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found
     */
    Optional<UserDTO> findUserById(Long id);


    /**
     * Find user login details by email
     * @param email User email
     * @return User login DTO
     */
    UserLoginDTO findUserByEmail(String email);

    /**
     * Find user ID by email
     * @param email User email
     * @return Optional containing user ID if found
     */
    Optional<Long> findUserIdByEmail(String email);

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
