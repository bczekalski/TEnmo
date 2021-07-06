package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class TransferService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticationService authenticationService;

    public TransferService(String url) {
        this.baseUrl = url;
        this.authenticationService = new AuthenticationService(url);
    }

    public List transferHistory(String jwt, long id){
        return restTemplate.exchange(baseUrl + "history/" + id, HttpMethod.GET,
                authenticationService.createAuthEntity(jwt), List.class).getBody();
    }

    public boolean sendMoney(Transfer currentTransfer, String jwt){
        return restTemplate.exchange(baseUrl + "send", HttpMethod.POST,
                authenticationService.createAuthTransferEntity(currentTransfer, jwt), Boolean.class).getBody();
    }

    public Long addTransfer(Transfer transfer, String jwt){
        return restTemplate.exchange(baseUrl + "transaction", HttpMethod.POST,
                authenticationService.createAuthTransferEntity(transfer, jwt), Long.class).getBody();
    }

    public Transfer getTransfer(String jwt, long userId, long id){
        return restTemplate.exchange(baseUrl + "transfer/" + userId + "/" + id, HttpMethod.GET,
                authenticationService.createAuthEntity(jwt), Transfer.class).getBody();
    }
}
