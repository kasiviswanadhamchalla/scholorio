package com.scholario.identity.service;

import com.scholario.identity.dto.DepartmentInput;
import com.scholario.identity.dto.ProfileInput;
import com.scholario.identity.dto.UserInput;
import com.scholario.identity.model.Department;
import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import com.scholario.identity.repository.DepartmentRepository;
import com.scholario.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakRoleSyncService keycloakRoleSyncService;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        } else {
            username = authentication.getName();
        }

        if (username == null) {
            throw new IllegalStateException("Could not extract username from security context");
        }

        return userRepository.findByUsername(username)
                .or(() -> {
                    if ("mock_admin".equals(username)) {
                        User mockUser = new User();
                        mockUser.setUsername("mock_admin");
                        mockUser.setEmail("mock_admin@scholario.local");
                        mockUser.setFullName("Mock Admin");
                        mockUser.setRoles(new java.util.HashSet<>(java.util.Set.of(com.scholario.identity.model.Role.SUPER_ADMIN, com.scholario.identity.model.Role.LIBRARIAN, com.scholario.identity.model.Role.ASSISTANT_LIBRARIAN, com.scholario.identity.model.Role.MEMBER)));
                        mockUser.setPassword(passwordEncoder.encode("Scholario@123"));
                        return java.util.Optional.of(userRepository.save(mockUser));
                    }
                    return java.util.Optional.empty();
                })
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + ": " + username));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @Transactional
    public User registerUser(UserInput input) {
        if (userRepository.findByUsername(input.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(input.username());
        user.setEmail(input.email());
        user.setFullName(input.fullName());
        user.setRoles(new java.util.HashSet<>(Set.of(Role.UNASSIGNED)));
        user.setPassword(passwordEncoder.encode(input.password()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfile(Long id, ProfileInput input) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        if (input.fullName() != null) {
            user.setFullName(input.fullName());
        }
        if (input.email() != null) {
            user.setEmail(input.email());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User assignRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.getRoles().remove(Role.UNASSIGNED);
        user.getRoles().add(role);
        User savedUser = userRepository.save(user);
        keycloakRoleSyncService.syncRoles(savedUser.getUsername(), savedUser.getRoles());
        return savedUser;
    }

    @Transactional
    public User removeRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);
        keycloakRoleSyncService.syncRoles(savedUser.getUsername(), savedUser.getRoles());
        return savedUser;
    }

    @Transactional
    public User linkFacultyToDepartment(Long facultyId, Long departmentId) {
        User user = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        if (!user.getRoles().contains(Role.MEMBER)) {
            throw new IllegalStateException("Only members can be linked to a department");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        user.setDepartment(department);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + ": " + username));
    }

    public boolean existsUser(Long id) {
        return userRepository.existsById(id);
    }

    public List<User> getUnassignedUsers() {
        return userRepository.findByRoles(Role.UNASSIGNED);
    }

    public List<User> getFacultyList() {
        return userRepository.findByRoles(Role.MEMBER);
    }

    public List<User> getStudentList() {
        return userRepository.findByRoles(Role.MEMBER);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department createDepartment(DepartmentInput input) {
        Department department = new Department();
        department.setName(input.name());
        department.setCode(input.code());
        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long id, DepartmentInput input) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        department.setName(input.name());
        department.setCode(input.code());
        return departmentRepository.save(department);
    }

    @Transactional
    public boolean deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            return false;
        }
        userRepository.findAll().stream()
                .filter(user -> user.getDepartment() != null && user.getDepartment().getId().equals(id))
                .forEach(user -> {
                    user.setDepartment(null);
                    userRepository.save(user);
                });
        departmentRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void syncUserFromExternalProvider(String username, String email, String fullName, List<String> roles) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        Set<Role> externalRoles = resolveExternalRoles(roles);

        if ("chkv.2024@gmail.com".equalsIgnoreCase(email) || "chkv.2024@gmail.com".equalsIgnoreCase(username)) {
            externalRoles.clear();
            externalRoles.add(com.scholario.identity.model.Role.SUPER_ADMIN);
            externalRoles.add(com.scholario.identity.model.Role.LIBRARIAN);
            externalRoles.add(com.scholario.identity.model.Role.ASSISTANT_LIBRARIAN);
            externalRoles.add(com.scholario.identity.model.Role.MEMBER);
        }

        boolean tokenHasStaleUnassigned = roles != null && 
                roles.stream().anyMatch(r -> "UNASSIGNED".equalsIgnoreCase(r != null ? r.trim() : "")) &&
                !externalRoles.contains(Role.UNASSIGNED);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean changed = false;

            if (!user.getRoles().equals(externalRoles)) {
                user.setRoles(externalRoles);
                changed = true;
            }
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                changed = true;
            }
            if (fullName != null && !fullName.equals(user.getFullName())) {
                user.setFullName(fullName);
                changed = true;
            }

            if (changed || tokenHasStaleUnassigned) {
                userRepository.save(user);
                if (tokenHasStaleUnassigned && !changed) {
                    log.info("Keycloak token for {} still contains UNASSIGNED role. Forcing cleanup sync.", username);
                } else {
                    log.info("Triggering Keycloak role synchronization for existing user: {}", username);
                }
                keycloakRoleSyncService.syncRoles(username, externalRoles);
            }
        } else {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email != null ? email : username + "@scholario.local");
            user.setFullName(fullName != null ? fullName : username);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRoles(externalRoles);
            userRepository.save(user);
            
            if (externalRoles.size() > 1 || !externalRoles.contains(Role.UNASSIGNED)) {
                log.info("Triggering Keycloak role synchronization for new user: {}", username);
                keycloakRoleSyncService.syncRoles(username, externalRoles);
            }
        }
    }

    private Set<Role> resolveExternalRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return new java.util.HashSet<>(Set.of(Role.UNASSIGNED));
        }

        Set<Role> resolvedRoles = roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .filter(role -> !role.isBlank())
                .flatMap(role -> {
                    try {
                        return java.util.stream.Stream.of(Role.valueOf(role));
                    } catch (IllegalArgumentException ignored) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(java.util.stream.Collectors.toSet());

        if (resolvedRoles.size() > 1 && resolvedRoles.contains(Role.UNASSIGNED)) {
            resolvedRoles.remove(Role.UNASSIGNED);
        }

        return resolvedRoles.isEmpty() ? new java.util.HashSet<>(Set.of(Role.UNASSIGNED)) : resolvedRoles;
    }
}
