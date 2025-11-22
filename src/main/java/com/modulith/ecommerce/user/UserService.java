package com.modulith.ecommerce.user;

import com.modulith.ecommerce.exception.DuplicateResourceException;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserModuleAPI {
    private final UserRepository repository;

    public UserDTO findById(Long id) {
        return repository.findById(id).map(UserDTO::fromEntity).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public List<UserDTO> findAll() {
        return repository.findAll().stream().map(UserDTO::fromEntity).toList();
    }

    public List<UserDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(UserDTO::fromEntity)
                .getContent();
    }

    public UserDTO saveUser(UserCreateDTO user) {
        if (repository.findByEmail(user.email()).isPresent()) {
            throw new DuplicateResourceException("User already exists");
        }
        User newUser = new User(
                null,
                user.name(),
                Role.USER,
                user.email(),
                hashPassword(user.password()), // hash the password (temporary solution)
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return UserDTO.fromEntity(repository.save(newUser));
    }

    public UserDTO updateUser(Long id, UserCreateDTO user) {
        User existingUser = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        User updatedUser = new User(
                existingUser.getId(),
                user.name(),
                existingUser.getRole(),
                user.email(),
                hashPassword(user.password()), // hash the password (temporary solution)
                existingUser.getCreatedAt(),
                LocalDateTime.now()
        );

        return UserDTO.fromEntity(repository.save(updatedUser));
    }

    public void deleteUser(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Override
    public Optional<UserDTO> findUserById(Long id) {
        return repository.findById(id).map(UserDTO::fromEntity);
    }

    @Override
    public void validateUserExists(Long id) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

}
