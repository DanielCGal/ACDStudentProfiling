package com.studentprofiling.studentprofiling.DatabaseSchema.DAO;

import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicAchievementRepository extends JpaRepository<AcademicAchievement, Long> {

    List<AcademicAchievement> findByAccountId(Integer accountId);
    List<AcademicAchievement> findByAccountIdAndStatus(Integer accountId, AcademicAchievement.Status status);
    boolean existsByAccountIdAndStatus(Integer accountId, AcademicAchievement.Status status);

}