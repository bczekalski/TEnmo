package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserDao {

    List<User> findAll();

    Map<String, String> listUsers();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    boolean isValidUser(int id);

    User getUserByAccountId(int accId);

    String getUsernameById(int id);
}
