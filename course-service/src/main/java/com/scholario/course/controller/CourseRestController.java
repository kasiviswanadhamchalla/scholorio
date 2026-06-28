package com.scholario.course.controller;

import com.scholario.course.client.BookDto;
import com.scholario.course.dto.CourseInput;
import com.scholario.course.dto.CourseMaterialInput;
import com.scholario.course.model.Course;
import com.scholario.course.model.CourseMaterial;
import com.scholario.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CourseRestController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(input));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseInput input) {
        return ResponseEntity.ok(courseService.updateCourse(id, input));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<Course>> getCoursesByFaculty(@PathVariable Long facultyId) {
        return ResponseEntity.ok(courseService.getCoursesByFaculty(facultyId));
    }

    @GetMapping
    public ResponseEntity<List<Course>> searchCourses(
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String title) {
        return ResponseEntity.ok(courseService.searchCourses(courseCode, title));
    }

    @PostMapping("/materials")
    public ResponseEntity<CourseMaterial> assignBookToCourse(@Valid @RequestBody CourseMaterialInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.assignBookToCourse(input));
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<CourseMaterial> updateCourseMaterial(@PathVariable Long id, @RequestParam Boolean mandatory) {
        return ResponseEntity.ok(courseService.updateCourseMaterial(id, mandatory));
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<CourseMaterial> removeBookFromCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.removeBookFromCourse(id));
    }

    @GetMapping("/{id}/materials")
    public ResponseEntity<List<CourseMaterial>> getCourseMaterials(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseMaterials(id));
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<BookDto>> getBooksByCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getBooksByCourse(id));
    }

    @GetMapping("/count/faculty/{facultyId}")
    public ResponseEntity<Long> countByFacultyId(@PathVariable Long facultyId) {
        return ResponseEntity.ok(courseService.countByFacultyId(facultyId));
    }
}
