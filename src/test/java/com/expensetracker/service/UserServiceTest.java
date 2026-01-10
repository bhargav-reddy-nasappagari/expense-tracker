package com.expensetracker.service;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(BCrypt.hashpw("SecurePass1!", BCrypt.gensalt()));
        testUser.setEmailVerified(true); // Required for login to pass
    }

    @Test
    void testRegister_Success() {
        // Use a completely new username/email
        String newUsername = "brandnewuser";
        String newEmail = "brandnew@example.com";

        when(userRepository.findByUsernameIgnoreCase(eq(newUsername))).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(eq(newEmail))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = userService.register(newUsername, "SecurePass1!", "New User", newEmail, "1234567890");

        assertNotNull(result.getId());
        assertEquals(newUsername, result.getUsername());

        verify(userRepository).findByUsernameIgnoreCase(newUsername);
        verify(userRepository).findByEmailIgnoreCase(newEmail);
        verify(userRepository).save(any(User.class));
        verify(categoryRepository, times(7)).save(any());
    }

    @Test
    void testRegister_DuplicateUsername() {
        when(userRepository.findByUsernameIgnoreCase(eq("testuser"))).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
            userService.register("TestUser", "SecurePass1!", "Name", "email@ex.com", "1234567890"));
    }

    @Test
    void testRegister_DuplicateEmail() {
        when(userRepository.findByUsernameIgnoreCase(eq("newuser"))).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(eq("test@example.com"))).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
            userService.register("newuser", "SecurePass1!", "Name", "test@example.com", "1234567890"));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsernameIgnoreCase(eq("testuser"))).thenReturn(Optional.of(testUser));

        User loggedIn = userService.login("testuser", "SecurePass1!");

        assertNotNull(loggedIn);
        assertEquals(testUser.getId(), loggedIn.getId());
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByUsernameIgnoreCase(eq("testuser"))).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
            userService.login("testuser", "WrongPass1!"));
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsernameIgnoreCase(eq("ghost"))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            userService.login("ghost", "SecurePass1!"));
    }

    @Test
    void testUpdateProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.isEmailTakenByAnotherUser("new@email.com", 1L)).thenReturn(false);
        doNothing().when(userRepository).updateProfile(any(User.class));

        userService.updateProfile(1L, "New Name", "new@email.com", "0987654321");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).updateProfile(captor.capture());

        User updated = captor.getValue();
        assertEquals("New Name", updated.getFullName());
        assertEquals("new@email.com", updated.getEmail());
        assertEquals("0987654321", updated.getPhone());
    }

    @Test
    void testUpdateProfile_EmailTaken() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(userRepository.isEmailTakenByAnotherUser(eq("taken@email.com"), eq(1L))).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateProfile(1L, "Name", "taken@email.com", "1234567890"));
    }
}