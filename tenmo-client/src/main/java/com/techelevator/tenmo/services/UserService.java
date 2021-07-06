package com.techelevator.tenmo.services;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class UserService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticationService authenticationService;

    public UserService(String url) {
        this.baseUrl = url;
        this.authenticationService = new AuthenticationService(url);
    }

    public Map listAll(String jwt){
        return restTemplate.exchange(baseUrl + "list", HttpMethod.GET,
                authenticationService.createAuthEntity(jwt), Map.class).getBody();
    }

    public String getUsernameByAccId(long id){
        return restTemplate.getForObject(baseUrl + "account/username/" + id, String.class);
    }

    public boolean isValidUser(long id){
        return restTemplate.getForObject(baseUrl + "valid/" + id, Boolean.class);
    }
}
