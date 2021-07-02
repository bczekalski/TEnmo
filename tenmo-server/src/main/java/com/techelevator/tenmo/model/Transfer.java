package com.techelevator.tenmo.model;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Transfer {

    @Positive
    private int senderId;
    @Positive
    private int receiverId;
    @Positive
    private BigDecimal amount;

    public Transfer(){}

    public Transfer(int senderId, int receiverId, BigDecimal amount){
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }


    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
