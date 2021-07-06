package com.techelevator.tenmo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class TransferController {

    private TransferDao transferDao;

    public TransferController(TransferDao transferDao){
        this.transferDao = transferDao;
    }

    @RequestMapping(path = "/history/{id}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public List<String> getHistory(@PathVariable long id) throws JsonProcessingException {
        return transferDao.getUserHistory(id);
    }

    @RequestMapping(path = "/transfer/{userID}/{id}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public Transfer getTransfer(@PathVariable long userID, @PathVariable long id) {
        return transferDao.getTransfer(userID, id);
    }

    @RequestMapping(path = "/send", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public boolean sendMoney(@Valid @RequestBody Transfer currentTransfer){
        return transferDao.sendMoney(currentTransfer);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transaction", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public long addTransfer(@Valid @RequestBody Transfer transfer) {
        return transferDao.addTransfer(transfer);
    }
}
