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

    private static final Transfer TRANSFER_1 = new Transfer(3001L,
            2, 2, 2001L, 2002L, BigDecimal.valueOf(50));
    private static final Transfer TRANSFER_2 = new Transfer(3002L,
            2, 2, 2002L, 2003L, BigDecimal.valueOf(10));
    private static final Transfer TRANSFER_3 = new Transfer(3003L,
            2, 2, 2003L, 2001L, BigDecimal.valueOf(20));
    private static final Transfer TRANSFER_4 = new Transfer(3004L,
            2, 2, 2002L, 2001L, BigDecimal.valueOf(20));

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
        TRANSFER_1.setTransferTypeDesc("Send");
        TRANSFER_1.setTransferStatusDesc("Approved");
        TRANSFER_2.setTransferTypeDesc("Send");
        TRANSFER_2.setTransferStatusDesc("Approved");
        TRANSFER_3.setTransferTypeDesc("Send");
        TRANSFER_3.setTransferStatusDesc("Approved");
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

    @Test
    public void get_transfer_should_return_full_details_of_a_transfer(){
        assertTransfersMatch(TRANSFER_1, sut.getTransfer(1001, 3001));
        assertTransfersMatch(TRANSFER_2, sut.getTransfer(1002, 3002));
        assertTransfersMatch(TRANSFER_3, sut.getTransfer(1003, 3003));
    }

    @Test
    public void send_money_returns_true_after_lowering_sender_balance_and_raising_receiver_balance(){
        Assert.assertTrue(sut.sendMoney(TRANSFER_1));
        Assert.assertTrue(accountDao.getUserBalance(2001L).compareTo(BigDecimal.valueOf(920)) == 0);
        Assert.assertTrue(accountDao.getUserBalance(2002L).compareTo(BigDecimal.valueOf(1090)) == 0);
    }

    @Test
    public void add_transfer_successfully_adds_a_transfer_to_the_database(){
        testTransfer = TRANSFER_4;
        long newId = sut.addTransfer(testTransfer);
        testTransfer.setTransferId(newId);
        testTransfer.setTransferStatusDesc("Approved");
        testTransfer.setTransferTypeDesc("Send");
        assertTransfersMatch(TRANSFER_4, sut.getTransfer(1001, newId));
    }

    private void assertTransfersMatch(Transfer expected, Transfer actual){
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getTransferTypeDesc(), actual.getTransferTypeDesc());
        Assert.assertEquals(expected.getTransferStatusDesc(), actual.getTransferStatusDesc());
        Assert.assertEquals(expected.getSenderId(), actual.getSenderId());
        Assert.assertEquals(expected.getReceiverId(), actual.getReceiverId());
        Assert.assertTrue(expected.getAmount().compareTo(actual.getAmount()) == 0);
    }

    private void shortAssertTransferMatch(Transfer expected, Transfer actual){
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getSenderId(), actual.getSenderId());
        Assert.assertEquals(expected.getReceiverId(), actual.getReceiverId());
        Assert.assertTrue(expected.getAmount().compareTo(actual.getAmount()) == 0);
    }
}
