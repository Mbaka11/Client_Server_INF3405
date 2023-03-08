package tp1_client_serveur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Iterator;

public class ClientResponseHandler {
	File localClientStorage;
	
	public ClientResponseHandler() {
		localClientStorage = new File(".\\ClientLocalStorage");
		if (localClientStorage.mkdir()) {
            System.out.println("Local Storage Directory created");
        }
        else {}
	}
	
	
	/**
	 * Handles exit command response from the server
	 * @param socket
	 * @throws IOException
	 */
	public void exitResponse(Socket socket) throws IOException {
		System.out.println("Disconnecting...");
		socket.close();
		System.exit(0);
	}
	

	/**
	 * Handles invalid command response from the server
	 * @param socket
	 * @throws IOException
	 */
	public void invalidCommandResponse(Socket socket) throws IOException {
		System.out.println("Command was invalid");
	}
	

	/**
	 * Receives the list of files from the server current directory and displays it
	 * @param socket
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void listFileResponse(Socket socket) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		String[] dirContent = (String[])in.readObject();
		System.out.println("List of file received : ");
		if (dirContent != null) {
			for (String content : dirContent) {
				System.out.println(content);
			}
		}else {
			System.out.println("Folder is empty");
		}
		
       
	}
	

	/**
	 * Confirm the change of directory on the client side
	 * @param socket
	 * @throws IOException
	 */
	public String changeDirResponseOk(Socket socket) throws IOException {
		DataInputStream in = new DataInputStream(socket.getInputStream());
		String newPath = in.readUTF();
		
		return newPath;
	}
	

	/**
	 * Create a new folder on the server side 
	 * @param socket
	 * @throws IOException
	 */
	public void makeDirResponseOk(Socket socket) throws IOException {
		System.out.println("New folder succesfully created");
	}
	

	/**
	 * Download a file from the server to the client local storage 
	 * @param socket
	 * @throws IOException
	 */
	public void downloadFileResponseOk(Socket socket) throws IOException { 
		// RESPONSE CODE | FILE NAME (UTF8) | FILE SIZE (long) | FILE CONTENT
		System.out.println("Will download");
		DataInputStream in = new DataInputStream(socket.getInputStream());

		int bytes = 0;
		String fileName = in.readUTF();
		long fileSize = in.readLong(); // in bytes
		long leftData = fileSize;
		
		byte[] transferBuffer = new byte[16 * 1024];
		int max = (int) Math.min(transferBuffer.length, fileSize);
		FileOutputStream fileOutputStream = new FileOutputStream(localClientStorage.getPath() + "\\"+ fileName);
	
		while (leftData > 0) {
			bytes = in.read(transferBuffer, 0, max);
			fileOutputStream.write(transferBuffer, 0, bytes);
			max = (int) Math.min(transferBuffer.length, leftData); // otherwise will block thread
			leftData = leftData - bytes;
		}
		
		fileOutputStream.close();
	}
	
	/**
	 * Upload a file to the server from the client local storage
	 * @param socket
	 * @param command
	 * @throws IOException
	 */
	public void uploadFile(Socket socket, String command) throws IOException { 
		// CONFIRMATION | FILE NAME (UTF8) | FILE SIZE (long) | FILE CONTENT
		// CANCEL
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		String fileName = Utils.extractCommandArgument(command);
		
		try {
			File askedFile = new File(localClientStorage.getPath() + "\\" + fileName);
			FileInputStream fileInputStream = new FileInputStream(askedFile);
			out.writeBoolean(true); // CONFIRMATION
			
			int bytes = 0;
			out.writeUTF(askedFile.getName().toString());
			out.writeLong(askedFile.length()); // in bytes
			
			byte[] transferBuffer = new byte[16 * 1024];
			
			bytes = fileInputStream.read(transferBuffer);
			while(bytes != -1) {
			
				out.write(transferBuffer,0,bytes);
	            out.flush();
	            bytes = fileInputStream.read(transferBuffer);
			}
			
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			out.writeBoolean(false);
		}
	}
	
	
	
}
