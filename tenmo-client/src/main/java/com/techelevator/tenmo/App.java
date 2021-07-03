package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;
import io.cucumber.java.eo.Do;

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

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
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
		System.out.println("Your current account balance is: $" + authenticationService.balance(currentUser.getToken(), currentUser.getUser().getId()));
	}

	private void viewTransferHistory() {
		List<String> history = authenticationService.transferHistory(currentUser.getToken(), currentUser.getUser().getId());
		System.out.println("-------------------------------------------");
		System.out.println("Transfers");
		System.out.println("ID          From/To                 Amount");
		System.out.println("-------------------------------------------");
		for (String h : history){
			String[] s = h.split("\\|");
			System.out.println(s[0] + " \t " + s[1] + s[2] + " \t\t " + s[3]);
		}
		System.out.println("-------------------------------------------");
		System.out.println("Please enter transfer ID to view details (0 to cancel): ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		if (isInteger(input)){
			int id = Integer.parseInt(input);
			if (id != 0) {
				printTransfer(authenticationService.getTransfer(currentUser.getToken(), currentUser.getUser().getId(), id));
			}
		}
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
	}

	private void sendBucks() {
		System.out.println("-------------------------------------------");
		System.out.println("Users");
		System.out.println("ID \t\t Name");
		System.out.println("-------------------------------------------");
		Map<String, String> userMap = authenticationService.listAll(currentUser.getToken());
		List<User> users = new ArrayList<>();
		Transfer currentTransfer = new Transfer();
		for (String key : userMap.keySet()){
			User user = new User();
			user.setId(Integer.parseInt(key));
			user.setUsername(userMap.get(key));
			users.add(user);
		}
		for (User u : users){
			System.out.println(u.getId() + "\t\t" + u.getUsername());
		}
		currentTransfer.setSenderId(currentUser.getUser().getId());
		System.out.println("-------------------------------------------");
		System.out.println("Enter ID of user you are sending to (0 to cancel): ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		if (isInteger(input)){
			int receiverId = Integer.parseInt(input);
			if(authenticationService.isValidUser(receiverId)){
				currentTransfer.setReceiverId(receiverId);
				System.out.println("Enter amount: ");
				input = scanner.nextLine();
				if(isDouble(input)){
					currentTransfer.setAmount(BigDecimal.valueOf(Double.parseDouble(input)));
					if (authenticationService.sendMoney(currentTransfer, currentUser.getToken())) {
						currentTransfer.setTransferStatusId(2);
						currentTransfer.setTransferTypeId(2);
						Transfer transfer = authenticationService.addTransfer(currentTransfer, currentUser.getToken());
						printTransfer(transfer);

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

	private boolean isInteger(String s){
		try {
			Integer.parseInt(s);
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
			System.out.println("From: " + authenticationService.getUsernameById(t.getSenderId()));
			System.out.println("To: " + authenticationService.getUsernameById(t.getReceiverId()));
			System.out.println("Type: " + t.getTransferTypeDesc());
			System.out.println("Status: " + t.getTransferStatusDesc());
			System.out.println("Amount: $" + t.getAmount());
		} else {
			System.out.println("Error, the transfer ID you entered is not associated with a transfer.");
		}
	}
}
