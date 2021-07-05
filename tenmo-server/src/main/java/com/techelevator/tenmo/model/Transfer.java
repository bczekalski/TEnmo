package com.techelevator.tenmo.model;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Transfer {

    private long transferId;
    @Positive
    private long senderId;
    @Positive
    private long receiverId;
    @Positive
    private BigDecimal amount;
    private int transferTypeId;
    private int transferStatusId;
    private String transferStatusDesc;
    private String transferTypeDesc;
    //boolean used for checking if transfer stored during getUserHistory is being sent or received
    private boolean transferSent;

    public Transfer(){}

    public Transfer(long senderId, long receiverId, BigDecimal amount){
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public Transfer(long transferId, int transferTypeId, int transferStatusId, long senderId, long receiverId, BigDecimal amount){
        this.transferId = transferId;
        this.transferTypeId = transferTypeId;
        this.transferStatusId = transferStatusId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTransferStatusId() {
        return transferStatusId;
    }

    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public int getTransferTypeId() {
        return transferTypeId;
    }

    public void setTransferTypeId(int transferTypeId) {
        this.transferTypeId = transferTypeId;
    }

    public long getTransferId() {
        return transferId;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }

    public String getTransferStatusDesc() {
        return transferStatusDesc;
    }

    public void setTransferStatusDesc(String transferStatusDesc) {
        this.transferStatusDesc = transferStatusDesc;
    }

    public String getTransferTypeDesc() {
        return transferTypeDesc;
    }

    public void setTransferTypeDesc(String transferTypeDesc) {
        this.transferTypeDesc = transferTypeDesc;
    }

    public void setTransferSent(boolean transferSent) {
        this.transferSent = transferSent;
    }
}
