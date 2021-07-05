package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.model.Account;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;


public class JdbcAccountDaoTests extends TenmoDaoTests{

    private static final Account ACCOUNT_1 = new Account(2001L, 1001L, BigDecimal.valueOf(970));
    private static final Account ACCOUNT_2 = new Account(2002L, 1002L, BigDecimal.valueOf(1040));
    private static final Account ACCOUNT_3 = new Account(2003L, 1003L, BigDecimal.valueOf(990));
    private static final Account ACCOUNT_4 = new Account(2004L, 1004L, BigDecimal.valueOf(100));

    private Account testAccount;

    private JdbcAccountDao sut;

    @Before
    public void setup(){
        sut = new JdbcAccountDao(dataSource);
    }

    @Test
    public void get_account_id_by_user_id_should_return_account_id_for_corresponding_user(){
        Assert.assertEquals(ACCOUNT_1.getAccountId(), sut.getAccountIdByUserId(ACCOUNT_1.getUserId()));
        Assert.assertEquals(ACCOUNT_2.getAccountId(), sut.getAccountIdByUserId(ACCOUNT_2.getUserId()));
        Assert.assertEquals(ACCOUNT_3.getAccountId(), sut.getAccountIdByUserId(ACCOUNT_3.getUserId()));

    }

    @Test
    public void get_account_id_should_return_0_for_non_valid_user_id(){
        Assert.assertEquals(0L, sut.getAccountIdByUserId(ACCOUNT_4.getUserId()));
        Assert.assertEquals(0L, sut.getAccountIdByUserId(-20L));
    }







    private void assertAccountsMatch(Account expected, Account actual){
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertTrue(expected.getBalance().compareTo(actual.getBalance()) == 0);
    }

}
