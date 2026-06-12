package com.project.service;

import com.project.model.User;
import java.util.List;
import org.springframework.data.domain.Page;

public interface UserService {
    User createUser(User user);

    List<User> getAllUsers();

    Page<User> getUsers(String search, int page, int size);

    User getUserById(Long id);

    User getCurrentUser();

    User updateUser(Long id, User user);

    void deleteUser(Long id);
}
