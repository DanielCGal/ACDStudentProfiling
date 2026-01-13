package com.studentprofiling.studentprofiling.DatabaseSchema.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AdditionalInfo;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface AdditionalInfoDAO extends JpaRepository<AdditionalInfo, Long>{

    @Query("SELECT s FROM AdditionalInfo s WHERE s.accountId = :accountId")
    Optional<AdditionalInfo> findByAccountId(@Param("accountId") long accountId);

    Optional<AdditionalInfo> findTopByAccountIdOrderByIdDesc(long accountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AdditionalInfo ai SET ai.editUsed = NULL WHERE ai.accountId IN (:accountIds)")
    int resetEditUsedByAccountIds(@Param("accountIds") java.util.List<Integer> accountIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
          UPDATE AdditionalInfo a
          SET a.status = :status
          WHERE a.accountId IN :accountIds
          """)
          int bulkSetStatus(@Param("status") String status,
                            @Param("accountIds") List<Integer> accountIds);

    @Query("""
      SELECT a.accountId
      FROM AdditionalInfo a
      JOIN Student s ON s.accountId = a.accountId
      WHERE s.course = :course
        AND a.status = 'Currently Enrolled'
    """)
    List<Integer> findCurrentlyEnrolledAccountIdsByCourse(@Param("course") String course);


}
