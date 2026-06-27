package com.scholario.violation.repository;

import com.scholario.violation.model.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViolationReportRepository extends JpaRepository<ViolationReport, Long> {
    List<ViolationReport> findByUsername(String username);
    List<ViolationReport> findByResolved(boolean resolved);
}
