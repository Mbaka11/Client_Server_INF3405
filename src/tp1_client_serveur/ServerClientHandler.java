package tp1_client_serveur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.Format;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerClientHandler extends Thread{
	private Socket socket;
	private int clientNumber;
	private String clientIp;
	private String clientPort;
	private ServerCommandHandler serverCommandHandler = new ServerCommandHandler();

	/**
	 * Constructor
	 * @param socket
	 * @param clientNumber
	 */
	public ServerClientHandler(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber;
		System.out.println("New Connection Client #"+clientNumber);
	
	}
	

	/**
	 * Related to Thread class
	 * Sends a welcome message to the client and starts the ready() method
	 */
	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(getWelcomeMessage());
			this.clientIp = this.socket.getRemoteSocketAddress().toString();
			this.clientPort = Integer.toString(this.socket.getPort());
			this.ready();
		} catch (IOException e) {
			System.out.println("Error handling client # " + clientNumber + "; " + e);
		} 
	}
	
	/**
	 * Reads the input stream and interprets the command
	 */
	public void ready() {
		DataInputStream in;
		String inputCommand = "";
		while(!this.socket.isClosed()) {
			try {
				in = new DataInputStream(socket.getInputStream());
				if (in.available() > 0) {
					inputCommand = in.readUTF();
					logToConsole(inputCommand);
					String commandPrefix = Utils.extractCommandPrefix(inputCommand);
					String commandArgument = Utils.extractCommandArgument(inputCommand);
					interpretCommand(commandPrefix, commandArgument, this.socket);
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		
		logErrorConsole("Client disconnected... Bye Bye");
		
		
	}
	


	/**
	 * Interprets the command and calls the appropriate method in the ServerCommandHandler
	 * @param commandPrefix
	 * @param commandArgument
	 * @param socket
	 * @throws IOException
	 */
	private void interpretCommand(String commandPrefix, String commandArgument, Socket socket) throws IOException {
//		System.out.println("PREFIX : " + commandPrefix);
//		System.out.println("SUFFIX : " + commandArgument);
		switch (commandPrefix) {
			case "cd": 
				serverCommandHandler.changeDirectory(socket, commandArgument);
				break;
				
			case "ls": 
				serverCommandHandler.listFileCommand(socket);
				break;
				
			case "mkdir": 
				serverCommandHandler.makeDirectoryCommand(socket, commandArgument);
				break;
				
			case "upload": 
				serverCommandHandler.uploadFileCommand(socket);
				break;
				
			case "download": 
				serverCommandHandler.downloadFileCommand(socket, commandArgument);
				break;
				
			case "exit": 
				serverCommandHandler.exitCommand(socket);
				break;
			
			default:
				serverCommandHandler.invalidCommand(socket);
				break;
		
		}	
	}
	
	
	/**
	 * Closes the socket
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		DataOutputStream out;
		try {
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Closing connection... Bye bye!" );
			this.socket.close();
		} catch (IOException e) {
			logErrorConsole("Could'nt close a socket");
		}
		System.out.println("Connection with client#" + clientNumber + " closed");
	}

	/**
	 * Returns a welcome message
	 * @return
	 */
	public String getWelcomeMessage() {
		return ("Welcome to KaarismaBox - you are client #" + clientNumber);
	}
	

	/**
	 * Logs the message to the console
	 * @param text
	 */
	public void logToConsole(String text) {
		System.out.println(getLogHeader() + text);
	}


	/**
	 * Logs the error message to the console
	 * @param text
	 */
	public void logErrorConsole(String text) {
		System.err.println(getLogHeader() + text);
	}
	

	/**
	 * Returns the log header
	 * @return
	 */
	public String getLogHeader() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();  
		String time = dtf.format(now);
		
		String messageHeader = "[" + clientIp + ":" + clientPort +"] - " + time +"] : ";
		return messageHeader;	
	}
}
