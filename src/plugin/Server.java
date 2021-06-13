package plugin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	
	public interface ClientHandler{
		public void generate(int id, Socket s);
		public int getid();
		public Socket getSocket();
		public void start();
	}

	volatile boolean stop;
	public Server() {
		stop=false;
	}
	
	private Socket openSocketOnPort(int port)
	{
		try {
			ServerSocket server = new ServerSocket(port);
			return server.accept();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void startServer(int port, ClientHandler ch) {
		ch.generate(port, openSocketOnPort(port));
		clients.add(ch);
		ch.start();
	}
	
	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(()->startServer(port,ch)).start();
	}
	
	public void stop() {
		stop=true;
		for (ClientHandler c : clients)
		{
			try {
				c.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
