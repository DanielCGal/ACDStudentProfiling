package com.studentprofiling.studentprofiling.DatabaseSchema.DAO;

import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAuthorship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AcademicAuthorshipRepository extends JpaRepository<AcademicAuthorship, Long> {
    List<AcademicAuthorship> findByAccountId(Integer accountId);
    List<AcademicAuthorship> findByAccountIdAndStatus(Integer accountId, AcademicAuthorship.Status status);
    boolean existsByAccountIdAndStatus(Integer accountId, AcademicAuthorship.Status status);
}
