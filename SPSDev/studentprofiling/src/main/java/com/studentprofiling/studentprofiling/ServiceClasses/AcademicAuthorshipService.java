package com.studentprofiling.studentprofiling.ServiceClasses;

import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.AcademicAuthorshipRepository;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAuthorship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcademicAuthorshipService {

    @Autowired
    private final AcademicAuthorshipRepository repo;
    private final EmailService emailService;

    public AcademicAuthorshipService(AcademicAuthorshipRepository repo, EmailService emailService){
        this.repo = repo;
        this.emailService = emailService;
    }


    public AcademicAuthorship save(AcademicAuthorship a){ return repo.save(a); }
    public List<AcademicAuthorship> list(Integer accountId){ return repo.findByAccountId(accountId); }
    public List<AcademicAuthorship> listByStatus(Integer id, AcademicAuthorship.Status s){ return repo.findByAccountIdAndStatus(id, s); }
    public AcademicAuthorship updateStatus(Long id, AcademicAuthorship.Status s){
        AcademicAuthorship a = repo.findById(id).orElseThrow();
        a.setStatus(s); return repo.save(a);
    }
    public AcademicAuthorship updateStatusAndNotify(Long id, AcademicAuthorship.Status s, String studentEmail){
        AcademicAuthorship a = updateStatus(id, s);

        if (studentEmail != null && !studentEmail.isBlank()) {
            String typePretty = (a.getType() == null) ? "" : a.getType().name().replace('_',' ');
            emailService.sendAuthorshipDecisionEmail(
                studentEmail,           
                s.name(),               
                typePretty,
                a.getDescription(),
                a.getDate(),
                a.getProofPath()
            );
        }
        return a;
    }

    public void deleteAndNotify(Long id, String email, String reason){
        AcademicAuthorship a = repo.findById(id).orElseThrow();
        repo.deleteById(id);
        if (email != null && !email.isBlank()) {
            String typePretty = (a.getType()==null) ? "" : a.getType().name().replace('_',' ');
            emailService.sendAuthorshipDeletionEmail(
                email, typePretty, a.getDescription(), a.getDate(), reason
            );
        }
    }

    public boolean hasPending(Integer id){ return repo.existsByAccountIdAndStatus(id, AcademicAuthorship.Status.PENDING); }

    
}
