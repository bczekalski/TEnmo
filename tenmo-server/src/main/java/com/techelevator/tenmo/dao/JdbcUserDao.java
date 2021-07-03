package com.techelevator.tenmo.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.UserNotActivatedException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private JdbcTemplate jdbcTemplate;
    private ObjectMapper mapper;

    public JdbcUserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.mapper = new ObjectMapper();
    }

    @Override
    public long findIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username ILIKE ?;";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
        }
    }

    @Override
    public String getUsernameById(long id){
        String sql = "SELECT username FROM users WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    @Override
    public Map<String, String> listUsers(){
        Map<String, String> users = new HashMap<>();
        String sql = "SELECT user_id, username FROM users;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
        while(rowSet.next()){
            users.put(rowSet.getString("user_id"), rowSet.getString("username"));
        }
        return users;
    }

    @Override
    public boolean isValidUser(long id){
        String sql = "SELECT user_id FROM users WHERE user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        if(rowSet.next()){
            return true;
        }
        return false;
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
    public User getUserByAccountId(long accId){
        String sql = "SELECT u.user_id, username, password_hash " +
                "FROM users u " +
                "JOIN accounts a ON a.user_id = u.user_id " +
                "WHERE a.account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accId);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
        }
        return null;
    }

    @Override
    public long getUserIdByAccountId(long accId){
        String sql = "SELECT user_id FROM accounts WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accId);
        if (rowSet.next()){
            return rowSet.getLong("user_id");
        }
        return 0L;
    }

    @Override
    public String getUsernameByAccId(long accId){
        String sql = "SELECT u.username FROM users u " +
                "JOIN accounts a ON a.user_id = u.user_id " +
                "WHERE a.account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accId);
        if (rowSet.next()){
            return rowSet.getString("username");
        }
        throw new UsernameNotFoundException("User for account id" + accId + " does not exist");
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
