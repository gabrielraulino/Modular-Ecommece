package com.modulith.ecommerce.user;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@AllArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping()
    public List<UserDTO> getAllUsers() {
        return service.findAll();
    }

    @PostMapping()
    public UserDTO createUser(@RequestBody UserCreateDTO user) {
        return service.saveUser(user);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserCreateDTO user) {
        return service.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }


}
