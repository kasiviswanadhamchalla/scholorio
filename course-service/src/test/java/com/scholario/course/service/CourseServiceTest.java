package com.scholario.course.service;

import com.scholario.course.client.BookDto;
import com.scholario.course.client.BookServiceClient;
import com.scholario.course.client.IdentityServiceClient;
import com.scholario.course.client.UserDto;
import com.scholario.course.dto.CourseInput;
import com.scholario.course.dto.CourseMaterialInput;
import com.scholario.course.model.Course;
import com.scholario.course.model.CourseMaterial;
import com.scholario.course.repository.CourseMaterialRepository;
import com.scholario.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMaterialRepository courseMaterialRepository;
    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;

    @InjectMocks
    private CourseService courseService;

    @Test
    void testCreateCourse_Success() {
        UserDto faculty = new UserDto(10L, "f", "e", "Faculty Name", Set.of("FACULTY"));
        when(identityServiceClient.getUserById(10L)).thenReturn(faculty);
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseInput input = new CourseInput("CS101", "Intro to CS", "Desc", 10L);
        Course created = courseService.createCourse(input);

        assertNotNull(created);
        assertEquals("CS101", created.getCourseCode());
        assertEquals("Intro to CS", created.getTitle());
    }

    @Test
    void testCreateCourse_DuplicateCode() {
        UserDto faculty = new UserDto(10L, "f", "e", "Faculty Name", Set.of("FACULTY"));
        when(identityServiceClient.getUserById(10L)).thenReturn(faculty);
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(new Course()));

        CourseInput input = new CourseInput("CS101", "Intro to CS", "Desc", 10L);
        assertThrows(IllegalArgumentException.class, () -> courseService.createCourse(input));
    }

    @Test
    void testCreateCourse_UserNotFound() {
        when(identityServiceClient.getUserById(10L)).thenReturn(null);
        CourseInput input = new CourseInput("CS101", "Intro to CS", "Desc", 10L);
        assertThrows(IllegalArgumentException.class, () -> courseService.createCourse(input));
    }

    @Test
    void testUpdateCourse_Success() {
        Course course = new Course();
        course.setId(100L);
        course.setTitle("Old Title");

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseInput input = new CourseInput("CS101", "New Title", "New Desc", null);
        Course updated = courseService.updateCourse(100L, input);

        assertEquals("New Title", updated.getTitle());
        assertEquals("New Desc", updated.getDescription());
    }

    @Test
    void testUpdateCourse_WithFaculty() {
        Course course = new Course();
        course.setId(100L);

        UserDto faculty = new UserDto(10L, "f", "e", "Faculty Name", Set.of("FACULTY"));
        when(identityServiceClient.getUserById(10L)).thenReturn(faculty);
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseInput input = new CourseInput("CS101", "Title", "Desc", 10L);
        Course updated = courseService.updateCourse(100L, input);

        assertEquals(10L, updated.getFacultyId());
    }

    @Test
    void testSearchCourses() {
        Course course = new Course();
        course.setCourseCode("CS101");

        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course));
        assertEquals(1, courseService.searchCourses("CS101", null).size());

        when(courseRepository.searchCourses(null, "Intro")).thenReturn(List.of(course));
        assertEquals(1, courseService.searchCourses(null, "Intro").size());

        when(courseRepository.findAll()).thenReturn(List.of(course));
        assertEquals(1, courseService.searchCourses(null, null).size());
    }

    @Test
    void testAssignBookToCourse_Success() {
        Course course = new Course();
        course.setId(100L);

        BookDto book = new BookDto(1L, "Book", "123", 10L, "Desc", 1, "PUBLISHED", null, null, null);

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(bookServiceClient.getBookById(1L)).thenReturn(book);
        when(courseMaterialRepository.findByCourseIdAndBookId(100L, 1L)).thenReturn(Optional.empty());
        when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseMaterialInput input = new CourseMaterialInput(100L, 1L, true);
        CourseMaterial material = courseService.assignBookToCourse(input);

        assertNotNull(material);
        assertEquals(course, material.getCourse());
        assertEquals(1L, material.getBookId());
        assertTrue(material.isMandatory());
    }

    @Test
    void testAssignBookToCourse_NonPublished() {
        Course course = new Course();
        course.setId(100L);
        BookDto book = new BookDto(1L, "Book", "123", 10L, "Desc", 1, "DRAFT", null, null, null);

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(bookServiceClient.getBookById(1L)).thenReturn(book);

        CourseMaterialInput input = new CourseMaterialInput(100L, 1L, true);
        assertThrows(IllegalStateException.class, () -> courseService.assignBookToCourse(input));
    }

    @Test
    void testUpdateCourseMaterial() {
        CourseMaterial material = new CourseMaterial();
        material.setId(50L);
        material.setMandatory(false);

        when(courseMaterialRepository.findById(50L)).thenReturn(Optional.of(material));
        when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseMaterial updated = courseService.updateCourseMaterial(50L, true);
        assertTrue(updated.isMandatory());
    }

    @Test
    void testRemoveBookFromCourse() {
        CourseMaterial material = new CourseMaterial();
        material.setId(50L);

        when(courseMaterialRepository.findById(50L)).thenReturn(Optional.of(material));

        CourseMaterial removed = courseService.removeBookFromCourse(50L);
        assertEquals(material, removed);
        verify(courseMaterialRepository).delete(material);
    }

    @Test
    void testGetBooksByCourse() {
        when(courseMaterialRepository.findBookIdsByCourseId(100L)).thenReturn(List.of(1L));
        BookDto book = new BookDto(1L, "Book", "123", 10L, "Desc", 1, "PUBLISHED", null, null, null);
        when(bookServiceClient.getBooksByIds(List.of(1L))).thenReturn(List.of(book));

        List<BookDto> list = courseService.getBooksByCourse(100L);
        assertEquals(1, list.size());

        when(courseMaterialRepository.findBookIdsByCourseId(200L)).thenReturn(new ArrayList<>());
        assertTrue(courseService.getBooksByCourse(200L).isEmpty());
    }

    @Test
    void testGetCourseById() {
        Course c = new Course();
        when(courseRepository.findById(100L)).thenReturn(Optional.of(c));
        assertTrue(courseService.getCourseById(100L).isPresent());
    }

    @Test
    void testGetCoursesByFaculty() {
        Course c = new Course();
        when(courseRepository.findByFacultyId(10L)).thenReturn(List.of(c));
        assertEquals(1, courseService.getCoursesByFaculty(10L).size());
    }

    @Test
    void testCountByFacultyId() {
        when(courseRepository.countByFacultyId(10L)).thenReturn(5L);
        assertEquals(5L, courseService.countByFacultyId(10L));
    }
}
