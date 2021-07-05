package com.techelevator.dao;


import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUserDaoTests extends TenmoDaoTests{

    private static final User USER_1 = new User(1001L, "test 1", "password 1");
    private static final User USER_2 = new User(1002L, "test 2", "password 2");
    private static final User USER_3 = new User(1003L, "test 3", "password 3");

    private User testUser;
    private JdbcTemplate sut;

    @Before
    public void setup(){
        sut = new JdbcTemplate(dataSource);
    }


    
    private void assertUsersMatch(User expected, User actual){
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getPassword(), actual.getPassword());
    }


}
