package com.scholario.course.dto;

import com.scholario.course.model.CourseMaterial;

public record CourseMaterialResponse(
        Long id,
        Long courseId,
        Long bookId,
        boolean mandatory,
        String createdAt,
        String updatedAt
) {
    public static CourseMaterialResponse fromEntity(CourseMaterial material) {
        return new CourseMaterialResponse(
                material.getId(),
                material.getCourse().getId(),
                material.getBookId(),
                material.isMandatory(),
                material.getCreatedAt() != null ? material.getCreatedAt().toString() : null,
                material.getUpdatedAt() != null ? material.getUpdatedAt().toString() : null
        );
    }
}
