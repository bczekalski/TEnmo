package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getUserBalance(long id);

    long getAccountIdByUserId(long id);

}
