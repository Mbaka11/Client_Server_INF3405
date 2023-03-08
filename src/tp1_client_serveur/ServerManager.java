package tp1_client_serveur;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ServerManager {
	static final private int MIN_PORT = 5000;
	static final private int MAX_PORT = 5050;
	private int clientNumber = 0;
	private String serverAddress= ""; 
	private int serverPort = -1;
	private static ServerSocket Listener;
	
	
	
	public ServerManager() {}
	
	public ServerManager(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		
		try {
			this.Listener = new ServerSocket();
			this.Listener.setReuseAddress(true);
			//System.out.println("DEBUG : All private attributes set.");
			
			//System.out.println("DEBUG : SETTING IP ADDRESS.");
			InetAddress serverIP = InetAddress.getByName(this.serverAddress);
			
			Listener.bind(new InetSocketAddress(serverIP, this.serverPort));
			System.out.format("The server is running on %s:%d%n", this.serverAddress, this.serverPort);
			
		} catch (IOException e) {
			System.err.println("Caught an exception " + e);
		}
		
	}
	
	
	/**
	 * Handle new clients that connect to the server socket.
	 */
	public void readyHandleClients() {
		try {
			//System.out.println("DEBUG : HANDLING CLIENT");
			while (true) {
				new ServerClientHandler(Listener.accept(), clientNumber++).start();
				System.out.println("new client");
			}
		} catch (IOException e) {
			System.err.println("Caught an exception " + e);
		} finally {
			try {
				this.Listener.close();
			} catch (IOException e) {
				System.err.println("Caught an exception " + e);
			}
		} 
	}
	
	
	/**
	 * Build a ServerManager object
	 * @return the ServerManager object
	 */
	static public ServerManager builder() {
		ServerManager serverManager = new ServerManager();
		
		System.out.println("Welcome to the Server setup assistant!");
		String serverAddress = askForServerAdress();
		int serverPort = askForServerPort();
		serverManager = new ServerManager(serverAddress, serverPort);
		
		return serverManager;
	}


	/**
	 * Ask for the server address to connect to
	 * @return the server address
	 */
	static private String askForServerAdress() {
		Scanner inputScanner = new Scanner(System.in);
		String input = "";
		while (!isValidIPAddress(input)) {
			System.out.println("Input valid server address to use: ");
			input = inputScanner.nextLine();
		}
		
		return input;
	}
	
	/**
	 * Ask for the server port to connect to
	 * @return the server port
	 */
	static private int askForServerPort() {
		Scanner inputScanner = new Scanner(System.in);
		int input = -1;
		while (!isValidPort(input)) {
			System.out.format("Input valid port to use (%d-%d):%n", MIN_PORT, MAX_PORT);
			try {
				input = inputScanner.nextInt();
			} catch (InputMismatchException e) {
				System.err.println("Wrong input type (int required)");
				inputScanner.nextLine();
			}
		}
		
		return input;
	}
	

	/**
	 * Check if the given IP address is valid
	 * @param ipAddress the IP address to check
	 * @return true if the IP address is valid, false otherwise
	 */
	static private boolean isValidIPAddress(String ipAddress) {
		String copy = ipAddress;
		String[] networkIDs = copy.split("\\.", -2);
		if (networkIDs.length != 4) {
			// does not respect subnet formats
			return false;
		}
		
		for (String id: networkIDs) {
			if (id.isEmpty()) { // is empty
				return false;
			}
			if (!id.matches("[0-9]+")) { // doesnt use numbers
				return false;
			}
			int id_num = Integer.parseInt(id);
			if (id_num > 255 || id_num < 0) {
				return false;
			}
		}
		
		return true;
	}


	/**
	 * Check if the given port is valid
	 * @param port the port to check
	 * @return true if the port is valid, false otherwise
	 */
	static private boolean isValidPort(int port) {
		if (port < MIN_PORT || port > MAX_PORT) {
			return false;
		}
		return true;
	}

}

