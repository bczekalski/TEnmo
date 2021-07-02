package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.UserNotActivatedException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

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
    public boolean isValidUser(int id){
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
    public BigDecimal getUserBalance(int id) {
        String sql = "SELECT balance FROM accounts WHERE user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        rowSet.next();
        return rowSet.getBigDecimal("balance");

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
                    getUserByAccountID(rowSet.getInt("account_to")).getUsername() + "| $" + rowSet.getBigDecimal("amount"));
        }

        sql = "SELECT t.transfer_id, t.account_from, t.amount " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_to = a.account_id " +
                "WHERE a.user_id = ?;";
        rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while(rowSet.next()){
            history.add(rowSet.getInt("transfer_id") + "|From: |" +
                    getUserByAccountID(rowSet.getInt("account_from")).getUsername() + "| $" + rowSet.getBigDecimal("amount"));
        }
        return history;
    }
    @Override
    public String getTransfer(int userId, int id){
        String sql = "SELECT transfer_id, t.account_from, t.account_to, " +
                "ts.transfer_status_desc, tt.transfer_type_desc, t.amount " +
                "FROM transfers t " +
                "JOIN transfer_statuses ts ON ts.transfer_status_id = t.transfer_status_id " +
                "JOIN transfer_types tt ON tt.transfer_type_id = t.transfer_type_id " +
                "JOIN accounts a ON a.account_id = t.account_to OR a.account_id = t.account_from " +
                "JOIN users u ON u.user_id = a.user_id " +
                "WHERE transfer_id = ? AND u.user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id, userId);
        if (rowSet.next()){
            return mapTransferToString(rowSet);
        }
        return null;
    }

    @Override
    public boolean sendMoney(Transfer ct){
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?;";
        try {
            jdbcTemplate.update(sql, getUserBalance(ct.getSenderId()).subtract(ct.getAmount())
                  , ct.getSenderId());
            jdbcTemplate.update(sql, getUserBalance(ct.getReceiverId()).add(ct.getAmount())
                    , ct.getReceiverId());
        }catch(DataAccessException e){
            return false;
        }
        return true;
    }

    @Override
    public Integer addTransfer(Transfer t){
        int senderAccountId = getAccountIdByUserId(t.getSenderId());
        int receiverAccountId = getAccountIdByUserId(t.getReceiverId());

        String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
        "VALUES (?, ?, ?, ?, ?);";
        return jdbcTemplate.update(sql, Integer.class, t.getTransferTypeId(), t.getTransferStatusId(),
                senderAccountId, receiverAccountId, t.getAmount());
    }

    private int getAccountIdByUserId(int id){
        String sql = "SELECT account_id FROM accounts WHERE user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        rowSet.next();
        return rowSet.getInt("account_id");
    }

    private User getUserByAccountID(int accId){
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

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }

    private String mapTransferToString(SqlRowSet r){
        return r.getInt("transfer_id") + "|" + getUserByAccountID(r.getInt("account_from")).getUsername() + "|" +
                getUserByAccountID(r.getInt("account_to")).getUsername() + "|" +
                r.getString("transfer_type_desc") + "|" +
                r.getString("transfer_status_desc") + "|" + r.getBigDecimal("amount");
    }
}
