package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

//class to simplify socket communication
public class SocketIO {
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;
	
	public SocketIO(Socket nsocket)
	{
		socket = nsocket;
		try
		{
			input = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		catch(UnknownHostException u)
		{
			return;
		}
		catch(IOException i)
		{
			return;
		}
	}
	
	//constructor for client connection
	public SocketIO(String ip, int port) throws UnknownHostException, IOException
	{
		this(new Socket(ip, port));
	}
	
	//constructor for server connection (with single client, waits until connected to)
	public SocketIO(int port) throws UnknownHostException, IOException
	{
		this((new ServerSocket(port)).accept());
	}
	
	public void writeln(String data)
	{
		try {
			//add '\n' to data if none so it writes a line
			if (data.charAt(data.length()-1) != '\n')
				data += '\n';
			out.write(data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}
	
	public String readln()
	{
		try {
			return input.readLine();
		} catch (IOException e) {
			return "";
		}
	}
	
	public void close()
	{
		try {
			socket.close();
		} catch (IOException e) {
			return;
		}
	}
}