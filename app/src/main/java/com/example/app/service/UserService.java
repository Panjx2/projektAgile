package com.example.app.service;

import com.example.app.data.User;
import com.example.app.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User updateUser(Long id, User updated) {

        User user = getUserById(id);

        user.setUsername(updated.getUsername());
        user.setEmail(updated.getEmail());
        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setRole(updated.getRole());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> getUsers(String username, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        }

        return userRepository.findAll(pageable);
    }
}