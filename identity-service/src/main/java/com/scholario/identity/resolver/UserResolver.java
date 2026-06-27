package com.scholario.identity.resolver;

import com.scholario.identity.dto.DepartmentInput;
import com.scholario.identity.dto.ProfileInput;
import com.scholario.identity.dto.UserInput;
import com.scholario.identity.model.Department;
import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import com.scholario.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final UserService userService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public User getUserById(@Argument Long id) {
        return userService.getUserById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public User getUserByUsername(@Argument String username) {
        return userService.getUserByUsername(username);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public boolean existsUser(@Argument Long id) {
        return userService.existsUser(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User getMyProfile() {
        return userService.getCurrentUser();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public List<User> getFacultyList() {
        return userService.getFacultyList();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public List<User> getStudentList() {
        return userService.getStudentList();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUnassignedUsers() {
        return userService.getUnassignedUsers();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public List<Department> getDepartments() {
        return userService.getDepartments();
    }

    @MutationMapping
    public User registerUser(@Valid @Argument UserInput input) {
        return userService.registerUser(input);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public User updateUserProfile(@Valid @Argument ProfileInput input) {
        return userService.updateUserProfile(userService.getCurrentUserId(), input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User assignRole(@Argument Long userId, @Argument Role role) {
        return userService.assignRole(userId, role);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User linkFacultyToDepartment(@Argument Long facultyId, @Argument Long departmentId) {
        return userService.linkFacultyToDepartment(facultyId, departmentId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Department createDepartment(@Valid @Argument DepartmentInput input) {
        return userService.createDepartment(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Department updateDepartment(@Argument Long id, @Valid @Argument DepartmentInput input) {
        return userService.updateDepartment(id, input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteDepartment(@Argument Long id) {
        return userService.deleteDepartment(id);
    }
}
