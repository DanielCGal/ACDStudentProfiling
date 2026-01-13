package com.studentprofiling.studentprofiling.ServiceClasses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studentprofiling.studentprofiling.DatabaseSchema.DAO.AccountsDAO;
import com.studentprofiling.studentprofiling.DatabaseSchema.Entity.Accounts;

@Service
public class ServiceAccounts {

    @Autowired
    private AccountsDAO accountsDAO;

    public Accounts addaccounts(Accounts accounts) {
        return accountsDAO.save(accounts);
    }

    public Accounts getaccounts(long id) {
        return accountsDAO.findById(id).orElse(null);
    }

    public Accounts login(String email, String password) {
        return accountsDAO.login(email, password);
    }

    public Accounts getByEmail(String email) {
    return accountsDAO.findByStudentEmail(email).orElse(null);
}

public Accounts saveAndFlush(Accounts accounts) {
    return accountsDAO.saveAndFlush(accounts);
}

public boolean emailTakenByAnother(String email, long excludeAccountId) {
    var existing = accountsDAO.findByStudentEmail(email);
    return existing.isPresent() && existing.get().getAccountId() != excludeAccountId;
}

public java.util.List<Accounts> findByUserRolePrefix(String prefix){
    return accountsDAO.findByUserRolePrefix(prefix);
}
public long countByUserRolePrefix(String prefix){
    return accountsDAO.countByUserRolePrefix(prefix);
}
public java.util.List<String> activeSlotCodes(String prefix){
    return accountsDAO.activeSlotCodes(prefix);
}
public Accounts findSlotByCode(String code){
    return accountsDAO.findSlotByCode(code);
}
public void deleteAll(java.util.List<Accounts> list){
    accountsDAO.deleteAll(list);
}



}
