package com.scholario.book.repository;

import com.scholario.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByTitleContainingIgnoreCaseAndIsbnContaining(String title, String isbn);

    List<Book> findByFacultyId(Long facultyId);

    @Query("SELECT b FROM Book b WHERE b.parentBookId = :parentBookId ORDER BY b.versionNumber DESC")
    List<Book> findVersionsByParentBookId(@Param("parentBookId") Long parentBookId);

    @Query("SELECT b FROM Book b WHERE b.parentBookId IS NULL AND b.id = :id OR b.parentBookId = :id")
    List<Book> findBookWithVersions(@Param("id") Long id);

    long countByFacultyId(Long facultyId);
}
