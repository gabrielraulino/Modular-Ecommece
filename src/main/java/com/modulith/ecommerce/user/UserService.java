package com.modulith.ecommerce.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService implements UserModuleAPI {
    private final UserRepository repository;

    public UserDTO findByUsername(String username) {
        return repository.findAll().stream()
                .filter(user -> user.getName().equals(username))
                .findFirst().map(UserDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDTO findById(Long id) {
        return repository.findById(id).map(UserDTO::fromEntity).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserDTO> findAll() {
        return repository.findAll().stream().map(UserDTO::fromEntity).collect(Collectors.toList());
    }

    public UserDTO saveUser(UserCreateDTO user) {
        if (repository.findByEmail(user.email()).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User newUser = new User(
                null,
                user.name(),
                user.email(),
                hashPassword(user.password()), // hash the password (temporary solution)
                LocalDateTime.now()
        );
        return UserDTO.fromEntity(repository.save(newUser));
    }

    public UserDTO updateUser(Long id, UserCreateDTO user) {
        User existingUser = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        User updatedUser = new User(
                existingUser.getId(),
                user.name(),
                user.email(),
                hashPassword(user.password()), // hash the password (temporary solution)
                existingUser.getCreatedAt()
        );

        return UserDTO.fromEntity(repository.save(updatedUser));
    }

    public void deleteUser(Long id) {
        try {
            findById(id);
        } catch (RuntimeException e) {
            throw new RuntimeException("User not found");
        }
        repository.deleteById(id);
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

    @Override
    public Optional<UserDTO> findUserById(Long id) {
        return repository.findById(id).map(UserDTO::fromEntity);
        // return Optional.of(repository.findById(id)
        //         .map(user -> new UserDTO(
        //                 user.getId(),
        //                 user.getName(),
        //                 user.getEmail(),
        //                 user.getCreatedAt()
        //         ))
        // ).orElse(Optional.empty());
    }
}
