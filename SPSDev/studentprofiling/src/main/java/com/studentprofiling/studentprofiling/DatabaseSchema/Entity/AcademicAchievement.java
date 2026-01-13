package com.studentprofiling.studentprofiling.DatabaseSchema.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "academic_achievements")
public class AcademicAchievement {

    public enum Status { PENDING, ACCEPTED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Integer accountId;

    @Column(length=20, nullable=false)
    private String achievement;           

    @Column(length=100, nullable=false)
    private String description;           

    @Column(length=120)
    private String date;                  

    @Column(length=255)
    private String proofPath;            

    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable=false)
    private Status status = Status.PENDING;       

    
    public Long getId() { return id; }
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getProofPath() { return proofPath; }
    public void setProofPath(String proofPath) { this.proofPath = proofPath; }
    public Status getStatus(){ return status; }
    public void setStatus(Status status){ this.status = status; }


    
}
