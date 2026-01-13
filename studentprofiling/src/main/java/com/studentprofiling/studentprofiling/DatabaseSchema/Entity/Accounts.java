package com.studentprofiling.studentprofiling.DatabaseSchema.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Accounts {

    public Accounts() {}

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private long AccountId;

    @Column(name = "StudentEmail", nullable = false, unique = true)
    private String studentEmail;

    @Column(name = "AccountPassword", nullable = false, unique = false)
    private String accountPassword;

    @Column(name = "UserRole", nullable = false, unique = false)
    private String userRole;

    @JsonProperty("accountId")
    public long getAccountId() {
        return AccountId;
    }

    public void setAccountId(long accountId) {
        this.AccountId = accountId;
    }

    @JsonProperty("studentEmail")
    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    @JsonProperty("accountPassword")
    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    @JsonProperty("userRole")
    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

}
