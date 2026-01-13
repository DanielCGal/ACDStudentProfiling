package com.studentprofiling.studentprofiling.dto;

public class StudentSummary {
    private long id;              
    private int accountId;        
    private String firstName;
    private String middleName;
    private String lastName;
    private String course;
    private String gender;
    private int age;
    private String yearLevel;
    private String contactInfo;
    private String email;
    private String address;
    private String studentNationality;
    private String studentReligion;

    private String classSchedule;        
    private String regularOrIrregular;   

    
    public StudentSummary(long id, int accountId, String firstName, String middleName, String lastName,
                          String course, String gender, int age, String yearLevel,
                          String contactInfo, String email, String address,
                          String studentNationality, String studentReligion,String classSchedule, String regularOrIrregular) {
        this.id = id;
        this.accountId = accountId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.course = course;
        this.gender = gender;
        this.age = age;
        this.yearLevel = yearLevel;
        this.contactInfo = contactInfo;
        this.email = email;
        this.address = address;
        this.studentNationality = studentNationality;
        this.studentReligion = studentReligion;
        this.classSchedule = classSchedule;             
        this.regularOrIrregular = regularOrIrregular;  
    }

    public long getId() { return id; }
    public int getAccountId() { return accountId; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getCourse() { return course; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getYearLevel() { return yearLevel; }
    public String getContactInfo() { return contactInfo; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getStudentNationality() { return studentNationality; }
    public String getStudentReligion() { return studentReligion; }
    // NEW getters
    public String getClassSchedule() { return classSchedule; }
    public String getRegularOrIrregular() { return regularOrIrregular; }
}
