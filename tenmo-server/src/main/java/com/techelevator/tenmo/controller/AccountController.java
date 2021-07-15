package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.model.Account;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class AccountController {

    private AccountDao accountDao;

    public AccountController(AccountDao accountDao){
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/balance/{id}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public BigDecimal getBalance(@PathVariable long id){
        return accountDao.getUserBalance(id);
    }

    @RequestMapping(path = "user/account/{id}", method = RequestMethod.GET)
    public long getAccIdByUserId(@PathVariable long id){
        return accountDao.getAccountIdByUserId(id);
    }
}
