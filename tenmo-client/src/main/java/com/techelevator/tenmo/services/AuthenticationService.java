package com.techelevator.tenmo.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.UserCredentials;

public class AuthenticationService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();

    public AuthenticationService(String url) {
        this.baseUrl = url;
    }

    public AuthenticatedUser login(UserCredentials credentials) throws AuthenticationServiceException {
        HttpEntity<UserCredentials> entity = createRequestEntity(credentials);
        return sendLoginRequest(entity);
    }

    public void register(UserCredentials credentials) throws AuthenticationServiceException {
    	HttpEntity<UserCredentials> entity = createRequestEntity(credentials);
        sendRegistrationRequest(entity);
    }

    public BigDecimal balance(int id){
		return restTemplate.getForObject(baseUrl + "balance/" + id, BigDecimal.class);
	}

	public List<String> transferHistory(int id){
    	return restTemplate.getForObject(baseUrl + "history/" + id, List.class);
	}

	public List<User> listAll(){
    	return restTemplate.getForObject(baseUrl + "list", List.class);
	}

	private HttpEntity<UserCredentials> createRequestEntity(UserCredentials credentials) {
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<UserCredentials> entity = new HttpEntity<>(credentials, headers);
    	return entity;
    }

	private AuthenticatedUser sendLoginRequest(HttpEntity<UserCredentials> entity) throws AuthenticationServiceException {
		try {	
			ResponseEntity<AuthenticatedUser> response = restTemplate.exchange(baseUrl + "login", HttpMethod.POST, entity, AuthenticatedUser.class);
			return response.getBody(); 
		} catch(RestClientResponseException ex) {
			String message = createLoginExceptionMessage(ex);
			throw new AuthenticationServiceException(message);
        }
	}

    private ResponseEntity<Map> sendRegistrationRequest(HttpEntity<UserCredentials> entity) throws AuthenticationServiceException {
    	try {
			return restTemplate.exchange(baseUrl + "register", HttpMethod.POST, entity, Map.class);
		} catch(RestClientResponseException ex) {
			String message = createRegisterExceptionMessage(ex);
			throw new AuthenticationServiceException(message);
        }
	}

	//private ResponseEntity<BigDecimal> sendBalanceRequest(HttpEntity<UserCredentials> entity){


	//}


	private String createLoginExceptionMessage(RestClientResponseException ex) {
		String message = null;
		if (ex.getRawStatusCode() == 401 && ex.getResponseBodyAsString().length() == 0) {
		    message = ex.getRawStatusCode() + " : {\"timestamp\":\"" + LocalDateTime.now() + "+00:00\",\"status\":401,\"error\":\"Invalid credentials\",\"message\":\"Login failed: Invalid username or password\",\"path\":\"/login\"}";
		}
		else {
		    message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
		}
		return message;
	}
	
	private String createRegisterExceptionMessage(RestClientResponseException ex) {
		String message = null;
		if (ex.getRawStatusCode() == 400 && ex.getResponseBodyAsString().length() == 0) {
		    message = ex.getRawStatusCode() + " : {\"timestamp\":\"" + LocalDateTime.now() + "+00:00\",\"status\":400,\"error\":\"Invalid credentials\",\"message\":\"Registration failed: Invalid username or password\",\"path\":\"/register\"}";
		}
		else {
		    message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
		}
		return message;
	}
}
