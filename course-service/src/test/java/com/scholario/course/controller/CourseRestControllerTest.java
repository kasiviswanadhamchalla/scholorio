package com.scholario.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholario.course.client.BookDto;
import com.scholario.course.dto.CourseInput;
import com.scholario.course.dto.CourseMaterialInput;
import com.scholario.course.model.Course;
import com.scholario.course.model.CourseMaterial;
import com.scholario.course.service.CourseService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourseRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseRestController courseRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseRestController).build();
    }

    @Test
    void testCreateCourse() throws Exception {
        Course course = new Course();
        course.setId(100L);
        when(courseService.createCourse(any(CourseInput.class))).thenReturn(course);

        CourseInput input = new CourseInput("CS101", "Intro to CS", "Desc", 10L);

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testUpdateCourse() throws Exception {
        Course course = new Course();
        course.setId(100L);
        when(courseService.updateCourse(eq(100L), any(CourseInput.class))).thenReturn(course);

        CourseInput input = new CourseInput("CS101", "Title", "Desc", 10L);

        mockMvc.perform(put("/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGetCourseById() throws Exception {
        Course course = new Course();
        course.setId(100L);
        when(courseService.getCourseById(100L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        when(courseService.getCourseById(200L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/200"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCoursesByFaculty() throws Exception {
        Course course = new Course();
        course.setId(100L);
        when(courseService.getCoursesByFaculty(10L)).thenReturn(List.of(course));

        mockMvc.perform(get("/faculty/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testSearchCourses() throws Exception {
        Course course = new Course();
        course.setId(100L);
        when(courseService.searchCourses("CS101", "Intro")).thenReturn(List.of(course));

        mockMvc.perform(get("/")
                        .param("courseCode", "CS101")
                        .param("title", "Intro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testAssignBookToCourse() throws Exception {
        CourseMaterial material = new CourseMaterial();
        material.setId(50L);
        when(courseService.assignBookToCourse(any(CourseMaterialInput.class))).thenReturn(material);

        CourseMaterialInput input = new CourseMaterialInput(100L, 1L, true);

        mockMvc.perform(post("/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L));
    }

    @Test
    void testUpdateCourseMaterial() throws Exception {
        CourseMaterial material = new CourseMaterial();
        material.setId(50L);
        when(courseService.updateCourseMaterial(50L, true)).thenReturn(material);

        mockMvc.perform(put("/materials/50")
                        .param("mandatory", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50L));
    }

    @Test
    void testRemoveBookFromCourse() throws Exception {
        CourseMaterial material = new CourseMaterial();
        material.setId(50L);
        when(courseService.removeBookFromCourse(50L)).thenReturn(material);

        mockMvc.perform(delete("/materials/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50L));
    }

    @Test
    void testGetCourseMaterials() throws Exception {
        CourseMaterial material = new CourseMaterial();
        material.setId(50L);
        when(courseService.getCourseMaterials(100L)).thenReturn(List.of(material));

        mockMvc.perform(get("/100/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50L));
    }

    @Test
    void testGetBooksByCourse() throws Exception {
        BookDto book = new BookDto(1L, "Book", "123", 10L, "Desc", 1, "PUBLISHED", null, null, null);
        when(courseService.getBooksByCourse(100L)).thenReturn(List.of(book));

        mockMvc.perform(get("/100/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testCountByFacultyId() throws Exception {
        when(courseService.countByFacultyId(10L)).thenReturn(5L);

        mockMvc.perform(get("/count/faculty/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5L));
    }
}
