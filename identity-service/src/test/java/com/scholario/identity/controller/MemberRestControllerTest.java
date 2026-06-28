package com.scholario.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholario.identity.dto.*;
import com.scholario.identity.model.Department;
import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import com.scholario.identity.repository.DepartmentRepository;
import com.scholario.identity.repository.UserRepository;
import com.scholario.identity.service.AuthService;
import com.scholario.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MemberRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private MemberRestController memberRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberRestController).build();
    }

    @Test
    void testLogin() throws Exception {
        AuthResponse authResponse = new AuthResponse("access", "refresh", "Bearer", 3600, null);
        when(authService.login(any(LoginInput.class))).thenReturn(authResponse);

        LoginInput input = new LoginInput("testuser", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void testRefresh() throws Exception {
        AuthResponse authResponse = new AuthResponse("access", "refresh", "Bearer", 3600, null);
        when(authService.refreshToken("refresh-token")).thenReturn(authResponse);

        mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", "refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void testLogout() throws Exception {
        when(authService.logout()).thenReturn(true);
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testRegister() throws Exception {
        User user = new User();
        user.setId(10L);
        when(userService.registerUser(any(UserInput.class))).thenReturn(user);

        UserInput input = new UserInput("testuser", "test@test.com", "Name", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void testDashboardAndReports() throws Exception {
        when(userRepository.count()).thenReturn(100L);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(100));

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMembers").value(100));

        mockMvc.perform(get("/reports/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    void testProfile() throws Exception {
        User user = new User();
        user.setId(10L);
        when(userService.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.updateUserProfile(eq(10L), any(ProfileInput.class))).thenReturn(user);

        ProfileInput input = new ProfileInput("New Name", "test@email.com");
        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void testAssignRole() throws Exception {
        User user = new User();
        user.setId(10L);
        when(userService.assignRole(10L, Role.MEMBER)).thenReturn(user);

        mockMvc.perform(post("/users/10/assign-role")
                        .param("role", "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void testLinkFacultyToDepartment() throws Exception {
        User user = new User();
        user.setId(10L);
        when(userService.linkFacultyToDepartment(10L, 100L)).thenReturn(user);

        mockMvc.perform(post("/users/10/link-department")
                        .param("departmentId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void testGetUsers() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));

        when(userService.getUserById(10L)).thenReturn(user);
        mockMvc.perform(get("/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        when(userService.getUserById(20L)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/users/20"))
                .andExpect(status().isNotFound());

        when(userService.getUserByUsername("testuser")).thenReturn(user);
        mockMvc.perform(get("/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        when(userService.getUserByUsername("other")).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/users/username/other"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUnassignedUsers() throws Exception {
        User user = new User();
        user.getRoles().add(Role.UNASSIGNED);
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/unassigned"))
                .andExpect(status().isOk());
    }

    @Test
    void testDepartmentsManagement() throws Exception {
        Department dept = new Department();
        dept.setId(100L);
        when(departmentRepository.findAll()).thenReturn(List.of(dept));

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        when(departmentRepository.save(any(Department.class))).thenReturn(dept);

        DepartmentInput input = new DepartmentInput("CS", "Computer Science");
        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));

        when(departmentRepository.findById(100L)).thenReturn(Optional.of(dept));
        mockMvc.perform(put("/departments/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        mockMvc.perform(delete("/departments/100"))
                .andExpect(status().isOk());
    }
}
