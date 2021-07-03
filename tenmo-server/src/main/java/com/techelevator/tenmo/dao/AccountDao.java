package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getUserBalance(int id);

    int getAccountIdByUserId(int id);
}
