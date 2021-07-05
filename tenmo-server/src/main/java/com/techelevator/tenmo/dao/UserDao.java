package com.techelevator.tenmo.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserDao {

    Map<String, String> listUsers();

    User findByUsername(String username);

    long findIdByUsername(String username);

    boolean create(String username, String password);

    boolean isValidUser(long id);

    User getUserByAccountId(long accId);

    String getUsernameById(long id);

    long getUserIdByAccountId(long accId);

    String getUsernameByAccId(long accId);
}
