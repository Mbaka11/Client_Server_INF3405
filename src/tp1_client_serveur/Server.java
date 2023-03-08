package tp1_client_serveur;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Server {
	
	
	
	public static void main(String[] args) throws IOException {
		ServerManager serverManager = ServerManager.builder();
		serverManager.readyHandleClients();
		
	}
}
