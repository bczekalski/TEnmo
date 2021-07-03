package com.techelevator.tenmo.dao;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private AccountDao accountDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, UserDao userDao, AccountDao accountDao){
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.accountDao = accountDao;
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
                    userDao.getUserByAccountId(rowSet.getInt("account_to")).getUsername() + "| $" + rowSet.getBigDecimal("amount"));
        }

        sql = "SELECT t.transfer_id, t.account_from, t.amount " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_to = a.account_id " +
                "WHERE a.user_id = ?;";
        rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while(rowSet.next()){
            history.add(rowSet.getInt("transfer_id") + "|From: |" +
                    userDao.getUserByAccountId(rowSet.getInt("account_from")).getUsername() + "| $" + rowSet.getBigDecimal("amount"));
        }
        return history;
    }
    @Override
    public Transfer getTransfer(int userId, int transferId){
        String sql = "SELECT transfer_id, t.account_from, t.account_to, " +
                "ts.transfer_status_desc, tt.transfer_type_desc, t.amount " +
                "FROM transfers t " +
                "JOIN transfer_statuses ts ON ts.transfer_status_id = t.transfer_status_id " +
                "JOIN transfer_types tt ON tt.transfer_type_id = t.transfer_type_id " +
                "JOIN accounts a ON a.account_id = t.account_to OR a.account_id = t.account_from " +
                "JOIN users u ON u.user_id = a.user_id " +
                "WHERE transfer_id = ? AND u.user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferId, userId);
        if (rowSet.next()){
            return mapRowToTransfer(rowSet);
        }
        return null;
    }

    @Override
    public boolean sendMoney(Transfer ct){
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?;";
        try {
            jdbcTemplate.update(sql, accountDao.getUserBalance(ct.getSenderId()).subtract(ct.getAmount())
                    , ct.getSenderId());
            jdbcTemplate.update(sql, accountDao.getUserBalance(ct.getReceiverId()).add(ct.getAmount())
                    , ct.getReceiverId());
        }catch(DataAccessException e){
            return false;
        }
        return true;
    }

    @Override
    public Transfer addTransfer(Transfer t){
        int senderAccountId = accountDao.getAccountIdByUserId(t.getSenderId());
        int receiverAccountId = accountDao.getAccountIdByUserId(t.getReceiverId());

        String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        int newId = jdbcTemplate.queryForObject(sql, Integer.class, t.getTransferTypeId(), t.getTransferStatusId(),
                senderAccountId, receiverAccountId, t.getAmount());
        return getTransfer(t.getSenderId(), newId);
    }

    private Transfer mapRowToTransfer(SqlRowSet r){
        Transfer t = new Transfer();
        t.setTransferId(r.getInt("transfer_id"));
        t.setSenderId((userDao.getUserByAccountId(r.getInt("account_from")).getId()).intValue());
        t.setReceiverId(userDao.getUserByAccountId(r.getInt("account_to")).getId().intValue());
        t.setTransferTypeDesc(r.getString("transfer_type_desc"));
        t.setTransferStatusDesc(r.getString("transfer_status_desc"));
        t.setAmount(r.getBigDecimal("amount"));
        return t;
    }
}
