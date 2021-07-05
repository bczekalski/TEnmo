package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
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
    private static final Account ACCOUNT_4 = new Account(2004L, 1004L, BigDecimal.valueOf(100));

    private Account testAccount;

    private JdbcAccountDao sut;

    @Before
    public void setup(){
        sut = new JdbcAccountDao(dataSource);
    }

    @Test
    public void get_user_balance_should_return_correct_user_balance(){
        Assert.assertTrue(ACCOUNT_1.getBalance().compareTo(sut.getUserBalance(ACCOUNT_1.getAccountId())) == 0);
        Assert.assertTrue(ACCOUNT_2.getBalance().compareTo(sut.getUserBalance(ACCOUNT_2.getAccountId())) == 0);
        Assert.assertTrue(ACCOUNT_3.getBalance().compareTo(sut.getUserBalance(ACCOUNT_3.getAccountId())) == 0);
    }

    @Test
    public void get_user_balance_return_null_for_non_valid_account_id(){
        Assert.assertNull(sut.getUserBalance(ACCOUNT_4.getAccountId()));
        Assert.assertNull(sut.getUserBalance(-20L));
    }





    private void assertAccountsMatch(Account expected, Account actual){
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertTrue(expected.getBalance().compareTo(actual.getBalance()) == 0);
    }

}
