package tp1_client_serveur;

import java.io.DataInputStream;
import java.net.Socket;

public class Client {
	
	
	public static void main(String[] args) throws Exception{
		
		ClientManager clientManager = ClientManager.builder();
		clientManager.ready();
		
		
	}

}
