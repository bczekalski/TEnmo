package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    private UserDao userDao;

    public UserController(UserDao userDao){
        this.userDao = userDao;
    }

    @RequestMapping(path = "/account/username/{id}", method = RequestMethod.GET)
    public String getUsernameByAccId(@PathVariable long id){
        return userDao.getUsernameByAccId(id);
    }

    @RequestMapping(path = "account/user/{id}", method = RequestMethod.GET)
    public long getUserIdByAccId(@PathVariable long id){
        return userDao.getUserIdByAccountId(id);
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> listAll(){
        return userDao.listUsers();
    }

    @RequestMapping(path = "/valid/{id}", method = RequestMethod.GET)
    public boolean isValidUser(@PathVariable long id){
        return userDao.isValidUser(id);
    }
}
