package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.List;

public interface TransferDao {

    List<String> getUserHistory(int id);

    Transfer getTransfer(int userId, int transferId);

    boolean sendMoney(Transfer ct);

    Transfer addTransfer(Transfer t);
}
