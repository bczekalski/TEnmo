package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.UserNotActivatedException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username ILIKE ?;";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
    }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM users;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }
        return users;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM users WHERE username ILIKE ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
            }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        try {
            newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
        } catch (DataAccessException e) {
            return false;
                }

        // create account
        sql = "INSERT INTO accounts (user_id, balance) values(?, ?)";
        try {
            jdbcTemplate.update(sql, newUserId, STARTING_BALANCE);
        } catch (DataAccessException e) {
            return false;
        }

        return true;
    }

    @Override
    public BigDecimal getUserBalance(int id) {
        String sql = "SELECT balance FROM accounts WHERE user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (rowSet.next()){
            return rowSet.getBigDecimal("balance");
        }
        throw new UserNotActivatedException("User " + id + " is not activated");
    }

    @Override
    public List<String> getUserHistory(int id){
        List<String> history = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.account_to, t.amount " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_from = a.account_id " +
                "WHERE a.user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while(rowSet.next()){
            history.add(rowSet.getInt("transfer_id") + "|To: |" +
                    getUserByAccountID(rowSet.getInt("account_id")).getUsername() + "| $" + rowSet.getBigDecimal("amount"));
        }

        sql = "SELECT t.transfer_id, t.account_from, t.amount " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_to = a.account_id " +
                "WHERE a.user_id = ?;";
        rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while(rowSet.next()){
            history.add(rowSet.getInt("transfer_id") + "|From: |" +
                    getUserByAccountID(rowSet.getInt("account_id")).getUsername() + "| $" + rowSet.getBigDecimal("amount"));
        }
        return history;
    }

    private User getUserByAccountID(int accId){
        String sql = "SELECT user_id, user_name, password_hash " +
                "FROM users u " +
                "JOIN accounts a ON a.user_id = u.user_id " +
                "WHERE a.account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accId);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
        }
        return null;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }
}