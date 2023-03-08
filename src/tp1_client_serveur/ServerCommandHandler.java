package tp1_client_serveur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerCommandHandler {
	private final String basePath = ".\\"; // ./data/
	String currentPath = basePath;
	File directoryPath;
	
	// FileFilter to get only directories
	FileFilter directoryFileFilter = new FileFilter() {
	    public boolean accept(File file) {
	        return file.isDirectory();
	    }
	};
	
	public ServerCommandHandler() {
		this.directoryPath = new File(currentPath);
	}
	
	
	/**
	 * Close the socket and session
	 * @param socket
	 * @throws IOException
	 */
	public void exitCommand(Socket socket) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeInt(ResponseCode.EXIT_OK);
		socket.close();
	}
	
	
	/**
	 * Invalid command handler
	 * @param socket
	 * @throws IOException
	 */
	public void invalidCommand(Socket socket) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeInt(ResponseCode.INVALID_COMMAND);
	}
	

	/**
	 * List the content of the current directory
	 * @param socket
	 * @throws IOException
	 */
	public void listFileCommand(Socket socket) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeInt(ResponseCode.LIST_FILE_OK);
		ObjectOutputStream outObject = new ObjectOutputStream(socket.getOutputStream());
		String dirContent[] = this.directoryPath.list();
		outObject.writeObject(dirContent);
	}
	

	/**
	 * Create a new directory in the current directory
	 * @param socket
	 * @param commandArgument
	 * @throws IOException
	 */
	public void makeDirectoryCommand(Socket socket, String commandArgument) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		File newFolder = new File(currentPath + "\\" + commandArgument);
		if (newFolder.mkdir()) {
            System.out.println("Directory is created");
            out.writeInt(ResponseCode.MAKE_DIR_OK);
        }
        else {
            System.out.println("Directory cannot be created");
            out.writeInt(ResponseCode.MAKE_DIR_ERROR);
        }
		
	}
	

	/**
	 * Change the current directory navigating the server filesystem
	 * @param socket
	 * @param commandArgument
	 * @throws IOException
	 */
	public void changeDirectory(Socket socket, String commandArgument) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
		if (commandArgument.equals("..")) {
			goToParent();
			out.writeInt(ResponseCode.CHANGE_DIR_OK);
			out.writeUTF(this.currentPath);
			return;
		}
		
		File[] dirContent = this.directoryPath.listFiles(directoryFileFilter);
		if (dirContent != null) {
			for (File file : dirContent) {
			if (file.getName().equals(commandArgument)) {
				addFileToCursorPath(file);
				out.writeInt(ResponseCode.CHANGE_DIR_OK);
				out.writeUTF(this.currentPath);
				return;
			}
		}
		}
		
		out.writeInt(ResponseCode.CHANGE_DIR_FAIL);
	}
	

	/**
	 * Download a file from the server to the client side (server -> client)
	 * @param socket
	 * @param commandArgument
	 * @throws IOException
	 */
	public void downloadFileCommand(Socket socket, String commandArgument) throws IOException {
		// RESPONSE CODE | FILE NAME (UTF8) | FILE SIZE (long) | FILE CONTENT
		//System.out.println("ASKED FOR download");
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		try {
			File askedFile = new File(directoryPath.getPath() + "\\" + commandArgument);
			FileInputStream fileInputStream = new FileInputStream(askedFile);
			out.writeInt(ResponseCode.DOWNLOAD_FILE_OK);
			
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
			out.writeInt(ResponseCode.DOWNLOAD_FILE_ERROR);
		}
	}
	

	/**
	 * Upload a file to the server from the client side (client -> server)
	 * @param socket
	 * @throws IOException
	 */
	public void uploadFileCommand(Socket socket) throws IOException {
		// CONFIRMATION | FILE NAME (UTF8) | FILE SIZE (long) | FILE CONTENT
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeInt(ResponseCode.UPLOAD_FILE_READY);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		if (!in.readBoolean()) {
			return;
		}
		int bytes = 0;
		String fileName = in.readUTF();
		long fileSize = in.readLong(); // in bytes
		long leftData = fileSize;
		
		byte[] transferBuffer = new byte[16 * 1024];
		int max = (int) Math.min(transferBuffer.length, fileSize);
		FileOutputStream fileOutputStream = new FileOutputStream(directoryPath.getPath() + "\\"+ fileName);
	
		while (leftData > 0) {
			bytes = in.read(transferBuffer, 0, max);
			fileOutputStream.write(transferBuffer, 0, bytes);
			max = (int) Math.min(transferBuffer.length, leftData); // otherwise will block thread
			leftData = leftData - bytes;
		}
		
		fileOutputStream.close();
	}
	
	
	/**
	 * Add file (folder) to cursor path
	 * @param file
	 */
	private void addFileToCursorPath(File file) {
		this.currentPath += "\\" + file.getName();
		this.directoryPath = new File(currentPath);
	}
	


	/**
	 * Go to parent directory
	 */
	private void goToParent() {
		if (this.directoryPath.getParent() == null) {
			return;
		}
		this.currentPath = this.directoryPath.getParent().toString();
		this.directoryPath = new File(currentPath);
	}
	
	
	
}
