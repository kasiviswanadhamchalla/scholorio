package com.scholario.course.resolver;

import com.scholario.course.dto.CourseInput;
import com.scholario.course.dto.CourseMaterialInput;
import com.scholario.course.model.Course;
import com.scholario.course.model.CourseMaterial;
import com.scholario.course.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
public class CourseMutationResolver {

    private final CourseService courseService;

    public CourseMutationResolver(CourseService courseService) {
        this.courseService = courseService;
    }

    @MutationMapping
    public Course createCourse(@Valid @Argument CourseInput input) {
        return courseService.createCourse(input);
    }

    @MutationMapping
    public Course updateCourse(@Argument Long id, @Valid @Argument CourseInput input) {
        return courseService.updateCourse(id, input);
    }

    @MutationMapping
    public CourseMaterial assignBookToCourse(@Valid @Argument CourseMaterialInput input) {
        return courseService.assignBookToCourse(input);
    }

    @MutationMapping
    public CourseMaterial updateCourseMaterial(@Argument Long id, @Argument Boolean mandatory) {
        return courseService.updateCourseMaterial(id, mandatory);
    }

    @MutationMapping
    public CourseMaterial removeBookFromCourse(@Argument Long id) {
        return courseService.removeBookFromCourse(id);
    }
}
