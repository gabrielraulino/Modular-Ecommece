package com.modulith.ecommerce.user.domain;

import com.modulith.ecommerce.common.Role;
import com.modulith.ecommerce.exception.DuplicateResourceException;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.user.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserModuleAPI {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO findById(Long id) {
        return repository.findById(id).map(UserDTO::fromEntity).orElseThrow(() -> new ResourceNotFoundException("User", id));
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
                passwordEncoder.encode(user.password()),
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
                passwordEncoder.encode(user.password()),
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
    public UserLoginDTO findUserByEmail(String email) {
        return UserLoginDTO.fromEntity(repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User","email",email)));
    }

    @Override
    public Optional<Long> findUserIdByEmail(String email) {
        return repository.findByEmail(email).map(User::getId);
    }

    @Override
    public UserDTO registerUser(String name, String email, String password, Role role) {
        if (repository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("User already exists with email: " + email);
        }

        User newUser = new User(
                null,
                name,
                role,
                email,
                password,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return UserDTO.fromEntity(repository.save(newUser));
    }
}
