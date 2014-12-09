// 1 - gameoption and indication of starting of the game
// 2 - opponents name
// 3 - the location chosen by the player for each button clicking

package com.example.serverfinal12;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.CountDownTimer;

public class Server extends ActionBarActivity 
{
	// Port where the server is ready to listen
	static final int SocketServerPORT = 6000;
	// gameMsg - Information about proceedings of the game
	TextView infoIp, infoPort, gameMsg;
	String msgLog = "";
	// When multiple players are playing 'X' and 'O' players are stored in separate lists
	List<GameClient> playerListO;
	List<GameClient> playerListX;

	ServerSocket serverSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		infoIp = (TextView) findViewById(R.id.infoip);
		infoPort = (TextView) findViewById(R.id.infoport);
		gameMsg = (TextView) findViewById(R.id.gamemsg);

		infoIp.setText(getIpAddress());

		playerListO = new ArrayList<GameClient>();
		playerListX = new ArrayList<GameClient>();
  
		GameServerThread GameServerThread = new GameServerThread();
		GameServerThread.start();
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();

		if (serverSocket != null) 
		{
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 	}

	// Thread which starts accepting the connections
	private class GameServerThread extends Thread 
	{
		@Override
		public void run() 
		{
			Socket socket = null;
			try 
			{
				// welcome socket on the client side
				serverSocket = new ServerSocket(SocketServerPORT);
				Server.this.runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						infoPort.setText("I'm waiting here: "+ serverSocket.getLocalPort());
					}
				});
				
				// accepting new clients. The server decides whether the player will take 'O' or 'X' depending upon the availability in each of the lists.
				while (true) 
				{
					socket = serverSocket.accept();
					GameClient client = null;
					if (playerListO.size()<= playerListX.size())
					{
						client = new GameClient();
						client.gameOption = 'O';
						client.opponent = null;
						playerListO.add(client);
					}
					else
					{
						client = new GameClient();
						client.gameOption = 'X';
						client.opponent = null;
						playerListX.add(client);
					}	
					// starting a separate thread for each client
					ConnectThread connectThread = new ConnectThread(client, socket);
					connectThread.start();					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
 
	private class ConnectThread extends Thread 
	{ 
		Socket socket;
		GameClient connectClient;
		String msgToSend = "";

		ConnectThread(GameClient client, Socket socket)
		{
			connectClient = client;
			this.socket= socket;
			client.socket = socket;
			client.gameThread = this;
		}

		@Override
		public void run() 
		{
			// generating input and output streams
			DataInputStream dataInputStream = null;
			DataOutputStream dataOutputStream = null;  
			try 
			{
				dataInputStream = new DataInputStream(socket.getInputStream());
				dataOutputStream = new DataOutputStream(socket.getOutputStream());  
				String n = dataInputStream.readUTF();    
				connectClient.name = n;    
				msgLog += connectClient.name + " connected@" + connectClient.socket.getInetAddress() + 
						":" + connectClient.socket.getPort() + "\n";
				Server.this.runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						gameMsg.setText(msgLog);
					}
				});
    
				dataOutputStream.writeUTF("Welcome " + n + "\n");
				dataOutputStream.flush();
    
				broadcastMsg("2|"+ n + "| joined the game.\n",this.connectClient);		
				
				// wait until partner player is idle or newly joined
				if(connectClient.opponent == null)
				{
					dataOutputStream.writeUTF("Checking if there is any player available if not you may need to wait for sometime\n");
					dataOutputStream.flush();
					GameClient opponent = null;
					while((opponent = findOpponent(connectClient))== null)
					{
						
					}
					connectClient.opponent = opponent;
					opponent.opponent = connectClient;
					while(opponent.name == null)
					{
						
					}
				}
				
				msgLog += connectClient.name + " is playing now \n" ;
				int gamopt = 0;
				if(connectClient.gameOption == 'O')
					gamopt = 1;
				else 
					gamopt = 0;
				msgLog += "1| Start the game now |"+gamopt+"|"+connectClient.opponent.name+"\n";
				Server.this.runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						gameMsg.setText(msgLog);
					}
				});
				sendMsg("1| Start the game now |"+gamopt+"|"+connectClient.opponent.name+"\n");
				// once this message reaches the client he can start the game (sending the symbol to choose and opponents name) 
    
				while (true) 
				{
					if (dataInputStream.available() > 0) 
					{
						String newMsg = dataInputStream.readUTF();     
						msgLog += n + ": " + newMsg +"\n";
						Server.this.runOnUiThread(new Runnable() 
						{
							@Override
							public void run() 
							{
								gameMsg.setText(msgLog);
							}
						});
      
						broadcastMsg(newMsg,this.connectClient);
					}
     
					if(!msgToSend.equals(""))
					{
						dataOutputStream.writeUTF(msgToSend);
						dataOutputStream.flush();
						msgToSend = "";
					}
				}    
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (dataInputStream != null) 
				{
					try 
					{
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dataOutputStream != null) 
				{
					try 
					{
						dataOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(connectClient.gameOption == 'X')
					playerListX.remove(connectClient);
				else 
					playerListO.remove(connectClient);
				
				Server.this.runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						Toast.makeText(Server.this, connectClient.name + " left.\n", Toast.LENGTH_LONG).show();     
						msgLog += "---- " + connectClient.name + " left\n";
						Server.this.runOnUiThread(new Runnable() 
						{
							@Override
							public void run() 
							{
								gameMsg.setText(msgLog);
							}
						});      
						broadcastMsg("-- " + connectClient.name + " left\n",connectClient);
					}
				});
			}   
		}
  
		private void sendMsg(String msg)
		{
			msgToSend = msg;
		}  
	}
	
	// finding an opponent to play with. If not found it returns null.
	private GameClient findOpponent(GameClient client)
	{
		if (client.gameOption == 'X')
		{
			for(GameClient i : playerListO)
			{
				if (i.opponent == null)
					return i;
			}
		}
		else if (client.gameOption == 'O')
		{
			for(GameClient i : playerListX)
			{
				if (i.opponent == null)
					return i;
			}
		}
		return null;
	}
 
	// message will be broadcasted to only the player with whom the current player is playing
	private void broadcastMsg(String msg,GameClient client)
	{
		client.gameThread.sendMsg(msg);
		msgLog += "- send to " + client.name + "\n";
		if(client.opponent != null)
		{
			client.opponent.gameThread.sendMsg(msg);
			msgLog += "- send to " + client.opponent.name + "\n";
		}
  
		Server.this.runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				gameMsg.setText(msgLog);
			}
		});
	}

 	private String getIpAddress() 
 	{
 		String ip = "";
 		try {
 			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
 			while (enumNetworkInterfaces.hasMoreElements()) 
 			{
 				NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
 				Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
 				while (enumInetAddress.hasMoreElements()) 
 				{
 					InetAddress inetAddress = enumInetAddress.nextElement();
 					if (inetAddress.isSiteLocalAddress()) 
 					{
 						ip += "SiteLocalAddress: "+ inetAddress.getHostAddress() + "\n";
 					}
 				}
 			}
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			ip += "Something Wrong! " + e.toString() + "\n";
 		}
 		return ip;
 	}

 	// simple class for the client
	class GameClient 
	{
		String name;
		Socket socket;
		ConnectThread gameThread;
		char gameOption;
		GameClient opponent;
	}
}
