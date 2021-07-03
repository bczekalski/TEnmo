package com.techelevator.tenmo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private ObjectMapper mapper;

    public static void main(String[] args) throws JsonProcessingException {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.mapper = new ObjectMapper();
	}

	public void run() throws JsonProcessingException {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() throws JsonProcessingException {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		System.out.println("Your current account balance is: $" + authenticationService.balance(currentUser.getToken(),
				authenticationService.getAccIdByUserId(currentUser.getUser().getId())));
	}

	private void viewTransferHistory() throws JsonProcessingException {
		List<String> jsonHistory = authenticationService.transferHistory(currentUser.getToken(), currentUser.getUser().getId());
		List<Transfer> history = new ArrayList<>();
		for (String s : jsonHistory){
			Transfer temp = mapper.readValue(s, Transfer.class);
			history.add(temp);
		}
		System.out.println("-------------------------------------------");
		System.out.println("Transfers");
		System.out.println("ID          From/To                 Amount");
		System.out.println("-------------------------------------------");
		for (Transfer t : history){
			if(t.isTransferSent()) {
				System.out.println(t.getTransferId() + " \t\t " + "To: " +
						authenticationService.getUsernameByAccId(t.getReceiverId()) + " \t\t\t " + t.getAmount());
			}else{
				System.out.println(t.getTransferId() + " \t\t " + "From: " +
						authenticationService.getUsernameByAccId(t.getSenderId()) + " \t\t\t " + t.getAmount());
			} //need to get username out of the given account id through the transfer
		}
		System.out.println("-------------------------------------------");
		System.out.println("Please enter transfer ID to view details (0 to cancel): ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		if (isLong(input)){
			long id = Long.parseLong(input);
			if (id != 0) {
				Transfer t = authenticationService.getTransfer(currentUser.getToken(), currentUser.getUser().getId(), id);
				printTransfer(t);
			}
		}
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
	}

	private void sendBucks() throws JsonProcessingException {
		System.out.println("-------------------------------------------");
		System.out.println("Users");
		System.out.println("ID \t\t Name");
		System.out.println("-------------------------------------------");
		Map<String, String> users = authenticationService.listAll(currentUser.getToken());
		for (String key : users.keySet()) {
			System.out.println(key + "\t\t" + users.get(key));
		}
		System.out.println("-------------------------------------------");
		System.out.println("Enter ID of user you are sending to (0 to cancel): ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		if (isLong(input)){
			long receiverId = Integer.parseInt(input);
			if(authenticationService.isValidUser(receiverId)){
				System.out.println("Enter amount: ");
				input = scanner.nextLine();
				if(isDouble(input)){
					Transfer currentTransfer = new Transfer(authenticationService.getAccIdByUserId(currentUser.getUser().getId()),
							authenticationService.getAccIdByUserId(receiverId), BigDecimal.valueOf(Double.parseDouble(input)));
					if (authenticationService.sendMoney(currentTransfer, currentUser.getToken())) {
						currentTransfer.setTransferStatusId(2);
						currentTransfer.setTransferTypeId(2);
						Long newId = authenticationService.addTransfer(currentTransfer, currentUser.getToken());
						currentTransfer.setTransferId(newId);
						currentTransfer.setTransferStatusDesc("Approved");
						currentTransfer.setTransferTypeDesc("Send");
						printTransfer(currentTransfer);
					}
				}
			}
		}
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}

	private boolean isLong(String s){
		try {
			Long.parseLong(s);
		} catch (NumberFormatException e){
			System.out.println("Error, you did not enter a number.");
			return false;
		}catch (NullPointerException e){
			System.out.println("Error, you did not enter anything.");
			return false;
		}
		return true;
	}

	private boolean isDouble(String s){
		try {
			Double.parseDouble(s);
		} catch (NumberFormatException e){
			System.out.println("Error, you did not enter a number.");
			return false;
		}catch (NullPointerException e){
			System.out.println("Error, you did not enter anything.");
			return false;
		}
		return true;
	}

	private void printTransfer(Transfer t){
		if (t != null) {
			System.out.println("--------------------------------------------");
			System.out.println("Transfer Details");
			System.out.println("--------------------------------------------");
			System.out.println("Id: " + t.getTransferId());
			System.out.println("From: " + authenticationService.getUsernameByAccId(t.getSenderId()));
			System.out.println("To: " + authenticationService.getUsernameByAccId(t.getReceiverId()));
			System.out.println("Type: " + t.getTransferTypeDesc());
			System.out.println("Status: " + t.getTransferStatusDesc());
			System.out.println("Amount: $" + t.getAmount());
		} else {
			System.out.println("Error, the transfer ID you entered is not associated with a transfer.");
		}
	}
}
