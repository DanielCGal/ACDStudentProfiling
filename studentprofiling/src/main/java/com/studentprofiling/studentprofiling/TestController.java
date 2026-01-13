package com.studentprofiling.studentprofiling;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Accounts;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AdditionalInfo;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Student;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAchievement;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAuthorship;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.AcademicAuthorship.Type;

import com.studentprofiling.studentprofiling.ServiceClasses.FileStorageService;
import com.studentprofiling.studentprofiling.ServiceClasses.ServiceAccounts;
import com.studentprofiling.studentprofiling.ServiceClasses.ServiceAdditionalInfo;
import com.studentprofiling.studentprofiling.ServiceClasses.ServiceStudent;
import com.studentprofiling.studentprofiling.ServiceClasses.AcademicAchievementService;
import com.studentprofiling.studentprofiling.ServiceClasses.AcademicAuthorshipService;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.annotation.Transactional;

import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.AccountsDAO;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Accounts;
import com.studentprofiling.studentprofiling.ServiceClasses.EmailService;

import com.studentprofiling.studentprofiling.dto.StudentSummary;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("studentprofiling")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired private ServiceAccounts serviceAccounts;
    @Autowired private ServiceStudent serviceStudent;
    @Autowired private ServiceAdditionalInfo serviceAdditionalInfo;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private AcademicAchievementService serviceAcademicAchievement;
    @Autowired private AcademicAuthorshipService serviceAcademicAuthorship;

    @Autowired private AccountsDAO accountsDAO;
    @Autowired private EmailService emailService;

    private static final SecureRandom RND = new SecureRandom();
    private static final ConcurrentHashMap<String, PendingLogin> PENDING = new ConcurrentHashMap<>();

    static class Step1Request { public String email; public String password; }
    static class VerifyRequest { public String tempId; public String code; }

    static class PendingLogin {
        String email;
        String redirect;
        String role;
        String code;
        Instant expiresAt;
        int attempts; 
    }
    

    @GetMapping("/test")
    public String test() { return "Test successful!"; }

    @PostMapping("/login")
    public String login(@RequestBody Accounts loginRequest) {
        Accounts account = serviceAccounts.login(
            loginRequest.getStudentEmail(),
            loginRequest.getAccountPassword()
        );
        if (account == null) return "incorrect";

        String role = account.getUserRole();
        if ("dean".equalsIgnoreCase(role)) {
        return "Dean.html";
    }

    if ("secretary".equalsIgnoreCase(role)) {
        return "Secretary.html";
    }



        if ("student".equalsIgnoreCase(role))
            return "profilestudent.html?accountId=" + account.getAccountId();

        String[] splits = role == null ? new String[0] : role.split("/");
        if (splits.length > 0 && "program head".equalsIgnoreCase(splits[0])) {
            String course = splits.length > 1 ? splits[1].trim() : "";
            return "ProgramHeadDean.html?course=" + course.toUpperCase();
        }
        return "unauthorized";
    }

    @PostMapping("/login-step1")
    @Transactional(readOnly = true)
    public Object step1(@RequestBody Step1Request body) {

        System.out.println(body.email + " " + body.password);


        if (body == null || body.email == null || body.password == null) {

            System.out.println("Hello failed1");
            return Map.of("status", "incorrect");
        }

        // unwrap optional safely
        Accounts acc = accountsDAO.findByStudentEmail(body.email.trim()).orElse(null);

        if (acc == null) {
            
            System.out.println("Hello failed2");
            return Map.of("status", "incorrect");
        
        }

        System.out.println(" "+acc.getAccountPassword());

        if (!body.password.equals(acc.getAccountPassword())) {

            System.out.println("Hello failed3");
            return Map.of("status", "incorrect");
        
        }

        String role = acc.getUserRole();
        System.out.println(role);
        if (role == null || role.isBlank()) {

            System.out.println("Hello failed4");
            
            return Map.of("status", "incorrect");
        
        }

        String redirect = mapRoleToRedirect(role, acc);
        System.out.println(redirect);
        if (redirect == null) {

            System.out.println("Hello failed5");
            
            return Map.of("status", "incorrect");
        
        }

        String code = String.format("%06d", RND.nextInt(1_000_000));
        String tempId = UUID.randomUUID().toString();

        PendingLogin pl = new PendingLogin();
        pl.email = acc.getStudentEmail();
        pl.redirect = redirect;
        pl.role = redirect;
        pl.code = code;
        pl.expiresAt = Instant.now().plusSeconds(300);
        pl.attempts = 0;
        PENDING.put(tempId, pl);

        boolean sent = emailService.sendVerificationCode(
            acc.getStudentEmail(),
            code
    );
    if (!sent) {
        PENDING.remove(tempId);
        return Map.of("status", "email_failed");
    }

        return Map.of("status", "2fa", "tempId", tempId, "redirect", redirect, "role", redirect);
    }


    @PostMapping("/login-verify")
public Object verify(@RequestBody VerifyRequest body) {
    if (body == null || body.tempId == null || body.code == null) {
        return Map.of("status", "incorrect");
    }

    // Do NOT remove yet – we might allow multiple attempts
    PendingLogin pl = PENDING.get(body.tempId);
    if (pl == null) {
        return Map.of("status", "incorrect");
    }

    // Check expiry
    if (pl.expiresAt.isBefore(Instant.now())) {
        PENDING.remove(body.tempId);
        return Map.of("status", "expired");
    }

    String code = body.code.replaceAll("\\D", "");

    // Wrong code
    if (!code.equals(pl.code)) {
        pl.attempts++;
        if (pl.attempts >= 3) {
            // Too many attempts: remove pending login and tell frontend to lock
            PENDING.remove(body.tempId);
            return Map.of("status", "locked");
        }
        return Map.of(
            "status", "incorrect",
            "attempts", pl.attempts
        );
    }

    // Correct code – success: remove and proceed
    PENDING.remove(body.tempId);
    return Map.of("status", "ok", "redirect", pl.redirect, "role", pl.role);
}

    private String mapRoleToRedirect(String role, Accounts acc) {

        if ("Dean".equalsIgnoreCase(role)) return "Dean.html";
        if ("Secretary".equalsIgnoreCase(role)) return "Secretary.html";
        if (role.startsWith("program head/")) return "ProgramHeadDean.html?course=" + role.substring("program head/".length());
        if ("Student".equalsIgnoreCase(role)) return "profilestudent.html?accountId=" + acc.getAccountId();
        if (role.startsWith("profilestudent.html")) return role;
        if (role.startsWith("ProgramHeadDean.html")) return role;
        return null;
    }

    private static String clip(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }


    private static LocalTime parseFlexibleTime(String raw) {
        if (raw == null) return null;
        String t = raw.trim().toUpperCase();
        t = t.replaceAll("(?i)AM$", " AM").replaceAll("(?i)PM$", " PM");
        t = t.replaceAll("(?i)AM\\b", " AM").replaceAll("(?i)PM\\b", " PM");
        t = t.replaceAll("\\s+", " ").trim();
        if (t.matches("^\\d{1,2}\\s*(AM|PM)$")) t = t.replaceAll("^(\\d{1,2})", "$1:00");
        if (t.matches("^\\d{1,2}$")) t = t + ":00";
        DateTimeFormatter[] patterns = {
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("h:mm a"),
            DateTimeFormatter.ofPattern("h a")
        };
        for (DateTimeFormatter f : patterns) {
            try { return LocalTime.parse(t, f); } catch (DateTimeParseException ignore) {}
        }
        return null;
    }
    private static String format12(LocalTime lt) {
        if (lt == null) return null;
        return lt.format(DateTimeFormatter.ofPattern("h:mm a"));
    }
    private static String to12HourRangeFlexible(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) return timeRange;
        String[] parts = timeRange.split("\\s*-\\s*");
        if (parts.length != 2) return timeRange;
        LocalTime start = parseFlexibleTime(parts[0]);
        LocalTime end   = parseFlexibleTime(parts[1]);
        if (start != null && end != null) {
            return format12(start) + " - " + format12(end);
        }
        return timeRange;
    }

    @PostMapping("/register")
    @Transactional
    public RegistrationResponse register(@RequestBody RegistrationRequest body) {
        if (body == null) throw new IllegalArgumentException("Body is required");
        if (body.getAccountEmail() == null || body.getAccountEmail().isBlank())
            throw new IllegalArgumentException("accountEmail is required");
        if (body.getStudent() == null)
            throw new IllegalArgumentException("student is required");

        Accounts acc = new Accounts();
        acc.setStudentEmail(body.getAccountEmail());
        String pwd = (body.getAccountPassword() == null || body.getAccountPassword().isBlank())
                ? String.valueOf((int)(Math.random() * 900000) + 100000)
                : body.getAccountPassword();
        acc.setAccountPassword(pwd);
        acc.setUserRole("student");

        Accounts savedAcc = serviceAccounts.addaccounts(acc);
        int accountId = (int) savedAcc.getAccountId();

        Student s = body.getStudent();
        s.setAccountId(accountId);

        String pictureUrl = null;
        String base64FromClient = s.getProfilePictureBase64();
        if (base64FromClient != null && !base64FromClient.isBlank()) {
            try {
                pictureUrl = fileStorageService.saveBase64Image(base64FromClient, String.valueOf(accountId));
            } catch (Exception e) {
                System.err.println("Failed to save profile picture: " + e.getMessage());
            }
        }
        s.setProfilePicture(pictureUrl);
        s.setProfilePictureBase64(null);

        s.setFirstName(clip(s.getFirstName(), 15));
        s.setMiddleName(clip(s.getMiddleName(), 15));
        s.setLastName(clip(s.getLastName(), 15));
        s.setCourse(clip(s.getCourse(), 10));
        s.setYearLevel(clip(s.getYearLevel(), 10));
        s.setContactInfo(clip(s.getContactInfo(), 11));
        s.setEmail(clip(s.getEmail(), 80));
        s.setAddress(clip(s.getAddress(), 50));
        s.setParentGuardianName(clip(s.getParentGuardianName(), 50));
        s.setStudentNationality(clip(s.getStudentNationality(), 20));
        s.setStudentReligion(clip(s.getStudentReligion(), 20));
        s.setSeniorHighSchool(clip(s.getSeniorHighSchool(), 20));

        serviceStudent.addStudent(s);

        if (body.getAdditionalInfo() != null) {
            AdditionalInfo ai = body.getAdditionalInfo();
            ai.setAccountId(accountId);
            if (ai.getShiftHours() != null)
                ai.setShiftHours(to12HourRangeFlexible(ai.getShiftHours()));
            serviceAdditionalInfo.saveAdditionalInfo(ai);
        }

        return new RegistrationResponse("ok", savedAcc.getAccountId(), pwd);
    }

    @PostMapping("/addacc")
    public Accounts addAccount(@RequestBody Accounts accounts) {
        return serviceAccounts.addaccounts(accounts);
    }

    @PostMapping("/addstudent")
    public Student addStudent(@RequestBody @Valid Student student) {
        return serviceStudent.addStudent(student);
    }

    @GetMapping("/programhead/students")
    public List<Student> getStudentsForProgramHead(@RequestParam String course) {
        return serviceStudent.getStudentsByCourse(course);
    }

    @PostMapping("/addadditionalinfo")
    public AdditionalInfo addAdditionalInfo(@RequestBody @Valid AdditionalInfo additionalInfo) {
        return serviceAdditionalInfo.saveAdditionalInfo(additionalInfo);
    }

    @GetMapping("/getadditioninfo/{accountId}")
    public AdditionalInfo getAdditionalInfo(@PathVariable long accountId) {
        try { return serviceAdditionalInfo.getAdditionalInfo(accountId); }
        catch (Exception ignore) { return null; }
    }

    @GetMapping("/getstudent/{AccountId}")
    public Student getStudent(@PathVariable long AccountId) {
        return serviceStudent.getStudent(AccountId);
    }

    @GetMapping("/{id}")
    public Accounts getAccount(@PathVariable long id) {
        return serviceAccounts.getaccounts(id);
    }

    @GetMapping("/student-full/{accountId}")
    @Transactional(readOnly = true)
    public StudentFullResponse getStudentFull(@PathVariable long accountId) {
        Student s = serviceStudent.getStudent(accountId);
        AdditionalInfo ai = null;
        try { ai = serviceAdditionalInfo.getAdditionalInfo(accountId); } catch (Exception ignore) {}
        Accounts acc = serviceAccounts.getaccounts(accountId);
        String normShift = (ai != null) ? ai.getShiftHours() : null;
        return new StudentFullResponse(
            acc != null ? acc.getStudentEmail() : null,
            s, ai, normShift
        );
    }

    public static class StudentFullResponse {
        private String accountEmail;
        private Student student;
        private AdditionalInfo additionalInfo;
        private String shiftHours;
        public StudentFullResponse(String accountEmail, Student student,
                                   AdditionalInfo additionalInfo, String shiftHours) {
            this.accountEmail = accountEmail;
            this.student = student;
            this.additionalInfo = additionalInfo;
            this.shiftHours = shiftHours;
        }
        public String getAccountEmail() { return accountEmail; }
        public Student getStudent() { return student; }
        public AdditionalInfo getAdditionalInfo() { return additionalInfo; }
        public String getShiftHours() { return shiftHours; }
    }

    @GetMapping("/programhead/students/summary")
    @Transactional(readOnly = true)
    public List<StudentSummary> getStudentsSummaryForProgramHead(@RequestParam String course) {
        return serviceStudent.getStudentsSummaryByCourse(course);
    }

    public static class RegistrationRequest {
        private String accountEmail;
        private String accountPassword;
        private Student student;
        private AdditionalInfo additionalInfo;
        public String getAccountEmail() { return accountEmail; }
        public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }
        public String getAccountPassword() { return accountPassword; }
        public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }
        public Student getStudent() { return student; }
        public void setStudent(Student student) { this.student = student; }
        public AdditionalInfo getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(AdditionalInfo additionalInfo) { this.additionalInfo = additionalInfo; }
    }

    public static class RegistrationResponse {
        private String status;
        private long accountId;
        private String passwordIssued;
        public RegistrationResponse(String status, long accountId, String passwordIssued) {
            this.status = status; this.accountId = accountId; this.passwordIssued = passwordIssued;
        }
        public String getStatus() { return status; }
        public long getAccountId() { return accountId; }
        public String getPasswordIssued() { return passwordIssued; }
    }

    public static class AchievementRequest {
        public Integer accountId;
        public String achievement;
        public String description;
        public String date;
        public String photoBase64;
    }
    public static class AuthorshipRequest {
        public Integer accountId;
        public String type;
        public String description;
        public String date;
        public String photoBase64;
    }

    @PostMapping("/achievements")
    public Map<String,String> addAchievement(@RequestBody AchievementRequest req) throws Exception {
        if (req==null || req.accountId==null) return Map.of("status","error","message","Missing accountId");
        if (req.achievement==null || req.achievement.isBlank() || req.achievement.length()>20)
            return Map.of("status","error","message","Achievement required, <=20 chars");
        if (req.description==null || req.description.isBlank() || req.description.length()>100)
            return Map.of("status","error","message","Description required, <=100 chars");
        if (req.photoBase64==null || req.photoBase64.isBlank())
            return Map.of("status","error","message","Photographic proof is required");

        String path = fileStorageService.saveBase64ImageTo(
            "records/achievements",
            req.accountId + "-" + System.currentTimeMillis(),
            req.photoBase64
        );

        AcademicAchievement a = new AcademicAchievement();
        a.setAccountId(req.accountId);
        a.setAchievement(req.achievement);
        a.setDescription(req.description);
        a.setDate(req.date);
        a.setProofPath(path);
        a.setStatus(AcademicAchievement.Status.PENDING);
        serviceAcademicAchievement.save(a);
        return Map.of("status","ok");
    }

    @PostMapping("/authorships")
    public Map<String,String> addAuthorship(@RequestBody AuthorshipRequest req) throws Exception {
        if (req==null || req.accountId==null) return Map.of("status","error","message","Missing accountId");
        if (req.type==null || req.type.isBlank())
            return Map.of("status","error","message","Type required");
        if (req.description==null || req.description.isBlank() || req.description.length()>100)
            return Map.of("status","error","message","Description required, <=100 chars");
        if (req.photoBase64==null || req.photoBase64.isBlank())
            return Map.of("status","error","message","Photographic proof is required");

        Type t = switch (req.type.trim()) {
            case "Research" -> Type.Research;
            case "Capstone" -> Type.Capstone;
            case "OJT Portfolio" -> Type.OJT_Portfolio;
            case "Project" -> Type.Project;
            default -> null;
        };
        if (t==null) return Map.of("status","error","message","Invalid type");

        String path = fileStorageService.saveBase64ImageTo(
            "records/authorships",
            req.accountId + "-" + System.currentTimeMillis(),
            req.photoBase64
        );

        AcademicAuthorship a = new AcademicAuthorship();
        a.setAccountId(req.accountId);
        a.setType(t);
        a.setDescription(req.description);
        a.setDate(req.date);
        a.setProofPath(path);
        a.setStatus(AcademicAuthorship.Status.PENDING);
        serviceAcademicAuthorship.save(a);
        return Map.of("status","ok");
    }

    @GetMapping("/achievements/by-account/{accountId}")
    public List<AcademicAchievement> achByAcc(@PathVariable Integer accountId,
                                              @RequestParam(required=false) String status){
        if (status==null) return serviceAcademicAchievement.list(accountId);
        return serviceAcademicAchievement.listByStatus(accountId, AcademicAchievement.Status.valueOf(status));
    }

    @GetMapping("/authorships/by-account/{accountId}")
    public List<AcademicAuthorship> autByAcc(@PathVariable Integer accountId,
                                             @RequestParam(required=false) String status){
        if (status==null) return serviceAcademicAuthorship.list(accountId);
        return serviceAcademicAuthorship.listByStatus(accountId, AcademicAuthorship.Status.valueOf(status));
    }

    @PutMapping("/achievements/{id}/status")
    public ResponseEntity<AcademicAchievement> updateAchievementStatus(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestParam(defaultValue = "false") boolean notify,
        @RequestParam(required = false) String email) {

    AcademicAchievement.Status st = AcademicAchievement.Status.valueOf(status.toUpperCase());
    AcademicAchievement updated = notify
        ? serviceAcademicAchievement.updateStatusAndNotify(id, st, email)
        : serviceAcademicAchievement.updateStatus(id, st);
    return ResponseEntity.ok(updated);
}

    @PutMapping("/authorships/{id}/status")
    public ResponseEntity<AcademicAuthorship> updateAuthorshipStatus(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestParam(defaultValue = "false") boolean notify,
        @RequestParam(required = false) String email) {

    AcademicAuthorship.Status st = AcademicAuthorship.Status.valueOf(status.toUpperCase());
    AcademicAuthorship updated = notify
        ? serviceAcademicAuthorship.updateStatusAndNotify(id, st, email)
        : serviceAcademicAuthorship.updateStatus(id, st);
    return ResponseEntity.ok(updated);
}

    @DeleteMapping("/achievements/{id}")
    public ResponseEntity<Void> deleteAchievement(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String email) {
        serviceAcademicAchievement.deleteAndNotify(id, email, reason);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/authorships/{id}")
    public ResponseEntity<Void> deleteAuthorship(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String email) {
        serviceAcademicAuthorship.deleteAndNotify(id, email, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/records/pending-map")
    public java.util.Map<Integer,Boolean> pendingMap(@RequestBody java.util.List<Integer> accountIds){
        java.util.Map<Integer,Boolean> map = new java.util.HashMap<>();
        for (Integer id : accountIds) {
            boolean pending = serviceAcademicAchievement.hasPending(id) || serviceAcademicAuthorship.hasPending(id);
            map.put(id, pending);
        }
        return map;
    }

    @PutMapping("/student/{accountId}")
    @Transactional
    public Student updateStudentByAccount(@PathVariable long accountId, @RequestBody Student body) {
        
    Student existing = serviceStudent.getStudent(accountId);
    if (existing == null) throw new IllegalArgumentException("Student not found for account " + accountId);

    existing.setFirstName(clip(body.getFirstName(), 15));
    existing.setMiddleName(clip(body.getMiddleName(), 15));
    existing.setLastName(clip(body.getLastName(), 15));
    existing.setCourse(clip(body.getCourse(), 10));
    existing.setGender(body.getGender());
    existing.setAge(body.getAge());
    existing.setBirthdate(body.getBirthdate());
    existing.setYearLevel(clip(body.getYearLevel(), 10));
    existing.setContactInfo(clip(body.getContactInfo(), 11));

    
    String newEmail = clip(body.getEmail(), 80);
    existing.setEmail(newEmail);

    existing.setAddress(clip(body.getAddress(), 50));
    existing.setParentGuardianName(clip(body.getParentGuardianName(), 50));
    existing.setStudentNationality(clip(body.getStudentNationality(), 20));
    existing.setStudentReligion(clip(body.getStudentReligion(), 20));
    existing.setSeniorHighSchool(clip(body.getSeniorHighSchool(), 20));

    existing.setClassSchedule(body.getClassSchedule());             // NEW ensure copied
    existing.setRegularOrIrregular(body.getRegularOrIrregular());   // NEW ensure copied

        
        String b64 = body.getProfilePictureBase64();
        if (b64 != null && !b64.isBlank()) {
            try {
                String url = fileStorageService.saveBase64Image(b64, String.valueOf(accountId));
                existing.setProfilePicture(url);
            } catch (Exception e) {
                System.err.println("Failed to save profile picture: " + e.getMessage());
            }
        }

        
    if (newEmail != null && !newEmail.isBlank()) {
        Accounts acc = serviceAccounts.getaccounts(accountId);
        if (acc != null) {
            String trimmed = newEmail.trim();
            if (!trimmed.equals(acc.getStudentEmail())) {
                
                if (serviceAccounts.emailTakenByAnother(trimmed, acc.getAccountId())) {
                    throw new IllegalArgumentException("Email is already taken by another account");
                }
                acc.setStudentEmail(trimmed);
                serviceAccounts.saveAndFlush(acc);   
            }
        }
    }

        
        return serviceStudent.addStudent(existing);
    }
    private static String trimOrNull(String s) {
    return (s == null || s.isBlank()) ? null : s.trim();
    }

private static String normalizeStatus(String s) {
    if (s == null) return null;
    String t = s.trim();

    
    if (t.equalsIgnoreCase("Currently Enrolled"))       return "Currently Enrolled";
    if (t.equalsIgnoreCase("NOT Currently Enrolled"))   return "NOT Currently Enrolled";
    if (t.equalsIgnoreCase("Inactive"))                 return "Inactive";
    if (t.equalsIgnoreCase("Graduated"))                return "Graduated";

    
    String u = t.toUpperCase().replace('-', '_').replace(' ', '_');
    switch (u) {
        case "CURRENTLY_ENROLLED":      return "Currently Enrolled";
        case "NOT_CURRENTLY_ENROLLED":  return "NOT Currently Enrolled";
        case "INACTIVE":                return "Inactive";
        case "GRADUATED":               return "Graduated";
        default:                        return t; // or return null to ignore unknowns
    }
}


    
    @PutMapping("/additionalinfo/{accountId}")
    @Transactional
    public AdditionalInfo updateAdditionalByAccount(@PathVariable long accountId,
                                                    @RequestBody AdditionalInfo body) {
       
        AdditionalInfo existing;
        try {
            existing = serviceAdditionalInfo.getAdditionalInfo(accountId);
        } catch (Exception e) {
            existing = new AdditionalInfo();
            existing.setAccountId((int) accountId);
        }

        
        existing.setWorkingStudent(body.getWorkingStudent());

       
        String wsType = trimOrNull(body.getWorkingStudentType());
        existing.setWorkingStudentType(wsType);
        existing.setDepartment(trimOrNull(body.getDepartment()));
        existing.setSupervisorName(trimOrNull(body.getSupervisorName()));
        existing.setSupervisorContact(trimOrNull(body.getSupervisorContact()));

        
        existing.setPlaceOfWork(trimOrNull(body.getPlaceOfWork()));

        
        String sh = trimOrNull(body.getShiftHours());
        existing.setShiftHours(sh != null ? to12HourRangeFlexible(sh) : null);

        
        existing.setBreadwinner(body.getBreadwinner());
        existing.setSelfFinancing(body.getSelfFinancing());

        
        existing.setOrphan(body.getOrphan());
        existing.setNameOfParentFather(body.getNameOfParentFather());
        existing.setNameOfParentMother(body.getNameOfParentMother());
        existing.setDateLossMother(body.getDateLossMother());
        existing.setDateLossFather(body.getDateLossFather());
        existing.setCurrentGuardian(trimOrNull(body.getCurrentGuardian()));
        existing.setGuardianRelationship(trimOrNull(body.getGuardianRelationship()));
        existing.setGuardianOccupation(trimOrNull(body.getGuardianOccupation()));
        existing.setGuardianIncome(trimOrNull(body.getGuardianIncome()));

        String schType = trimOrNull(body.getScholarshipType());
        String govSch  = trimOrNull(body.getGovernmentScholarship());

        
        if (schType == null) {
            existing.setScholarshipType(null);
            existing.setGovernmentScholarship(null);
        } else {
            existing.setScholarshipType(schType);
            if ("Government Scholarship".equalsIgnoreCase(schType)) {
                existing.setGovernmentScholarship(govSch);
            } else {
                existing.setGovernmentScholarship(null);
            }
        }

        
        Boolean perm = body.getDavaoPermanentResidence();
        if (Boolean.TRUE.equals(perm)) {
            existing.setDavaoPermanentResidence(true);
            existing.setCurrentAddress(null);
            existing.setLivingCondition(null);
            existing.setRegion(null);  
        } else if (Boolean.FALSE.equals(perm)) {
            existing.setDavaoPermanentResidence(false);
            existing.setCurrentAddress(trimOrNull(body.getCurrentAddress()));
            existing.setLivingCondition(trimOrNull(body.getLivingCondition()));
            existing.setRegion(trimOrNull(body.getRegion()));  
        }
        
        if (Boolean.FALSE.equals(body.getWorkingStudent())) {
            existing.setWorkingStudentType(null);
            existing.setDepartment(null);
            existing.setPlaceOfWork(null);
            existing.setSupervisorName(null);
            existing.setSupervisorContact(null);
            existing.setShiftHours(null);
            existing.setBreadwinner(null);
            existing.setSelfFinancing(null);
        } else if (Boolean.TRUE.equals(body.getWorkingStudent())) {
            
            if ("Student SA".equalsIgnoreCase(wsType)) {
                existing.setPlaceOfWork(null);
                
            } else if ("Outside".equalsIgnoreCase(wsType)) {
                existing.setDepartment(null);
                
            } else {
                
                existing.setDepartment(null);
                existing.setPlaceOfWork(null);
            }
        }

        String rawStatus = trimOrNull(body.getStatus());
        if (rawStatus != null) {
            existing.setStatus(normalizeStatus(rawStatus));
        }

        if (Boolean.TRUE.equals(body.getEditUsed())) {
            existing.setEditUsed(true);
        }

        return serviceAdditionalInfo.saveAdditionalInfo(existing);
    }


    public static class UpdateCredentialsRequest {
        private Long accountId;
        private String currentEmail;
        private String newEmail;
        private String oldPassword;
        private String newPassword;
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
        public String getCurrentEmail() { return currentEmail; }
        public void setCurrentEmail(String currentEmail) { this.currentEmail = currentEmail; }
        public String getNewEmail() { return newEmail; }
        public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @PutMapping("/account/update-credentials")
    public java.util.Map<String,String> updateAccountCredentials(@RequestBody UpdateCredentialsRequest req) {
        if (req == null) return java.util.Map.of("status","error","message","Missing body");

        Accounts acc = null;
        if (req.getAccountId() != null && req.getAccountId() > 0) {
            acc = serviceAccounts.getaccounts(req.getAccountId());
        }
        if (acc == null && req.getCurrentEmail() != null && !req.getCurrentEmail().isBlank()) {
            acc = serviceAccounts.getByEmail(req.getCurrentEmail().trim());
        }
        if (acc == null) return java.util.Map.of("status","error","message","Account not found");

        if (req.getOldPassword() == null || !req.getOldPassword().equals(acc.getAccountPassword())) {
            return java.util.Map.of("status","invalid-old-password");
        }

        String newEm = null;
        if (req.getNewEmail() != null && !req.getNewEmail().isBlank()) {
            newEm = clip(req.getNewEmail().trim(), 80);
            if (serviceAccounts.emailTakenByAnother(newEm, acc.getAccountId())) {
                return java.util.Map.of("status","email-taken");
            }
            acc.setStudentEmail(newEm);
        }
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            acc.setAccountPassword(req.getNewPassword());
        }

        if (newEm != null) {
            try {
                Student s = serviceStudent.getStudent(acc.getAccountId());
                if (s != null) {
                    s.setEmail(newEm);
                    serviceStudent.addStudent(s);
                }
            } catch (Exception ignore) {}
        }

        serviceAccounts.saveAndFlush(acc);
        return java.util.Map.of("status","ok","email", acc.getStudentEmail());
    }

    



    public static class OpenRegistrationRequest {
        public String course;
        public int count;
    }
    public static class CodePair {
        public long accountId;
        public String code;
        public CodePair(long id, String code){ this.accountId=id; this.code=code; }
    }
    public static class OpenRegistrationResponse {
        public String status; 
        public String course;
        public java.util.List<CodePair> codes;
        public OpenRegistrationResponse(String status, String course, java.util.List<CodePair> codes){
            this.status=status; this.course=course; this.codes=codes;
        }
    }
    public static class CloseRegistrationRequest { public String course; public boolean force; }
    public static class FlagRequest { public boolean open; } 
    public static class RegisterWithCodeRequest {
        public String code;
        public String accountEmail;
        public String temporaryPassword; 
        public Student student;
        public AdditionalInfo additionalInfo;
    }

    private static String sixDigits(java.util.Set<String> avoid){
        java.util.Random r = new java.util.Random();
        String s;
        do { s = String.format("%06d", r.nextInt(1_000_000)); } while (avoid.contains(s));
        return s;
    }


    @GetMapping("/registration/open-state")
    public java.util.Map<String,Boolean> openState(){
        boolean any = serviceAccounts.countByUserRolePrefix("slot/") > 0;
        return java.util.Map.of("open", any);
    }


    @GetMapping("/registration/status")
    public java.util.Map<String,Object> regStatus(@RequestParam String course){
        String prefix = "slot/" + course.toUpperCase();
        int empty = serviceAccounts.findByUserRolePrefix(prefix).size();
        return java.util.Map.of("open", empty>0, "emptyCount", empty);
    }


    @PostMapping("/registration/open")
    @Transactional
    public OpenRegistrationResponse openRegistration(@RequestBody OpenRegistrationRequest req){
        if (req==null || req.course==null || req.course.isBlank() || req.count<=0)
            throw new IllegalArgumentException("course and positive count are required");
        String course = req.course.trim().toUpperCase();
        String prefix = "slot/" + course;

        
        java.util.Set<String> activeCodes = new java.util.HashSet<>(
            serviceAccounts.activeSlotCodes(prefix)
        );

        java.util.List<CodePair> out = new java.util.ArrayList<>();
        for(int i=0;i<req.count;i++){
            String code = sixDigits(activeCodes);
            activeCodes.add(code);

            Accounts a = new Accounts();
            
            a.setStudentEmail("slot+"+course+"+"+System.currentTimeMillis()+"+"+i+"@acd.local");
            a.setAccountPassword(code);
            a.setUserRole(prefix); 
            Accounts saved = serviceAccounts.addaccounts(a);
            out.add(new CodePair(saved.getAccountId(), code));
        }
        return new OpenRegistrationResponse("open", course, out);
    }


    @PostMapping("/registration/close")
    @Transactional
    public java.util.Map<String,Object> closeRegistration(@RequestBody CloseRegistrationRequest req){
        if (req==null || req.course==null || req.course.isBlank())
            throw new IllegalArgumentException("course is required");
        String prefix = "slot/" + req.course.trim().toUpperCase();
        var slots = serviceAccounts.findByUserRolePrefix(prefix);
        int empty = slots.size();
        if (empty>0 && !req.force) return java.util.Map.of("status","has-empty","emptyCount", empty);
        if (empty>0) serviceAccounts.deleteAll(slots);
        return java.util.Map.of("status","closed","deleted", empty);
    }


    @PostMapping("/registration/flag")
    public java.util.Map<String,String> regFlag(@RequestBody FlagRequest r){ return java.util.Map.of("status","ok"); }


    @PostMapping("/registration/register-with-code")
    @Transactional
    public RegistrationResponse registerWithCode(@RequestBody RegisterWithCodeRequest req) {
        if (req == null || req.code == null || req.code.isBlank())
            throw new IllegalArgumentException("Code is required");

        Accounts slot = serviceAccounts.findSlotByCode(req.code);
        if (slot == null)
            throw new IllegalArgumentException("Invalid or already used code");

    
        String role = slot.getUserRole();
        String course = (role != null && role.startsWith("slot/")) ? role.substring(5) : null;

        
        String temp = (req.temporaryPassword != null && !req.temporaryPassword.isBlank())
                ? req.temporaryPassword
                : String.format("%06d", (int) (Math.random() * 1_000_000));
        slot.setStudentEmail(req.accountEmail);
        slot.setAccountPassword(temp);  
        slot.setUserRole("student");
        serviceAccounts.saveAndFlush(slot);

    
        if (req.student != null) {
            Student s = req.student;
            s.setAccountId((int) slot.getAccountId());
            if (course != null && (s.getCourse() == null || s.getCourse().isBlank())) {
                s.setCourse(course);
            }

            
            String pictureUrl = null;
            String base64FromClient = s.getProfilePictureBase64();
            if (base64FromClient != null && !base64FromClient.isBlank()) {
                try {
                    pictureUrl = fileStorageService.saveBase64Image(
                            base64FromClient, String.valueOf(slot.getAccountId()));
                } catch (Exception e) {
                    System.err.println("Failed to save profile picture: " + e.getMessage());
                }
            }

            s.setProfilePicture(pictureUrl);
            s.setProfilePictureBase64(null);

            s.setFirstName(clip(s.getFirstName(), 15));
            s.setMiddleName(clip(s.getMiddleName(), 15));
            s.setLastName(clip(s.getLastName(), 15));
            s.setCourse(clip(s.getCourse(), 10));
            s.setYearLevel(clip(s.getYearLevel(), 10));
            s.setContactInfo(clip(s.getContactInfo(), 11));
            s.setEmail(clip(s.getEmail(), 80));
            s.setAddress(clip(s.getAddress(), 50));
            s.setParentGuardianName(clip(s.getParentGuardianName(), 50));

            s.setStudentNationality(clip(s.getStudentNationality(), 20));
            s.setStudentReligion(clip(s.getStudentReligion(), 20));
            s.setSeniorHighSchool(clip(s.getSeniorHighSchool(), 20));

            s.setRegularOrIrregular(clip(s.getRegularOrIrregular(), 20));
            s.setClassSchedule(clip(s.getClassSchedule(), 20));

            serviceStudent.addStudent(s);
            
        }

        
        if (req.additionalInfo != null) {

            AdditionalInfo ai = req.additionalInfo;
            ai.setAccountId((int) slot.getAccountId());
            if (ai.getShiftHours() != null)

                ai.setShiftHours(to12HourRangeFlexible(ai.getShiftHours()));

            if (ai.getStatus() == null || ai.getStatus().isBlank()) {
            ai.setStatus("Currently Enrolled");
        }

            serviceAdditionalInfo.saveAdditionalInfo(ai);

        }

        

        return new RegistrationResponse("ok", slot.getAccountId(), temp);
    }


    private static final java.util.concurrent.ConcurrentHashMap<String,Boolean> EDIT_OPEN
            = new java.util.concurrent.ConcurrentHashMap<>();

    public static class EditFlag {
        public String course;
        public boolean open;
    }


    @GetMapping("/edit/state")
    public java.util.Map<String, Object> editState(@RequestParam String course) {
        boolean open = Boolean.TRUE.equals(EDIT_OPEN.get(course == null ? "" : course.trim().toUpperCase()));
        return java.util.Map.of("open", open);
    }


    @PostMapping("/edit/flag")
    @Transactional
    public Map<String, Object> editFlag(@RequestBody EditFlag req) {
        if (req == null || req.course == null || req.course.isBlank()) {
            throw new IllegalArgumentException("course required");
        }

        final String course = req.course.trim().toUpperCase();
        int resetCount = 0;

        
        if (!req.open) {
            List<Integer> accIds = serviceAdditionalInfo.currentlyEnrolledAccountIdsByCourse(course);
            if (!accIds.isEmpty()) {
                resetCount = serviceAdditionalInfo.resetEditUsedForAccounts(accIds);
            }
        }

        
        EDIT_OPEN.put(course, req.open);

        return Map.of("status", "ok", "open", req.open, "resetCount", resetCount);
    }

    @PostMapping("/status/bulk-update")
    public Map<String, Object> bulkUpdateEnrollmentStatus(@RequestBody List<Integer> keepEnrolledAccountIds) {
        Map<String, Object> out = new HashMap<>();
        int updated = serviceAdditionalInfo.applyBulkEnrollmentStatus(keepEnrolledAccountIds);
        out.put("updated", updated);
        out.put("message", "Bulk status update complete: " + updated + " record(s) affected.");
        return out;
    }

    @PostMapping("/status/graduate")
        public ResponseEntity<Map<String,Object>> markGraduated(@RequestBody List<Integer> accountIds) {
            int updated = serviceAdditionalInfo.graduateEligible(accountIds);
            Map<String,Object> body = new HashMap<>();
            body.put("updated", updated);
            body.put("message", updated + " student(s) set to Graduated.");
            return ResponseEntity.ok(body);
        }

    @GetMapping("/dean/students/summary")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<com.studentprofiling.studentprofiling.dto.StudentSummary> getAllStudentsSummary() {
        return serviceStudent.getAllStudentsSummary();
    }

    @PostMapping("/achievements/admin")
    public Map<String,String> addAchievementAdmin(@RequestBody AchievementRequest req) throws Exception {
        if (req==null || req.accountId==null) return Map.of("status","error","message","Missing accountId");
        if (req.achievement==null || req.achievement.isBlank() || req.achievement.length()>20)
            return Map.of("status","error","message","Achievement required, <=20 chars");
        if (req.description==null || req.description.isBlank() || req.description.length()>100)
            return Map.of("status","error","message","Description required, <=100 chars");
        if (req.photoBase64==null || req.photoBase64.isBlank())
            return Map.of("status","error","message","Photographic proof is required");

        String path = fileStorageService.saveBase64ImageTo(
            "records/achievements",
            req.accountId + "-" + System.currentTimeMillis(),
            req.photoBase64
        );

        AcademicAchievement a = new AcademicAchievement();
        a.setAccountId(req.accountId);
        a.setAchievement(req.achievement);
        a.setDescription(req.description);
        a.setDate(req.date);
        a.setProofPath(path);
        a.setStatus(AcademicAchievement.Status.ACCEPTED); 
        serviceAcademicAchievement.save(a);
        return Map.of("status","ok");
    }


    @PostMapping("/authorships/admin")
    public Map<String,String> addAuthorshipAdmin(@RequestBody AuthorshipRequest req) throws Exception {
        if (req==null || req.accountId==null) return Map.of("status","error","message","Missing accountId");
        if (req.type==null || req.type.isBlank()) return Map.of("status","error","message","Type required");
        if (req.description==null || req.description.isBlank() || req.description.length()>100)
            return Map.of("status","error","message","Description required, <=100 chars");
        if (req.photoBase64==null || req.photoBase64.isBlank())
            return Map.of("status","error","message","Photographic proof is required");

        AcademicAuthorship.Type t = switch (req.type.trim()) {
            case "Research" -> AcademicAuthorship.Type.Research;
            case "Capstone" -> AcademicAuthorship.Type.Capstone;
            case "OJT Portfolio" -> AcademicAuthorship.Type.OJT_Portfolio;
            case "Project" -> AcademicAuthorship.Type.Project;
            default -> null;
        };
        if (t==null) return Map.of("status","error","message","Invalid type");

        String path = fileStorageService.saveBase64ImageTo(
            "records/authorships",
            req.accountId + "-" + System.currentTimeMillis(),
            req.photoBase64
        );

        AcademicAuthorship a = new AcademicAuthorship();
        a.setAccountId(req.accountId);
        a.setType(t);
        a.setDescription(req.description);
        a.setDate(req.date);
        a.setProofPath(path);
        a.setStatus(AcademicAuthorship.Status.ACCEPTED); 
        serviceAcademicAuthorship.save(a);
        return Map.of("status","ok");
    }

    @GetMapping("/registration/verify-code")
    public java.util.Map<String,Object> verifyCode(@RequestParam String code) {
        if (code == null || code.isBlank()) {
            return java.util.Map.of("status","invalid");
        }
        Accounts slot = serviceAccounts.findSlotByCode(code.trim());
        if (slot == null) {

            return java.util.Map.of("status","invalid");

        }
        String role = slot.getUserRole();
        String course = (role != null && role.startsWith("slot/")) ? role.substring(5) : null;
        return java.util.Map.of(
            "status", "ok",
            "accountId", slot.getAccountId(),
            "course", course
        );

    }

}

