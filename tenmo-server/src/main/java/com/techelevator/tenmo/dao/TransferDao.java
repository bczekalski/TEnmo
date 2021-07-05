package com.techelevator.tenmo.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.List;

public interface TransferDao {

    List<String> getUserHistory(long id) throws JsonProcessingException;

    Transfer getTransfer(long userId, long transferId);

    boolean sendMoney(Transfer ct);

    long addTransfer(Transfer t);
}
