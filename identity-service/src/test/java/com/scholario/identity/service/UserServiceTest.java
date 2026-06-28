package com.scholario.identity.service;

import com.scholario.identity.dto.DepartmentInput;
import com.scholario.identity.dto.ProfileInput;
import com.scholario.identity.dto.UserInput;
import com.scholario.identity.model.Department;
import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import com.scholario.identity.repository.DepartmentRepository;
import com.scholario.identity.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private KeycloakRoleSyncService keycloakRoleSyncService;

    @InjectMocks
    private UserService userService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    private void mockAuthentication(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(username);
        when(auth.getName()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGetCurrentUser_Success() {
        mockAuthentication("testuser");
        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User current = userService.getCurrentUser();
        assertNotNull(current);
        assertEquals(10L, current.getId());
        assertEquals(10L, userService.getCurrentUserId());
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        SecurityContextHolder.setContext(mock(SecurityContext.class));
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserInput input = new UserInput("testuser", "test@test.com", "Test User", "password");
        User registered = userService.registerUser(input);

        assertNotNull(registered);
        assertEquals("testuser", registered.getUsername());
        assertEquals("hashed", registered.getPassword());
        assertTrue(registered.getRoles().contains(Role.UNASSIGNED));
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));
        UserInput input = new UserInput("testuser", "test@test.com", "Test User", "password");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(input));
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        UserInput input = new UserInput("testuser", "test@test.com", "Test User", "password");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(input));
    }

    @Test
    void testUpdateUserProfile_Success() {
        User user = new User();
        user.setId(10L);
        user.setFullName("Old Name");
        user.setEmail("old@test.com");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileInput input = new ProfileInput("New Name", "new@test.com");
        User updated = userService.updateUserProfile(10L, input);

        assertEquals("New Name", updated.getFullName());
        assertEquals("new@test.com", updated.getEmail());
    }

    @Test
    void testAssignRole_Success() {
        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setRoles(new HashSet<>(List.of(Role.UNASSIGNED)));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.assignRole(10L, Role.MEMBER);

        assertFalse(updated.getRoles().contains(Role.UNASSIGNED));
        assertTrue(updated.getRoles().contains(Role.MEMBER));
        verify(keycloakRoleSyncService).syncRoles("testuser", Set.of(Role.MEMBER));
    }

    @Test
    void testRemoveRole_Success() {
        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setRoles(new HashSet<>(List.of(Role.MEMBER)));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.removeRole(10L, Role.MEMBER);

        assertFalse(updated.getRoles().contains(Role.MEMBER));
        verify(keycloakRoleSyncService).syncRoles("testuser", Collections.emptySet());
    }

    @Test
    void testLinkFacultyToDepartment_Success() {
        User user = new User();
        user.setId(10L);
        user.setRoles(Set.of(Role.MEMBER));

        Department dept = new Department();
        dept.setId(100L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(100L)).thenReturn(Optional.of(dept));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User linked = userService.linkFacultyToDepartment(10L, 100L);

        assertNotNull(linked.getDepartment());
        assertEquals(100L, linked.getDepartment().getId());
    }

    @Test
    void testLinkFacultyToDepartment_NotMember() {
        User user = new User();
        user.setId(10L);
        user.setRoles(Set.of(Role.UNASSIGNED));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> userService.linkFacultyToDepartment(10L, 100L));
    }

    @Test
    void testDepartmentCRUD() {
        Department dept = new Department();
        dept.setId(100L);

        when(departmentRepository.findAll()).thenReturn(List.of(dept));
        assertEquals(1, userService.getDepartments().size());

        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));
        DepartmentInput input = new DepartmentInput("Computer Science", "CS");
        Department created = userService.createDepartment(input);
        assertEquals("Computer Science", created.getName());
        assertEquals("CS", created.getCode());

        when(departmentRepository.findById(100L)).thenReturn(Optional.of(dept));
        Department updated = userService.updateDepartment(100L, input);
        assertEquals("Computer Science", updated.getName());

        when(departmentRepository.existsById(100L)).thenReturn(true);
        User u = new User();
        u.setDepartment(dept);
        when(userRepository.findAll()).thenReturn(List.of(u));

        assertTrue(userService.deleteDepartment(100L));
        assertNull(u.getDepartment());
        verify(departmentRepository).deleteById(100L);

        when(departmentRepository.existsById(200L)).thenReturn(false);
        assertFalse(userService.deleteDepartment(200L));
    }

    @Test
    void testSyncUserFromExternalProvider_ExistingUser() {
        User user = new User();
        user.setUsername("externalUser");
        user.setEmail("old@test.com");
        user.setFullName("Old Name");
        user.setRoles(new HashSet<>(List.of(Role.UNASSIGNED)));

        when(userRepository.findByUsername("externalUser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.syncUserFromExternalProvider("externalUser", "new@test.com", "New Name", List.of("MEMBER"));

        assertEquals("new@test.com", user.getEmail());
        assertEquals("New Name", user.getFullName());
        assertTrue(user.getRoles().contains(Role.MEMBER));
        verify(keycloakRoleSyncService).syncRoles("externalUser", Set.of(Role.MEMBER));
    }

    @Test
    void testSyncUserFromExternalProvider_NewUser() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.syncUserFromExternalProvider("newUser", "new@test.com", "New User", List.of("MEMBER"));

        verify(userRepository).save(any(User.class));
        verify(keycloakRoleSyncService).syncRoles(eq("newUser"), eq(Set.of(Role.MEMBER)));
    }

    @Test
    void testQueriesAndHelpers() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        assertEquals(user, userService.getUserById(10L));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertEquals(user, userService.getUserByUsername("testuser"));

        when(userRepository.existsById(10L)).thenReturn(true);
        assertTrue(userService.existsUser(10L));

        when(userRepository.findByRoles(Role.UNASSIGNED)).thenReturn(List.of(user));
        assertEquals(1, userService.getUnassignedUsers().size());

        when(userRepository.findByRoles(Role.MEMBER)).thenReturn(List.of(user));
        assertEquals(1, userService.getFacultyList().size());
        assertEquals(1, userService.getStudentList().size());

        when(userRepository.findAll()).thenReturn(List.of(user));
        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(10L));
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername("unknown"));
    }

    @Test
    void testUpdateUserProfile_NotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfile(10L, new ProfileInput("Name", "test@test.com")));
    }

    @Test
    void testAssignRole_NotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.assignRole(10L, Role.MEMBER));
    }

    @Test
    void testRemoveRole_NotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.removeRole(10L, Role.MEMBER));
    }

    @Test
    void testLinkFacultyToDepartment_FacultyNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.linkFacultyToDepartment(10L, 100L));
    }

    @Test
    void testLinkFacultyToDepartment_DepartmentNotFound() {
        User user = new User();
        user.setId(10L);
        user.setRoles(new HashSet<>(List.of(Role.MEMBER)));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.linkFacultyToDepartment(10L, 100L));
    }

    @Test
    void testUpdateDepartment_NotFound() {
        when(departmentRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.updateDepartment(100L, new DepartmentInput("Name", "Code")));
    }

    @Test
    void testSyncUserFromExternalProvider_ExistingUser_NoChange() {
        User user = new User();
        user.setUsername("externalUser");
        user.setEmail("new@test.com");
        user.setFullName("New Name");
        user.setRoles(new HashSet<>(List.of(Role.MEMBER)));

        when(userRepository.findByUsername("externalUser")).thenReturn(Optional.of(user));

        userService.syncUserFromExternalProvider("externalUser", "new@test.com", "New Name", List.of("MEMBER"));

        verify(userRepository, never()).save(any(User.class));
        verify(keycloakRoleSyncService, never()).syncRoles(anyString(), anySet());
    }

    @Test
    void testResolveExternalRoles_NullOrEmpty() {
        // null roles list
        userService.syncUserFromExternalProvider("user", "email@test.com", "Name", null);
        verify(userRepository).save(argThat(u -> u.getRoles().size() == 1 && u.getRoles().contains(Role.UNASSIGNED)));
    }

    @Test
    void testResolveExternalRoles_InvalidRole() {
        // Invalid role name like "INVALID"
        reset(userRepository);
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        userService.syncUserFromExternalProvider("user", "email@test.com", "Name", List.of("INVALID", "MEMBER"));
        verify(userRepository).save(argThat(u -> u.getRoles().size() == 1 && u.getRoles().contains(Role.MEMBER)));
    }

    @Test
    void testResolveExternalRoles_UnassignedCleanup() {
        reset(userRepository);
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        userService.syncUserFromExternalProvider("user", "email@test.com", "Name", List.of("UNASSIGNED", "MEMBER", "LIBRARIAN"));
        verify(userRepository).save(argThat(u -> u.getRoles().size() == 2 && u.getRoles().contains(Role.MEMBER) && u.getRoles().contains(Role.LIBRARIAN)));
    }
}
