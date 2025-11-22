package com.modulith.ecommerce.auth;

import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CurrentUserService implements AuthModuleAPI {

    private final UserModuleAPI userModuleAPI;

    /**
     * Gets the email of the currently authenticated user from SecurityContext
     * @return User email
     * @throws ResourceNotFoundException if user is not authenticated
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Authentication", "status", "User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        
        throw new ResourceNotFoundException("Authentication", "error", "Unable to extract user email from authentication");
    }

    /**
     * Gets the ID of the currently authenticated user
     * @return User ID
     * @throws ResourceNotFoundException if user is not authenticated or not found
     */
    public Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        return userModuleAPI.findUserIdByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Checks if the currently authenticated user has ADMIN role
     * @return true if user is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}

