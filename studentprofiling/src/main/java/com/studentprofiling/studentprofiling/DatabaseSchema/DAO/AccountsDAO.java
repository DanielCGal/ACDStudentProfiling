package com.studentprofiling.studentprofiling.DatabaseSchema.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Accounts;

@Repository
public interface AccountsDAO extends JpaRepository<Accounts, Long> {

    @Query("SELECT a FROM Accounts a " + "WHERE a.studentEmail = :email AND a.accountPassword = :password")
    Accounts login(@Param("email") String email, @Param("password") String password);

    Optional<Accounts> findByStudentEmail(String studentEmail);
    
    @Query("SELECT a FROM Accounts a WHERE a.accountPassword = :code AND a.userRole LIKE 'slot/%'")
    Accounts findSlotByCode(@Param("code") String code);

    @Query("SELECT a FROM Accounts a WHERE a.userRole LIKE CONCAT(:prefix, '%')")
    java.util.List<Accounts> findByUserRolePrefix(@Param("prefix") String prefix);

    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.userRole LIKE CONCAT(:prefix, '%')")
    long countByUserRolePrefix(@Param("prefix") String prefix);

    @Query("SELECT a.accountPassword FROM Accounts a WHERE a.userRole LIKE CONCAT(:prefix, '%')")
    java.util.List<String> activeSlotCodes(@Param("prefix") String prefix);



}
