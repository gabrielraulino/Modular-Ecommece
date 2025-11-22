package com.modulith.ecommerce.auth;

/**
 * Public API for the Auth module.
 * This interface allows other modules to access authentication-related functionality
 * while maintaining module boundaries in Spring Modulith.
 */
public interface AuthModuleAPI {

    /**
     * Gets the email of the currently authenticated user from SecurityContext
     * @return User email
     * @throws com.modulith.ecommerce.exception.ResourceNotFoundException if user is not authenticated
     */
    String getCurrentUserEmail();

    /**
     * Gets the ID of the currently authenticated user
     * @return User ID
     * @throws com.modulith.ecommerce.exception.ResourceNotFoundException if user is not authenticated or not found
     */
    Long getCurrentUserId();

    /**
     * Checks if the currently authenticated user has ADMIN role
     * @return true if user is ADMIN, false otherwise
     */
    boolean isAdmin();
}

