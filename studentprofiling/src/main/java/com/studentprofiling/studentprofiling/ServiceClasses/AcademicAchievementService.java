package com.studentprofiling.studentprofiling.ServiceClasses;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.AcademicAchievementRepository;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAchievement;

import com.studentprofiling.studentprofiling.ServiceClasses.EmailService;
import lombok.RequiredArgsConstructor;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAchievement;

@Service
public class AcademicAchievementService {

    @Autowired
    private final AcademicAchievementRepository repo;
    private final EmailService emailService;

    public AcademicAchievementService(AcademicAchievementRepository repo,
                                      EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    public AcademicAchievement save(AcademicAchievement a){ return repo.save(a); }
    public List<AcademicAchievement> list(Integer accountId){ return repo.findByAccountId(accountId); }
    public List<AcademicAchievement> listByStatus(Integer id, AcademicAchievement.Status s){ return repo.findByAccountIdAndStatus(id, s); }
    public AcademicAchievement updateStatus(Long id, AcademicAchievement.Status s){
        AcademicAchievement a = repo.findById(id).orElseThrow();
        a.setStatus(s); return repo.save(a);
    }

    
    public AcademicAchievement updateStatusAndNotify(Long id, AcademicAchievement.Status s, String studentEmail){
        AcademicAchievement a = updateStatus(id, s);
        if (studentEmail != null && !studentEmail.isBlank()) {
            
            emailService.sendAchievementDecisionEmail(
                studentEmail,                   
                s.name(),                       
                a.getAchievement(),             
                a.getDescription(),
                a.getDate(),
                a.getProofPath()                
            );
        }
        return a;
    }

    public void deleteAndNotify(Long id, String email, String reason){
        AcademicAchievement a = repo.findById(id).orElseThrow();
        repo.deleteById(id);
        if (email != null && !email.isBlank()) {
            emailService.sendAchievementDeletionEmail(
                email, a.getAchievement(), a.getDescription(), a.getDate(), reason
            );
        }
    }

    public boolean hasPending(Integer id){ return repo.existsByAccountIdAndStatus(id, AcademicAchievement.Status.PENDING); }

}
