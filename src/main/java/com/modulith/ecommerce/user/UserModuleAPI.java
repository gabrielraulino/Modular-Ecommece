package com.modulith.ecommerce.user;

import java.util.Optional;

public interface UserModuleAPI {

    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found
     */
    Optional<UserDTO> findUserById(Long id);
}
