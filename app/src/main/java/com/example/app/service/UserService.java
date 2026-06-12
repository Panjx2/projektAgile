package com.example.app.service;

import com.example.app.data.Project;
import com.example.app.data.Task;
import com.example.app.data.User;
import com.example.app.repository.MessageRepository;
import com.example.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public UserService(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
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

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
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

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();

        // Remove messages referencing the user (sender_id is NOT NULL, receiver_id is nullable)
        messageRepository.deleteBySenderId(id);
        messageRepository.clearReceiver(id);

        // Unassign tasks so the foreign key on tasks.user_id does not block deletion
        if (user.getTasks() != null) {
            for (Task task : user.getTasks()) {
                task.setAssignedUser(null);
            }
        }

        // Remove the user from every project (owning side of the join table)
        if (user.getProjects() != null) {
            for (Project project : new HashSet<>(user.getProjects())) {
                project.getUsers().remove(user);
            }
            user.getProjects().clear();
        }

        userRepository.delete(user);
    }

    public Page<User> getUsers(String username, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        }

        return userRepository.findAll(pageable);
    }

    public boolean validateUsername(String username){
        return userRepository.existsByUsername(username);
    }

    public boolean validateEmail(String email){
        return userRepository.existsByEmail(email);
    }
}