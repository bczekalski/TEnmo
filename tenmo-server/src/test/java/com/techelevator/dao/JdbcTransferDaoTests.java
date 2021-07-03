package com.techelevator.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDaoTests extends TenmoDaoTests{

    private static final Transfer TRANSFER_1 = new Transfer(3001,
            2, 2, 2001, 2002, BigDecimal.valueOf(50));
    private static final Transfer TRANSFER_2 = new Transfer(3002,
            2, 2, 2002, 2003, BigDecimal.valueOf(10));
    private static final Transfer TRANSFER_3 = new Transfer(3003,
            2, 2, 2003, 2001, BigDecimal.valueOf(20));

    private Transfer testTransfer;

    private JdbcTransferDao sut;
    private AccountDao accountDao;
    private UserDao userDao;
    private ObjectMapper mapper;

    @Before
    public void setup(){
        userDao = new JdbcUserDao(dataSource);
        accountDao = new JdbcAccountDao(dataSource);
        sut = new JdbcTransferDao(dataSource, userDao, accountDao);
        this.mapper = new ObjectMapper();
    }

    @Test
    public void get_user_history_should_return_both_transfers_with_account_2001_stored() throws JsonProcessingException {
        List<String> json = sut.getUserHistory(userDao.getUserByAccountId(2001).getId().intValue());
        List<Transfer> actual = new ArrayList<>();
        for (String s : json){
            Transfer temp = mapper.readValue(s, Transfer.class);
            actual.add(temp);
        }
        Assert.assertEquals(2, actual.size());
        Transfer t1 = actual.get(0);
        Transfer t2 = actual.get(1);

        shortAssertTransferMatch(TRANSFER_1, t1);
        shortAssertTransferMatch(TRANSFER_3, t2);
    }







    private void assertTransfersMatch(Transfer expected, Transfer actual){
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getTransferTypeId(), actual.getTransferTypeId());
        Assert.assertEquals(expected.getTransferStatusDesc(), actual.getTransferStatusDesc());
        Assert.assertEquals(expected.getSenderId(), actual.getSenderId());
        Assert.assertEquals(expected.getReceiverId(), actual.getReceiverId());
        Assert.assertEquals(expected.getAmount(), actual.getAmount());
    }

    private void shortAssertTransferMatch(Transfer expected, Transfer actual){
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getSenderId(), actual.getSenderId());
        Assert.assertEquals(expected.getReceiverId(), actual.getReceiverId());
        Assert.assertEquals(expected.getAmount(), actual.getAmount());
    }
}
