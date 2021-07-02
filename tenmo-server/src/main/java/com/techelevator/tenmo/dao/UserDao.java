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

    BigDecimal getUserBalance(int id);

    List<String> getUserHistory(int id);

    String getTransfer(int userId, int id);

    boolean isValidUser(int id);

    boolean sendMoney(Transfer ct);

    Integer addTransfer(Transfer t);
}
