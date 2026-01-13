package com.studentprofiling.studentprofiling.ServiceClasses;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.AccountsDAO;
import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.AdditionalInfoDAO;
import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.StudentDAO;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AdditionalInfo;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Student;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceAdditionalInfo {

    @Autowired
    private AdditionalInfoDAO additionalInfoDAO;

    @Autowired private StudentDAO studentDAO;

    private AccountsDAO studentRepo;

    public AdditionalInfo saveAdditionalInfo(AdditionalInfo additionalInfo) {
        return additionalInfoDAO.save(additionalInfo);
    }

    @Transactional
    public AdditionalInfo getAdditionalInfo(long accountId) {
        
        return additionalInfoDAO.findTopByAccountIdOrderByIdDesc(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Additional info not found for account ID: " + accountId));
    }

    @Transactional
    public AdditionalInfo upsertByAccountId(long accountId, java.util.function.UnaryOperator<AdditionalInfo> mutator) {
        AdditionalInfo ai = additionalInfoDAO.findTopByAccountIdOrderByIdDesc(accountId)
                .orElseGet(() -> { AdditionalInfo x = new AdditionalInfo(); x.setAccountId((int) accountId); return x; });
        AdditionalInfo mutated = mutator.apply(ai);
        return additionalInfoDAO.save(mutated);
    }

    @Transactional
    public int resetEditUsedForAccounts(java.util.List<Integer> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) return 0;
        return additionalInfoDAO.resetEditUsedByAccountIds(accountIds);
    }

    @Transactional
    public int applyBulkEnrollmentStatus(List<Integer> keepEnrolledIds) {
        Set<Integer> keepSet = new HashSet<>(keepEnrolledIds == null ? Collections.emptyList() : keepEnrolledIds);

        
        List<Student> all = studentDAO.findAll();
        int changes = 0;

        for (Student s : all) {
            int accountId = s.getAccountId();

            
            AdditionalInfo ai = additionalInfoDAO.findByAccountId(accountId)
                    .orElseGet(() -> {
                        AdditionalInfo x = new AdditionalInfo();
                        x.setAccountId(accountId);
                        x.setStatus("Inactive"); 
                        return x;
                    });

            String prev = ai.getStatus() == null ? "Inactive" : ai.getStatus().trim();

            if (keepSet.contains(accountId)) {
                if (!"Currently Enrolled".equalsIgnoreCase(prev)) {
                    ai.setStatus("Currently Enrolled");
                    additionalInfoDAO.save(ai);
                    changes++;
                }
            } else {
                
                if ("Currently Enrolled".equalsIgnoreCase(prev)) {
                    ai.setStatus("NOT Currently Enrolled");
                    additionalInfoDAO.save(ai);
                    changes++;
                } else if ("NOT Currently Enrolled".equalsIgnoreCase(prev)) {
                    ai.setStatus("Inactive");
                    additionalInfoDAO.save(ai);
                    changes++;
                } 
            }
        }
        return changes;
    }

    public List<Integer> currentlyEnrolledAccountIdsByCourse(String course) {
    return additionalInfoDAO.findCurrentlyEnrolledAccountIdsByCourse(course);
}

    @Transactional
    public int graduateEligible(List<Integer> accountIds) {
        
        List<Integer> eligible = accountIds.stream().filter(id -> {
            var s = studentDAO.findByAccountId(id).orElse(null);
            var ai = additionalInfoDAO.findByAccountId(id).orElse(null);
            boolean fourth = s != null && "4th Year".equalsIgnoreCase(
                String.valueOf(s.getYearLevel()).trim()
            );
            boolean ce = ai != null && "currently enrolled".equalsIgnoreCase(
                String.valueOf(ai.getStatus()).trim()
            );
            return fourth && ce;
        }).toList();

        if (eligible.isEmpty()) return 0;
        return additionalInfoDAO.bulkSetStatus("Graduated", eligible);
    }
    
    
}
