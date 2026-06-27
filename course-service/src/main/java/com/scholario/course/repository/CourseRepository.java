package com.scholario.course.repository;

import com.scholario.course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    List<Course> findByFacultyId(Long facultyId);

    @Query("SELECT c FROM Course c WHERE c.courseCode = :courseCode OR c.title LIKE %:title%")
    List<Course> searchCourses(@Param("courseCode") String courseCode, @Param("title") String title);

    long countByFacultyId(Long facultyId);
}
