package com.techelevator.tenmo.controller;

import javax.validation.Valid;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.LoginDTO;
import com.techelevator.tenmo.model.RegisterUserDTO;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller to authenticate users.
 */
@RestController
public class AuthenticationController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private UserDao userDao;

    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse login(@Valid @RequestBody LoginDTO loginDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);
        
        User user = userDao.findByUsername(loginDto.getUsername());

        return new LoginResponse(jwt, user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@Valid @RequestBody RegisterUserDTO newUser) {
        if (!userDao.create(newUser.getUsername(), newUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User registration failed.");
        }
    }

    @RequestMapping(path = "/balance/{id}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public BigDecimal getBalance(@PathVariable int id){
        return userDao.getUserBalance(id);
    }

    @RequestMapping(path = "/history/{id}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public List<String> getHistory(@PathVariable int id){
        return userDao.getUserHistory(id);
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> listAll(){
        return userDao.listUsers();
    }

    @RequestMapping(path = "/transfer/{userID}/{id}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String getTransfer(@PathVariable int userID, @PathVariable int id){
        return userDao.getTransfer(userID, id);
    }

    @RequestMapping(path = "/valid/{id}", method = RequestMethod.GET)
    public boolean isValidUser(@PathVariable int id){
        return userDao.isValidUser(id);
    }

    @RequestMapping(path = "/send", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public boolean sendMoney(@Valid @RequestBody Transfer currentTransfer){
        return userDao.sendMoney(currentTransfer);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transaction", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public Integer addTransfer(@Valid @RequestBody Transfer transfer){
        return userDao.addTransfer(transfer);
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class LoginResponse {

        private String token;
        private User user;

        LoginResponse(String token, User user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        void setToken(String token) {
            this.token = token;
        }

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}
    }
}

