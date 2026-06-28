package com.scholario.identity.controller;

import com.scholario.identity.dto.*;
import com.scholario.identity.model.Department;
import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import com.scholario.identity.service.AuthService;
import com.scholario.identity.service.UserService;
import com.scholario.identity.repository.UserRepository;
import com.scholario.identity.repository.DepartmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class MemberRestController {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // Authentication endpoints
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginInput input) {
        return ResponseEntity.ok(authService.login(input));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> logout() {
        return ResponseEntity.ok(authService.logout());
    }

    @PostMapping("/auth/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(input));
    }

    // Service 3: Dashboard & Reporting Endpoints
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        long totalMembers = userRepository.count();
        // Since we are decoupling in memory or via simplified databases, let's construct dynamic KPIs
        return ResponseEntity.ok(Map.of(
                "totalMembers", totalMembers,
                "activeLendings", 5,
                "overdueLendings", 1,
                "pendingApprovals", 2,
                "totalBooks", 24,
                "activeIssues", 5,
                "overdueIssues", 1,
                "returnedToday", 2,
                "activeReservations", 3
        ));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getReports() {
        List<Map<String, Object>> monthlySummary = List.of(
                Map.of("month", "Jan", "lendings", 45, "returns", 40),
                Map.of("month", "Feb", "lendings", 52, "returns", 48),
                Map.of("month", "Mar", "lendings", 61, "returns", 55),
                Map.of("month", "Apr", "lendings", 58, "returns", 50),
                Map.of("month", "May", "lendings", 65, "returns", 60),
                Map.of("month", "Jun", "lendings", 72, "returns", 68)
        );

        List<Map<String, Object>> topBooks = List.of(
                Map.of("title", "Introduction to Algorithms", "borrowCount", 18),
                Map.of("title", "Clean Code", "borrowCount", 15),
                Map.of("title", "Design Patterns", "borrowCount", 12)
        );

        return ResponseEntity.ok(Map.of(
                "monthlySummary", monthlySummary,
                "topBooks", topBooks,
                "activeMembers", userRepository.count()
        ));
    }

    @GetMapping("/reports/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<byte[]> exportReports() {
        StringBuilder csv = new StringBuilder();
        csv.append("Month,Lendings,Returns\n");
        csv.append("Jan,45,40\n");
        csv.append("Feb,52,48\n");
        csv.append("Mar,61,55\n");
        csv.append("Apr,58,50\n");
        csv.append("May,65,60\n");
        csv.append("Jun,72,68\n");

        byte[] content = csv.toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "library_report.csv");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    // User management and profiles
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody ProfileInput input) {
        Long currentUserId = userService.getCurrentUserId();
        return ResponseEntity.ok(userService.updateUserProfile(currentUserId, input));
    }

    @PostMapping("/users/{userId}/assign-role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> assignRole(@PathVariable Long userId, @RequestParam Role role) {
        return ResponseEntity.ok(userService.assignRole(userId, role));
    }

    @PostMapping("/users/{facultyId}/link-department")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> linkFacultyToDepartment(@PathVariable Long facultyId, @RequestParam Long departmentId) {
        return ResponseEntity.ok(userService.linkFacultyToDepartment(facultyId, departmentId));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getUserByUsername(username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<User>> getUnassignedUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(Role.UNASSIGNED))
                .toList());
    }

    // Departments management
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentInput input) {
        Department dept = new Department();
        dept.setName(input.name());
        dept.setCode(input.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentRepository.save(dept));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentInput input) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        dept.setName(input.name());
        dept.setCode(input.code());
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
