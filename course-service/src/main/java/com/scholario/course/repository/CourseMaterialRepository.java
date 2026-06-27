package com.scholario.course.repository;

import com.scholario.course.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {

    List<CourseMaterial> findByCourseId(Long courseId);

    Optional<CourseMaterial> findByCourseIdAndBookId(Long courseId, Long bookId);

    @Query("SELECT cm FROM CourseMaterial cm WHERE cm.course.id = :courseId")
    List<CourseMaterial> findMaterialsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT cm.bookId FROM CourseMaterial cm WHERE cm.course.id = :courseId")
    List<Long> findBookIdsByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);

    long countByCourseIdAndMandatory(Long courseId, boolean mandatory);
}
