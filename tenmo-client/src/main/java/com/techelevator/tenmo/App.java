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

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.mapper = new ObjectMapper();
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
		System.out.println("Your current account balance is: $" + authenticationService.balance(currentUser.getToken(),
				authenticationService.getAccIdByUserId(currentUser.getUser().getId())));
	}

	private void viewTransferHistory() {
    	//Map the transfers to a list of strings with the strings being JSON
		List<String> jsonHistory = authenticationService.transferHistory(currentUser.getToken(), currentUser.getUser().getId());
		List<Transfer> history = new ArrayList<>();
		boolean historySuccess = false;
		//try to turn the JSON back into transfer objects and put them in a list
		try {
			for (String s : jsonHistory) {
				Transfer temp = mapper.readValue(s, Transfer.class);
				history.add(temp);
			}
			historySuccess = true;
		}catch(JsonProcessingException e){
			System.out.println("Error, the was an issue getting your transfer history.");
		}
		//if this passes, print out the list of transfers
		if (historySuccess) {
			System.out.println("-------------------------------------------");
			System.out.println("Transfers");
			System.out.println("ID          From/To                 Amount");
			System.out.println("-------------------------------------------");
			for (Transfer t : history) {
				if (t.isTransferSent()) {
					System.out.println(t.getTransferId() + " \t\t " + "To: " +
							authenticationService.getUsernameByAccId(t.getReceiverId()) + " \t\t\t " + t.getAmount());
				} else {
					System.out.println(t.getTransferId() + " \t\t " + "From: " +
							authenticationService.getUsernameByAccId(t.getSenderId()) + " \t\t\t " + t.getAmount());
				}
			}
			System.out.println("-------------------------------------------");
			System.out.println("Please enter transfer ID to view details (0 to cancel): ");
			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine();
			//check the transfer Id entered to make sure it follows proper formatting
			if (isLong(input)) {
				long id = Long.parseLong(input);
				if (id != 0) { //if it is 0, it must return to main menu, so if it isn't zero we should try to get the transfer
					Transfer t = authenticationService.getTransfer(currentUser.getToken(), currentUser.getUser().getId(), id);
					printTransfer(t);
				}
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
		//acquires a map of all usernames and ids for printing
		Map<String, String> users = authenticationService.listAll(currentUser.getToken());
		//prints out the users in formatting from above
		for (String key : users.keySet()) {
			System.out.println(key + "\t\t" + users.get(key));
		}
		System.out.println("-------------------------------------------");
		System.out.println("Enter ID of user you are sending to (0 to cancel): ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		//confirm that the input is a proper number that can be used
		if (isLong(input)){
			//confirm that the id entered is not the current user id
			if (currentUser.getUser().getId() != Long.parseLong(input)) {
				long receiverId = Long.parseLong(input);
				//confirm that the id entered is a valid user id
				if (authenticationService.isValidUser(receiverId)) {
					System.out.println("Enter amount: ");
					input = scanner.nextLine();
					//confirm that the amount is a proper number that can be used
					if (isDouble(input)){
						BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(input));
						//confirm that the amount entered is not greater than the user's balance
						if (authenticationService.balance(currentUser.getToken(),
								authenticationService.getAccIdByUserId(currentUser.getUser().getId())).
								compareTo(amount) > 0) {
							//Assign the known values to a transfer to pass it to the server
							Transfer currentTransfer = new Transfer(authenticationService.getAccIdByUserId(currentUser.getUser().getId()),
									authenticationService.getAccIdByUserId(receiverId), amount);
							//pass it to the server and ensure that no issues occurred during it sending
							if (authenticationService.sendMoney(currentTransfer, currentUser.getToken())) {
								currentTransfer.setTransferStatusId(2);
								currentTransfer.setTransferTypeId(2);
								Long newId = authenticationService.addTransfer(currentTransfer, currentUser.getToken());
								currentTransfer.setTransferId(newId);
								currentTransfer.setTransferStatusDesc("Approved");
								currentTransfer.setTransferTypeDesc("Send");
								//Having assigned the remaining data for a transfer after adding it to db, print it.
								printTransfer(currentTransfer);
							} else {
								System.out.println("Error, there was an issue sending money to the user.");
							}
						}else{
							System.out.println("Error, you don't have that much money.");
						}
					}
				}else{
					System.out.println("Error, you did not enter an Id associated with any user.");
				}
			}else{
				System.out.println("Error, you can not send money to yourself.");
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
		if (Double.parseDouble(s) <= 0){
			System.out.println("Error, you can not send 0 or a negative amount of money");
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
