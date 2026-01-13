package com.studentprofiling.studentprofiling.DatabaseSchema.Entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "students")
public class Student {

    public Student(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "AccountId", nullable = false)
    private int accountId;

    @Column(name = "FirstName", length = 15)
    private String firstName;

    @Column(name = "MiddleName", length = 15)
    private String middleName;

    @Column(name = "LastName", length = 15)
    private String lastName;

    @Column(name = "Course", length = 10)
    private String course;

    @Column(name = "Gender", nullable = false)
    private String gender;

    @Min(18)
    @Max(100)
    @Column(name = "Age", nullable = false)
    private int age;

    @Column(name = "Birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "YearLevel", length = 10)
    private String yearLevel;

    @Column(name = "ContactInfo", length = 11)
    private String contactInfo;

    @Column(name = "Email", length = 80)
    private String email;

    @Column(name = "Address", length = 50)
    private String address;

    @Column(name = "ParentGuardianName", length = 50)
    private String parentGuardianName;

    @Column(name = "StudentNationality", length = 20)
    private String studentNationality;

    @Column(name = "StudentReligion", length = 20)
    private String studentReligion;

    @Column(name = "SeniorHighSchool", length = 20)
    private String seniorHighSchool;
 
    @Column(name = "ProfilePicture", columnDefinition = "TEXT")
    private String profilePicture;

    @Transient
    private String profilePictureBase64;

    @Column(name = "RegularOrIrregular", length = 20)
    private String regularOrIrregular;

    @Column(name = "ClassSchedule", length = 20)
    private String classSchedule;

    @JsonProperty("id")
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }   

    @JsonProperty("accountId")
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    @JsonProperty("firstName")
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    @JsonProperty("middleName")
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    @JsonProperty("lastName")
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    @JsonProperty("course")
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    @JsonProperty("gender")
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    @JsonProperty("age")
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @JsonProperty("birthdate")
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    @JsonProperty("yearLevel")
    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }

    @JsonProperty("contactInfo")
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    @JsonProperty("email")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonProperty("address")
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @JsonProperty("parentGuardianName")
    public String getParentGuardianName() { return parentGuardianName; }
    public void setParentGuardianName(String parentGuardianName) { this.parentGuardianName = parentGuardianName; }

    @JsonProperty("studentNationality")
    public String getStudentNationality() { return studentNationality; }
    public void setStudentNationality(String studentNationality) { this.studentNationality = studentNationality; }

    @JsonProperty("studentReligion")
    public String getStudentReligion() { return studentReligion; }
    public void setStudentReligion(String studentReligion) { this.studentReligion = studentReligion; }

    @JsonProperty("seniorHighSchool")
    public String getSeniorHighSchool() { return seniorHighSchool; }
    public void setSeniorHighSchool(String seniorHighSchool) { this.seniorHighSchool = seniorHighSchool; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getProfilePictureBase64() { return profilePictureBase64; }
    public void setProfilePictureBase64(String profilePictureBase64) { this.profilePictureBase64 = profilePictureBase64; }

    public String getRegularOrIrregular() { return regularOrIrregular; }
    public void setRegularOrIrregular(String regularOrIrregular) { this.regularOrIrregular = regularOrIrregular; }

    public String getClassSchedule() { return classSchedule; }
    public void setClassSchedule(String classSchedule) { this.classSchedule = classSchedule; }
}
