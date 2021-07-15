package com.techelevator.tenmo.services;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticationService authenticationService;

    public AccountService(String url) {
        this.baseUrl = url;
        this.authenticationService = new AuthenticationService(url);
    }

    public long getAccIdByUserId(long id){
        return restTemplate.getForObject(baseUrl + "user/account/" + id, Long.class);
    }

    public BigDecimal balance(String jwt, long id){
        return restTemplate.exchange(baseUrl + "balance/" + id, HttpMethod.GET, authenticationService.createAuthEntity(jwt), BigDecimal.class).getBody();
    }
}
