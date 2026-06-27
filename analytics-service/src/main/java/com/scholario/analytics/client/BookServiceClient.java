package com.scholario.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "book-service", path = "/internal/books")
public interface BookServiceClient {

    @GetMapping("/by-id/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    @GetMapping("/count/by-faculty/{facultyId}")
    long countByFacultyId(@PathVariable("facultyId") Long facultyId);

    @GetMapping("/list/by-faculty/{facultyId}")
    List<BookDto> getBooksByFaculty(@PathVariable("facultyId") Long facultyId);
}
