package com.studentprofiling.studentprofiling.DatabaseSchema.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Student;
import com.studentprofiling.studentprofiling.dto.StudentSummary;

@Repository
public interface StudentDAO extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE s.accountId = :accountId")
    Optional<Student> findByAccountId(@Param("accountId") long accountId);

    @Query("SELECT s FROM Student s WHERE LOWER(s.course) = LOWER(:course)")
    List<Student> findByCourseIgnoreCase(@Param("course") String course);

    Optional<Student> findTopByAccountIdOrderByIdDesc(long accountId);

    List<Student> findAll();

    @Query("""
        SELECT new com.studentprofiling.studentprofiling.dto.StudentSummary(
            s.id, s.accountId, s.firstName, s.middleName, s.lastName,
            s.course, s.gender, s.age, s.yearLevel,
            s.contactInfo, s.email, s.address,
            s.studentNationality, s.studentReligion, s.classSchedule, s.regularOrIrregular
        )
        FROM Student s
        WHERE LOWER(s.course) = LOWER(:course)
        """)
    List<StudentSummary> findSummaryByCourse(@Param("course") String course);

    @Query("""
    SELECT new com.studentprofiling.studentprofiling.dto.StudentSummary(
        s.id, s.accountId, s.firstName, s.middleName, s.lastName,
        s.course, s.gender, s.age, s.yearLevel,
        s.contactInfo, s.email, s.address,
        s.studentNationality, s.studentReligion, s.classSchedule, s.regularOrIrregular
    )
    FROM Student s
    """)
    List<StudentSummary> findAllSummary();
}
