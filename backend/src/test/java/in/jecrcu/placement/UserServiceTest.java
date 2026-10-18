package in.jecrcu.placement;

import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.NotificationPreferenceRepository;
import in.jecrcu.placement.repository.UserRepository;
import in.jecrcu.placement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "allowedDomains", "jecrcu.edu.in,university.edu");
        ReflectionTestUtils.setField(userService, "requireEmailVerification", false);
    }

    @Test
    void testDomainValidation() {
        assertTrue(userService.isDomainAllowed("student@jecrcu.edu.in"));
        assertTrue(userService.isDomainAllowed("student@university.edu"));
        assertFalse(userService.isDomainAllowed("outsider@gmail.com"));
        assertFalse(userService.isDomainAllowed("invalid-format"));
    }

    @Test
    void testRegisterWithDisallowedDomainThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("hacker@gmail.com", "pass123", "Hacker", "CSE", 2026, BigDecimal.valueOf(8.0), 0)
        );
    }

    @Test
    void testRegisterValidUserSucceeds() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.registerUser("rahul.new@jecrcu.edu.in", "secret123", "Rahul New", "IT", 2026, BigDecimal.valueOf(8.2), 0);

        assertNotNull(user);
        assertEquals("rahul.new@jecrcu.edu.in", user.getEmail());
        assertEquals("hashed_password", user.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
