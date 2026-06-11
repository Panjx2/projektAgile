package com.example.app;

import com.example.app.data.User;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFirstName("Jan");
        u.setLastName("Testowy");
        u.setPassword("$2a$10$hashedpassword");
        u.setRole("ROLE_USER");
        return u;
    }

    @Test
    void shouldSaveAndFindUser() {
        User saved = userRepository.save(buildUser("johndoe", "john@test.com"));

        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
    }

    @Test
    void shouldFindByUsername() {
        userRepository.save(buildUser("uniqueuser", "unique@test.com"));

        Optional<User> result = userRepository.findByUsername("uniqueuser");

        assertTrue(result.isPresent());
        assertEquals("uniqueuser", result.get().getUsername());
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<User> result = userRepository.findByUsername("nieistniejacy");

        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindByEmail() {
        userRepository.save(buildUser("emailuser", "mail@company.com"));

        Optional<User> result = userRepository.findByEmail("mail@company.com");

        assertTrue(result.isPresent());
        assertEquals("emailuser", result.get().getUsername());
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        userRepository.save(buildUser("existing", "ex@test.com"));

        boolean exists = userRepository.existsByUsername("existing");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenUsernameNotExists() {
        boolean exists = userRepository.existsByUsername("nieistniejacy123");

        assertFalse(exists);
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        userRepository.save(buildUser("mailcheck", "check@mail.com"));

        boolean exists = userRepository.existsByEmail("check@mail.com");

        assertTrue(exists);
    }

    @Test
    void shouldFindByUsernameContainingIgnoreCase() {
        userRepository.save(buildUser("jankowalski", "jan@test.com"));
        userRepository.save(buildUser("janowiak", "janowiak@test.com"));
        userRepository.save(buildUser("piotrjan", "piotrjan@test.com"));
        userRepository.save(buildUser("marekwisniew", "marek@test.com"));

        Page<User> result = userRepository.findByUsernameContainingIgnoreCase(
                "jan", PageRequest.of(0, 10));

        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(u -> u.getUsername().toLowerCase().contains("jan")));
    }

    @Test
    void shouldFindByUsernameIgnoringCase() {
        userRepository.save(buildUser("MixedCase", "mixed@test.com"));

        Page<User> result = userRepository.findByUsernameContainingIgnoreCase(
                "MIXED", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldReturnAllUsersWhenSearchingPartialUsername() {
        userRepository.save(buildUser("admin", "admin@test.com"));
        userRepository.save(buildUser("superadmin", "superadmin@test.com"));
        userRepository.save(buildUser("user1", "user1@test.com"));

        Page<User> result = userRepository.findByUsernameContainingIgnoreCase(
                "admin", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldDeleteUser() {
        User saved = userRepository.save(buildUser("tousuniecia", "delete@test.com"));
        Long id = saved.getId();

        userRepository.deleteById(id);

        assertFalse(userRepository.findById(id).isPresent());
    }
}
