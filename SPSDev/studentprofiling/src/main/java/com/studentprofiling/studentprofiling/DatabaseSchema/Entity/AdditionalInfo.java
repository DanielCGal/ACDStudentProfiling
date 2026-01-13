package com.studentprofiling.studentprofiling.DatabaseSchema.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "additional_info")
public class AdditionalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "AccountId", nullable = false)
    private int accountId;

    @Column(name = "WorkingStudent")
    private Boolean workingStudent;

    @Column(name = "PlaceofWork", length = 100)
    private String placeOfWork;

    @Column(name = "ShiftHours", length = 50)
    private String shiftHours;

    @Column(name = "BreadWinner")
    private Boolean breadwinner;

    @Column(name = "SelfFinancing")
    private Boolean selfFinancing;

    @Column(name = "Orphan")
    private Boolean orphan;

    @Column(name = "NameOfParentMother", length = 100)
    private String nameOfParentMother;

    @Column(name = "NameOfParentFather", length = 100)
    private String nameOfParentFather;

    @Column(name = "DateLossMother")
    private LocalDate dateLossMother;

    @Column(name = "DateLossFather")
    private LocalDate dateLossFather;

    @Column(name = "CurrentGuaridan", length = 100)
    private String currentGuardian;

    @Column(name = "GuardianRelationship", length = 15)
    private String guardianRelationship;

    @Column(name = "GuardianOccupation", length = 100)
    private String guardianOccupation;

    @Column(name = "GuardianIncome", length = 100)
    private String guardianIncome;

    @Column(name = "DavaoPermanetResidence")
    private boolean davaoPermanentResidence;

    @Column(name = "CurrentAddress", length = 100)
    private String currentAddress;

    @Column(name = "LivingCondition", length = 50)
    private String livingCondition;

    @Column(name = "EditUsed")
    private Boolean editUsed;

    @Column(name = "SupervisorName", length = 50)
    private String supervisorName;

    @Column(name = "SupervisorContact", length = 20)
    private String supervisorContact;

    @Column(name = "WorkingStudentType", length = 20)
    private String workingStudentType;

    @Column(name = "Department", length = 40)
    private String department;

    @Column(name = "Region", length = 100)
    private String region;

    @Column(name = "HasScholarship")
    private Boolean hasScholarship;

    @Column(name = "ScholarshipType", length = 50)
    private String scholarshipType;

    @Column(name = "GovernmentScholarship", length = 80)
    private String governmentScholarship;

    @Column(name = "Status", length = 30, nullable = false)
    private String status;

    public void setId(Long id) {
        this.id = id;
    }

    public int getId() {
        return id.intValue();
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public Boolean getWorkingStudent() {
        return workingStudent;
    }

    public void setWorkingStudent(Boolean workingStudent) {
        this.workingStudent = workingStudent;
    }

    public String getPlaceOfWork() {
        return placeOfWork;
    }

    public void setPlaceOfWork(String placeOfWork) {
        this.placeOfWork = placeOfWork;
    }

    public String getShiftHours() {
        return shiftHours;
    }

    public void setShiftHours(String shiftHours) {
        this.shiftHours = shiftHours;
    }

    public Boolean getBreadwinner() {
        return breadwinner;
    }

    public void setBreadwinner(Boolean breadwinner) {
        this.breadwinner = breadwinner;
    }

    public Boolean getSelfFinancing() {
        return selfFinancing;
    }

    public void setSelfFinancing(Boolean selfFinancing) {
        this.selfFinancing = selfFinancing;
    }

    public Boolean getOrphan() {
        return orphan;
    }

    public void setOrphan(Boolean orphan) {
        this.orphan = orphan;
    }

    public LocalDate getDateLossMother() {
        return dateLossMother;
    }

    public void setDateLossMother(LocalDate dateLossMother) {
        this.dateLossMother = dateLossMother;
    }

    public String getNameOfParentMother() { return nameOfParentMother; }
    public void setNameOfParentMother(String v) { this.nameOfParentMother = v; }

    public String getNameOfParentFather() { return nameOfParentFather; }
    public void setNameOfParentFather(String v) { this.nameOfParentFather = v; }

    public LocalDate getDateLossFather() {
        return dateLossFather;
    }

    public void setDateLossFather(LocalDate dateLossFather) {
        this.dateLossFather = dateLossFather;
    }

    public String getCurrentGuardian() {
        return currentGuardian;
    }

    public void setCurrentGuardian(String currentGuardian) {
        this.currentGuardian = currentGuardian;
    }

    public String getGuardianRelationship() {
        return guardianRelationship;
    }

    public void setGuardianRelationship(String guardianRelationship) {
        this.guardianRelationship = guardianRelationship;
    }

    public String getGuardianOccupation() {
        return guardianOccupation;
    }

    public void setGuardianOccupation(String guardianOccupation) {
        this.guardianOccupation = guardianOccupation;
    }

    public String getGuardianIncome() {
        return guardianIncome;
    }

    public void setGuardianIncome(String guardianIncome) {
        this.guardianIncome = guardianIncome;
    }

    public boolean getDavaoPermanentResidence() {
        return davaoPermanentResidence;
    }

    public void setDavaoPermanentResidence(boolean davaoPermanentResidence) {
        this.davaoPermanentResidence = davaoPermanentResidence;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getLivingCondition() {
        return livingCondition;
    }

    public void setLivingCondition(String livingCondition) {
        this.livingCondition = livingCondition;
    }

    public Boolean getEditUsed() { return editUsed; }
    public void setEditUsed(Boolean editUsed) { this.editUsed = editUsed; }

    public String getSupervisorName(){ return supervisorName; }
    public void setSupervisorName(String v){ this.supervisorName = v; }

    public String getSupervisorContact(){ return supervisorContact; }
    public void setSupervisorContact(String v){ this.supervisorContact = v; }

    public String getWorkingStudentType(){ return workingStudentType; }
    public void setWorkingStudentType(String v){ this.workingStudentType = v; }

    public String getDepartment(){ return department; }
    public void setDepartment(String v){ this.department = v; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Boolean getHasScholarship() { return hasScholarship; }
    public void setHasScholarship(Boolean hasScholarship) { this.hasScholarship = hasScholarship; }

    public String getScholarshipType() { return scholarshipType; }
    public void setScholarshipType(String scholarshipType) { this.scholarshipType = scholarshipType; }

    public String getGovernmentScholarship() { return governmentScholarship; }
    public void setGovernmentScholarship(String governmentScholarship) { this.governmentScholarship = governmentScholarship; }

    
}
