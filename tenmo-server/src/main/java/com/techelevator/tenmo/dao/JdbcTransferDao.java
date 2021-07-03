package com.techelevator.tenmo.dao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private AccountDao accountDao;
    private ObjectMapper mapper;

    public JdbcTransferDao(DataSource dataSource, UserDao userDao, AccountDao accountDao){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<String> getUserHistory(long id) throws JsonProcessingException {
        List<Transfer> history = new ArrayList<>();
        List<String> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.account_to, t.amount " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_from = a.account_id " +
                "WHERE a.user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while(rowSet.next()) {
            Transfer transfer = new Transfer();
            transfer.setTransferId(rowSet.getInt("transfer_id"));
            transfer.setSenderId(id);
            transfer.setReceiverId(rowSet.getLong("account_to"));
            transfer.setAmount(rowSet.getBigDecimal("amount"));
            transfer.setTransferSent(true);
            history.add(transfer);
        }

        sql = "SELECT t.transfer_id, t.account_from, t.amount " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_to = a.account_id " +
                "WHERE a.user_id = ?;";
        rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while(rowSet.next()){
            Transfer transfer = new Transfer();
            transfer.setTransferId(rowSet.getInt("transfer_id"));
            transfer.setSenderId(rowSet.getLong("account_from"));
            transfer.setReceiverId(id);
            transfer.setAmount(rowSet.getBigDecimal("amount"));
            transfer.setTransferSent(false);
            history.add(transfer);
        }
        for (Transfer t : history){
            transfers.add(mapper.writeValueAsString(t));
        }
        return transfers;

    }
    @Override
    public Transfer getTransfer(long userId, long transferId) {
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
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?;";
        try {
            jdbcTemplate.update(sql,
                    accountDao.getUserBalance(ct.getSenderId()).subtract(ct.getAmount()),
                    ct.getSenderId());
            jdbcTemplate.update(sql,
                    accountDao.getUserBalance(ct.getReceiverId()).add(ct.getAmount()),
                    ct.getReceiverId());
        }catch(DataAccessException e){
            return false;
        }
        return true;
    }

    @Override
    public long addTransfer(Transfer t) {
        String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        long newId = jdbcTemplate.queryForObject(sql, Long.class, t.getTransferTypeId(), t.getTransferStatusId(),
                t.getSenderId(), t.getReceiverId(), t.getAmount());
        return newId;
    }

    private Transfer mapRowToTransfer(SqlRowSet r){
        Transfer t = new Transfer();
        t.setTransferId(r.getLong("transfer_id"));
        t.setSenderId(r.getLong("account_from"));
        t.setReceiverId(r.getLong("account_to"));
        t.setTransferTypeDesc(r.getString("transfer_type_desc"));
        t.setTransferStatusDesc(r.getString("transfer_status_desc"));
        t.setAmount(r.getBigDecimal("amount"));
        return t;
    }
}
