package com.studentprofiling.studentprofiling.ServiceClasses;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.StudentDAO;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Student;
import com.studentprofiling.studentprofiling.dto.StudentSummary;


@Service
public class ServiceStudent {

    @Autowired
    private StudentDAO studentDAO;

    public Student addStudent(Student student) {
        return studentDAO.save(student);
    }

    
    @Transactional(readOnly = true)
    public Student getStudent(long accountId) {
        return studentDAO.findTopByAccountIdOrderByIdDesc(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with account ID: " + accountId));
    }

    
    @Transactional
    public Student updateStudent(long accountId, java.util.function.UnaryOperator<Student> mutator) {
        Student existing = getStudent(accountId); 
        Student mutated = mutator.apply(existing);
        return studentDAO.save(mutated);
    }

    
    @Transactional(readOnly = true)
    public List<Student> getStudentsByCourse(String course) {
        return studentDAO.findByCourseIgnoreCase(course);
    }

    
    @Transactional(readOnly = true)
    public List<com.studentprofiling.studentprofiling.dto.StudentSummary> getStudentsSummaryByCourse(String course) {
        return studentDAO.findSummaryByCourse(course);
        
    }

    public List<StudentSummary> getStudentSummariesByCourse(String course) {
    return studentDAO.findSummaryByCourse(course);

    }

     @Transactional(readOnly = true)
public List<StudentSummary> getAllStudentsSummary() {
    return studentDAO.findAllSummary();

}


}

