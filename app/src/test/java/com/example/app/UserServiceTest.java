package com.example.app;

import com.example.app.data.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        User user = new User();

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
    }

    @Test
    void shouldUpdateUser() {
        User existing = new User();
        existing.setId(1L);

        User updated = new User();
        updated.setUsername("new");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateUser(1L, updated);

        assertEquals("new", result.getUsername());
    }

    @Test
    void shouldFilterUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(new User()));

        when(userRepository.findByUsernameContainingIgnoreCase("test", pageable))
                .thenReturn(page);

        Page<User> result = userService.getUsers("test", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldGetAllUsersWhenFilterIsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(new User(), new User()));

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getUsers(null, pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldGetAllUsers() {
        List<User> users = List.of(new User(), new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(3, result.size());
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setUsername("janek");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals("janek", result.getUsername());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.getUserById(99L));
    }

    @Test
    void shouldDeleteUser() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldUpdateAllUserFields() {
        User existing = new User();
        existing.setId(1L);

        User updated = new User();
        updated.setUsername("updatedUser");
        updated.setEmail("updated@example.com");
        updated.setFirstName("Jan");
        updated.setLastName("Kowalski");
        updated.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateUser(1L, updated);

        assertEquals("updatedUser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Jan", result.getFirstName());
        assertEquals("Kowalski", result.getLastName());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void shouldTreatEmptyStringFilterAsNoFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(new User(), new User()));

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getUsers("", pageable);

        assertEquals(2, result.getTotalElements());
        verify(userRepository).findAll(pageable);
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(any(), any());
    }

    @Test
    void shouldReturnExactlySavedEntityOnCreate() {
        User user = new User();
        user.setUsername("test");

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldUpdateUserFieldsToNullWhenUpdatedHasNulls() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("stary");
        existing.setEmail("stary@test.com");

        User updated = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateUser(1L, updated);

        assertNull(result.getUsername());
        assertNull(result.getEmail());
    }

    @Test
    void shouldCallDeleteByIdExactlyOnce() {
        userService.deleteUser(42L);

        verify(userRepository, times(1)).deleteById(42L);
    }
}