package plugin;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import plugin.Commands.DefaultIO;
import plugin.Server.ClientHandler;

public class AnomalyDetectionHandler implements ClientHandler{
	public int id = 0;
	public Socket s = null;
	
	public class SocketIO implements DefaultIO{
		private Socket socket = null;
		private DataInputStream input = null;
		private DataOutputStream output = null;
	    
	    public SocketIO(Socket nsocket)
	    {
	        // establish a connection 
	    	socket = nsocket;
	        try
	        {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
	        }
	        catch(UnknownHostException u)
	        {
	            System.out.println(u);
	        }
	        catch(IOException i)
	        {
	            System.out.println(i);
	        }
	    }
		
	    @Override
		public void write(String data)
		{
			try {
				output.write(data.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void write(float data)
		{
			try {
				output.writeFloat(data);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public String readText()
		{
			try {
				return input.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
	    }
		
		@Override
		public float readVal()
		{
			try {
				return input.readFloat();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
	    }
	}

	@Override
	public void start() {
		DefaultIO dio = new SocketIO(s);
		CLI cli = new CLI(dio,id);
		cli.start();
	}

	@Override
	public void generate(int cid, Socket sock) {
		id = cid;
		s = sock;
	}

	@Override
	public int getid() {
		return id;
	}

	@Override
	public Socket getSocket() {
		return s;
	}
}
