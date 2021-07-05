package com.techelevator.dao;

import com.techelevator.tenmo.model.Account;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;


public class JdbcAccountDaoTests extends TenmoDaoTests{

    private static final Account ACCOUNT_1 = new Account(2001, 1001, BigDecimal.valueOf(970));
    private static final Account ACCOUNT_2 = new Account(2002, 1002, BigDecimal.valueOf(1040));
    private static final Account ACCOUNT_3 = new Account(2003, 1003, BigDecimal.valueOf(990));

    private Account testAccount;

    private JdbcTemplate sut;

    @Before
    public void setup(){
        sut = new JdbcTemplate(dataSource);
    }




    private void assertAccountsMatch(Account expected, Account actual){
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertTrue(expected.getBalance().compareTo(actual.getBalance()) == 0);
    }

}
