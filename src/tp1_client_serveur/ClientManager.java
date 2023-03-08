package tp1_client_serveur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientManager {
	static final private int MIN_PORT = 5000;
	static final private int MAX_PORT = 5050;
	private static Socket socket;
	private String serverAddress = "";
	private int port = -1;
	private final String consoleHeader = "[User]> ";
	private String currentWorkingDir = ".";
	private ClientResponseHandler clientResponseHandler = new ClientResponseHandler();
	
	public ClientManager() {}

	public ClientManager(String serverAddress, int port) {
		this.serverAddress = serverAddress;
		this.port = port;
		
		try {
			this.socket = new Socket(serverAddress, port);
			System.out.format("Serveur lance sur [%s:%d]", serverAddress, port);
			DataInputStream in = new DataInputStream(socket.getInputStream());
			System.out.println("\n\n");
			String welcomeMsgFromServer = in.readUTF();
			System.out.println(welcomeMsgFromServer);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Handle the connection to the server and the user input
	 */
	public void ready(){
		System.out.println("Ready");	
		Scanner inputScanner = new Scanner(System.in);
		
		
		while(this.socket.isConnected()) {
			System.out.print(consoleHeader + " " + currentWorkingDir + " " );	
			String inputCommand = "";
			inputCommand = inputScanner.nextLine();
			try {
				DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
				out.writeUTF(inputCommand);
				
				DataInputStream in = new DataInputStream(this.socket.getInputStream());
				int responseCode = in.readInt();
				this.handleResponse(responseCode, this.socket, inputCommand);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//receive
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
	}
	

	/**
	 * Handle the response from the server
	 * @param responseCode
	 * @param socket
	 * @param inputCommand
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void handleResponse(int responseCode, Socket socket, String inputCommand) throws IOException, ClassNotFoundException {
		switch (responseCode) {
			case ResponseCode.LIST_FILE_OK:
				clientResponseHandler.listFileResponse(socket);
				break;
			case ResponseCode.CHANGE_DIR_OK:
				currentWorkingDir = clientResponseHandler.changeDirResponseOk(socket);
				break;
			case ResponseCode.CHANGE_DIR_FAIL:
				System.err.println("Directory doesnt exist");
				break;
			case ResponseCode.MAKE_DIR_OK:
				clientResponseHandler.makeDirResponseOk(socket);
				break;
			case ResponseCode.MAKE_DIR_ERROR:
				System.err.println("Cant make new directory");
				break;
			case ResponseCode.DOWNLOAD_FILE_OK:
				clientResponseHandler.downloadFileResponseOk(socket);
				break;
			case ResponseCode.DOWNLOAD_FILE_ERROR:
				System.err.println("Error in the request");
				break;
			case ResponseCode.UPLOAD_FILE_READY:
				clientResponseHandler.uploadFile(socket, inputCommand);
				break;
			case ResponseCode.UPLOAD_FILE_ERROR:
				System.err.println("Error asking to upload");
				break;
			case ResponseCode.EXIT_OK:
				clientResponseHandler.exitResponse(socket);
				break;
			case ResponseCode.INVALID_COMMAND:
				clientResponseHandler.invalidCommandResponse(socket);
				break;
				
		}
	}
	
	/**
	 * Disconnect from the server
	 */
	public void disconnect(){
		System.out.println("Disconnecting...");
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Construct a ClientManager object with the server address and port number
	 * @return ClientManager
	 */
	static public ClientManager builder() {
		ClientManager clientManager = new ClientManager();
		System.out.println("Welcome to the Client connection assistant!");
		String serverAddress = askForServerAdress();
		int serverPort = askForServerPort();
		clientManager = new ClientManager(serverAddress, serverPort);
		
		return clientManager;
	}
	
	
	/**
	 * Ask the user for the server address
	 * @return String
	 */
	static private String askForServerAdress() {
		Scanner inputScanner = new Scanner(System.in);
		String input = "";
		while (!isValidIPAddress(input)) {
			System.out.println("Input valid server address to connect to: ");
			input = inputScanner.nextLine();
		}
		
		return input;
	}
	
	/**
	 * Ask the user for the server port
	 * @return int
	 */
	static private int askForServerPort() {
		Scanner inputScanner = new Scanner(System.in);
		int input = -1;
		while (!isValidPort(input)) {
			System.out.format("Input valid port to connect to (%d-%d):%n", MIN_PORT, MAX_PORT);
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
	 * Check if the IP address is valid
	 * @param ipAddress
	 * @return boolean
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
		}
		
		return true;
	}

	/**
	 * Check if the port is valid
	 * @param port
	 * @return boolean
	 */
	static private boolean isValidPort(int port) {
		if (port < MIN_PORT || port > MAX_PORT) {
			return false;
		}
		return true;
	}
}
