package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JdbcUserDaoTests extends TenmoDaoTests{

    private static final User USER_1 = new User(1001L, "test 1", "password 1");
    private static final User USER_2 = new User(1002L, "test 2", "password 2");
    private static final User USER_3 = new User(1003L, "test 3", "password 3");
    private static final User USER_4 = new User(1004L, "test 4", "password 4");

    private JdbcUserDao sut;

    @Before
    public void setup(){
        sut = new JdbcUserDao(dataSource);
    }

    @Test
    public void find_id_by_username_should_return_correct_id_if_user_in_database(){
        Assert.assertEquals(USER_1.getId().longValue(), sut.findIdByUsername(USER_1.getUsername()));
        Assert.assertEquals(USER_2.getId().longValue(), sut.findIdByUsername(USER_2.getUsername()));
        Assert.assertEquals(USER_3.getId().longValue(), sut.findIdByUsername(USER_3.getUsername()));
    }

    @Test
    public void get_username_by_id_should_return_correct_username(){
        Assert.assertEquals(USER_1.getUsername(), sut.getUsernameById(USER_1.getId()));
        Assert.assertEquals(USER_2.getUsername(), sut.getUsernameById(USER_2.getId()));
        Assert.assertEquals(USER_3.getUsername(), sut.getUsernameById(USER_3.getId()));
    }

    @Test
    public void is_valid_user_should_return_true_for_users_in_database(){
        Assert.assertTrue(sut.isValidUser(USER_1.getId()));
        Assert.assertTrue(sut.isValidUser(USER_2.getId()));
        Assert.assertTrue(sut.isValidUser(USER_3.getId()));
        Assert.assertFalse(sut.isValidUser(USER_4.getId()));
    }

    @Test
    public void find_by_username_should_return_valid_user_for_username(){
        assertUsersMatch(USER_1, sut.findByUsername(USER_1.getUsername()));
        assertUsersMatch(USER_2, sut.findByUsername(USER_2.getUsername()));
        assertUsersMatch(USER_3, sut.findByUsername(USER_3.getUsername()));
    }

    @Test
    public void get_user_by_account_id_should_return_correct_user_for_account_id(){
        assertUsersMatch(USER_1, sut.getUserByAccountId(2001L));
        assertUsersMatch(USER_2, sut.getUserByAccountId(2002L));
        assertUsersMatch(USER_3, sut.getUserByAccountId(2003L));
        Assert.assertNull(sut.getUserByAccountId(2004L));
    }

    @Test
    public void get_user_id_by_account_id_should_return_correct_id_for_passed_account_id(){
        Assert.assertEquals(USER_1.getId().longValue(), sut.getUserIdByAccountId(2001L));
        Assert.assertEquals(USER_2.getId().longValue(), sut.getUserIdByAccountId(2002L));
        Assert.assertEquals(USER_3.getId().longValue(), sut.getUserIdByAccountId(2003L));
        Assert.assertEquals(0L, sut.getUserIdByAccountId(2004L));
    }

    @Test
    public void get_username_by_account_id_should_return_correct_username_for_passed_account_id(){
        Assert.assertEquals(USER_1.getUsername(), sut.getUsernameByAccId(2001L));
        Assert.assertEquals(USER_2.getUsername(), sut.getUsernameByAccId(2002L));
        Assert.assertEquals(USER_3.getUsername(), sut.getUsernameByAccId(2003L));
    }

    private void assertUsersMatch(User expected, User actual){
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getPassword(), actual.getPassword());
    }


}
