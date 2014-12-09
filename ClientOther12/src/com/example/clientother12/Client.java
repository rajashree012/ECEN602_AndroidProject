package com.example.clientother12;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.example.clientother12.TicTacToeGame;

import android.support.v7.app.ActionBarActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Client extends ActionBarActivity 
{
	ArrayList<String> input = new ArrayList<String>(); // from the server messages are put into this buffer
	
	static final int SocketServerPORT = 6000;
	 
	//  Design of the UI
	LinearLayout loginPanel, gamePanel;

	EditText editTextUserName, editTextAddress;
	Button buttonConnect;
	TextView textPort;
 
	TextView message;
	TextView player,opponent;
 	Button buttonDisconnect;

 	// few parameters to be used by all the threads
 	String msgLog = "";
 	String msgLog1 = "";
 	int temp1 = 0,temp2 = 0;
	gameClientThread gameClientThread = null;
	StartGame startGame = null;
	Playing playing = null;
 	
 	private TicTacToeGame mGame;  
    
    private Button mBoardButtons[]; 
      
    private TextView mInfoTextView;  
    private TextView mHumanCount;  
    private TextView mTieCount;  
    private TextView mOpponentCount;  
      
    private int mHumanCounter = 0;  
    private int mTieCounter = 0;  
    private int mOpponentCounter = 0;  
      
    private boolean mHumanFirst = true;  
    private boolean mGameOver = false;
 	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_client);
    	 
    	loginPanel = (LinearLayout)findViewById(R.id.loginpanel);
    	gamePanel = (LinearLayout)findViewById(R.id.gamepanel);

    	editTextUserName = (EditText) findViewById(R.id.username);
    	editTextAddress = (EditText) findViewById(R.id.address);
    	textPort = (TextView) findViewById(R.id.port);
    	textPort.setText("port: " + SocketServerPORT);
    	buttonConnect = (Button) findViewById(R.id.connect);
    	buttonDisconnect = (Button) findViewById(R.id.disconnect);
    	message = (TextView) findViewById(R.id.message);
    	player = (TextView) findViewById(R.id.player);
    	opponent = (TextView) findViewById(R.id.opponent);

    	buttonConnect.setOnClickListener(buttonConnectOnClickListener);
    	buttonDisconnect.setOnClickListener(buttonDisconnectOnClickListener);
	   	 
		mBoardButtons = new Button[mGame.getBOARD_SIZE()];  
		mBoardButtons[0] = (Button) findViewById(R.id.one);  
		mBoardButtons[1] = (Button) findViewById(R.id.two);  
		mBoardButtons[2] = (Button) findViewById(R.id.three);  
		mBoardButtons[3] = (Button) findViewById(R.id.four);  
		mBoardButtons[4] = (Button) findViewById(R.id.five);  
		mBoardButtons[5] = (Button) findViewById(R.id.six);  
		mBoardButtons[6] = (Button) findViewById(R.id.seven);  
		mBoardButtons[7] = (Button) findViewById(R.id.eight);  
		mBoardButtons[8] = (Button) findViewById(R.id.nine);  
		  
		mInfoTextView = (TextView) findViewById(R.id.information);  
		mHumanCount = (TextView) findViewById(R.id.humanCount);  
		mTieCount = (TextView) findViewById(R.id.tiesCount);  
		mOpponentCount = (TextView) findViewById(R.id.opponentCount);  
		  
		mHumanCount.setText(Integer.toString(mHumanCounter));  
		mTieCount.setText(Integer.toString(mTieCounter));  
		mOpponentCount.setText(Integer.toString(mOpponentCounter));
    }
    
    private class ButtonClickListener implements View.OnClickListener  
    {  
         int location;  
           
         public ButtonClickListener(int location)  
         {  
              this.location = location;  
         }  
           
         public void onClick(View view)  
         {  
              if (!mGameOver)  
              {  
                   if (mBoardButtons[location].isEnabled())  
                   { 
                	    // for each move made by a player a new thread start
                        gameClientThread.sendMsg("3|"+location+"|");
                        playing = new Playing(location);
                        playing.start();
                   }  
              }  
         }  
    }  
      
    // stores the values of moves and it is shown on the UI
    private void setMove(char player, int location)  
    {
    	mGame.setMove(player, location);
    	mBoardButtons[location].setEnabled(false);  
    	mBoardButtons[location].setText(String.valueOf(player));  
    	if (player == mGame.OPPONENT_PLAYER)  
    		mBoardButtons[location].setTextColor(Color.GREEN);  
    	else  
    		mBoardButtons[location].setTextColor(Color.RED);  
    }
 
    // on clicking the player will disconnect from the server
    OnClickListener buttonDisconnectOnClickListener = new OnClickListener() 
    {
    	@Override
    	public void onClick(View v) 
    	{
    		if(Client.this.gameClientThread==null)
    		{
    			return;
    		}
    		Client.this.gameClientThread.disconnect();
    	}  
    };

    // on clicking player will connect to teh server
    OnClickListener buttonConnectOnClickListener = new OnClickListener() 
    {
    	@Override
    	public void onClick(View v) 
    	{
    		String textUserName = editTextUserName.getText().toString();
    		if (textUserName.equals("")) 
    		{
    			Toast.makeText(Client.this, "Enter User Name",Toast.LENGTH_LONG).show();
    			return;
    		}

    		String textAddress = editTextAddress.getText().toString();
    		if (textAddress.equals("")) 
    		{
    			Toast.makeText(Client.this, "Enter Addresse",Toast.LENGTH_LONG).show();
    			return;
    		}
   
    		msgLog = "";
    		message.setText(msgLog);
    		
    		// a separate thread is created for the start of each game
    		startGame = new StartGame(textUserName, textAddress, SocketServerPORT);
    		startGame.start();
    	}
    };

    // thread which deals with communicating with the server
    private class gameClientThread extends Thread 
    {
    	String name;
    	String dstAddress;
    	int dstPort;
    	
    	String msgToSend = "";
    	boolean goOut = false;

    	gameClientThread(String name, String address, int port) 
    	{
    		this.name = name;
    		dstAddress = address;
    		dstPort = port;
    	}

    	@Override
    	public void run() 
    	{
    		Socket socket = null;
    		DataOutputStream dataOutputStream = null;
    		DataInputStream dataInputStream = null;

    		try 
    		{
    			socket = new Socket(dstAddress, dstPort);
    			dataOutputStream = new DataOutputStream(socket.getOutputStream());
    			dataInputStream = new DataInputStream(socket.getInputStream());
    			dataOutputStream.writeUTF(name);
    			dataOutputStream.flush();

    			while (!goOut) 
    			{
    				if (dataInputStream.available() > 0) 
    				{
    					msgLog = "";
    					msgLog = dataInputStream.readUTF();
    					input.add(msgLog);
    					Client.this.runOnUiThread(new Runnable() 
    					{
    						@Override
    						public void run() 
    						{
    							message.setText(msgLog);
    						}
    					});
    					synchronized(this)
    					{
	    					notify();
    					}    					
    				}
     
    				if(!msgToSend.equals("")){
    					dataOutputStream.writeUTF(msgToSend);
    					dataOutputStream.flush();
    					msgToSend = "";
    				}
    			}

    		} catch (UnknownHostException e) {
    			e.printStackTrace();
    			final String eString = e.toString();
    			Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() {
    					Toast.makeText(Client.this, eString, Toast.LENGTH_LONG).show();
    				}     
    			});
    		} catch (IOException e) 
    		{
    			e.printStackTrace();
    			final String eString = e.toString();
    			Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					Toast.makeText(Client.this, eString, Toast.LENGTH_LONG).show();
    				}
    			});
    		} finally {
    			if (socket != null) {
    				try {
    					socket.close();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}

    			if (dataOutputStream != null) {
    				try {
    					dataOutputStream.close();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}

    			if (dataInputStream != null) {
    				try {
    					dataInputStream.close();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}

    			Client.this.runOnUiThread(new Runnable() {
    				@Override
    				public void run() {
    					loginPanel.setVisibility(View.VISIBLE);
    					gamePanel.setVisibility(View.GONE);
    				}
     
    			});
    		}
    	}
  
    	private void sendMsg(String msg){
    		msgToSend = msg;
    	}
  
    	private void disconnect(){
    		goOut = true;
    	}
    }
    
    // thread which deals with initial start of the game
    private class StartGame extends Thread
    {
    	String name;
    	String dstAddress;
    	int dstPort;

    	StartGame(String name, String address, int port) 
    	{
    		this.name = name;
    		dstAddress = address;
    		dstPort = port;
    	}
    	
    	public void run()
    	{	
    		gameClientThread = new gameClientThread(name, dstAddress, dstPort);
    		gameClientThread.start();
    		// wait until a message is received from the server
    		synchronized(gameClientThread)
    		{
                try
                {
                    gameClientThread.wait();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
    		}
    		msgLog1 = "";
    		
    		msgLog1 = input.get(0);
    		Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					message.setText(msgLog1);
				}
			});
    		startNewGame();
    		
    	}
    	
    	private void startNewGame()  
        {  
        	mGame = new TicTacToeGame(); 
            mGame.clearBoard(); 
            
            Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{   
		    		for (int i = 0; i < mBoardButtons.length; i++)  
		            {  
		                  mBoardButtons[i].setText("");  
		                  mBoardButtons[i].setEnabled(true);  
		                  mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));  
		            }
				}
			});
    		 
            // waiting for the message in order to start the game (the message will start with |1|)
    		while (true)
    		{
    			 if(input.isEmpty())    				 
	    			synchronized(gameClientThread)
	        		{
	                    try
	                    {
	                        gameClientThread.wait();
	                    }catch(InterruptedException e){
	                        e.printStackTrace();
	                    }
	        		}
    			 else if(!input.isEmpty() && !input.get(0).split("\\|")[0].equals("1"))
    				 input.remove(0);
    			 else
    				 break;
    		}
    		msgLog = "";
    		msgLog = input.get(0).split("\\|")[3];
    		// displaying current players and opponents name
    		Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					player.setText("player : "+gameClientThread.name +"   ");
					opponent.setText("opponent : "+msgLog);
				}
			});
    		 
    		// getting whether 'O' or 'X' for current player
    		String temp = input.get(0).split("\\|")[2];
    		msgLog1 = ""; 
    		msgLog1 = temp;
    		Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					message.setText(msgLog1);
				}
			});
    		if(Integer.parseInt(temp)== 1)
    		{
    			mGame.CURRENT_PLAYER = 'O';
    			mGame.OPPONENT_PLAYER = 'X';
    			mHumanFirst = true;
    		}
    		else 
    		{
    			mGame.CURRENT_PLAYER = 'X';
    			mGame.OPPONENT_PLAYER = 'O';
    			mHumanFirst = false;
    		}
    		/*msgLog1 = "";
    		msgLog1 = mHumanFirst+" "+temp;
    		Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					message.setText(msgLog1);
				}
			});*/
    		// starting the game
    		Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					loginPanel.setVisibility(View.GONE);
		    		gamePanel.setVisibility(View.VISIBLE);
				}
			});
    		// if current player has to play first   			
    		if (mHumanFirst)  
    	    {
    			Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.first_human);
    				}
    			});
    			mHumanFirst = false;  
    	    } 
    		// if the opponent has to play first then we need to wait for the opponent to make the move
    		else  
            {  
    			Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.turn_opponent);
    				}
    			});				
    			while (true)
    			{
    				if(!input.isEmpty() && !(input.get(0).charAt(0) == '3'))
    					 input.remove(0);
    				else if(input.isEmpty() || !(input.get(0).charAt(0) == '3'))
    				{	
	    				synchronized(gameClientThread)
	    	    		{
	    	                try
	    	                {
	    	                    gameClientThread.wait();
	    	                }catch(InterruptedException e){
	    	                    e.printStackTrace();
	    	                }
	    	    		}
    				}
    				else 
    					break;
    			}
    			msgLog1 = "";
    			msgLog1 = input.get(0).split("\\|")[1];
    			Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					int move = Integer.parseInt(msgLog1);  
    	                Client.this.setMove(Client.this.mGame.OPPONENT_PLAYER, move);
    					mInfoTextView.setText(msgLog1);
    				}
    			});
                  
                mHumanFirst = true;  
            } 
               
             mGameOver = false;  
        }
    }
    
    // This thread starts whenever a button is clicked by a player 
    private class Playing extends Thread
    {
    	private int location;
    	Playing(int location)
    	{
    		this.location = location;
    	}
    	public void run()
    	{
    		temp2 = location;
    		int winner;
    		Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					setMove(mGame.CURRENT_PLAYER, temp2);
					temp1 = mGame.checkForWinner();
				}
			});
    		try 
			{
				Thread.sleep(100);
            } catch (InterruptedException ex) { }
    		winner = temp1; 
            
            if (winner == 0)  
            {  
            	int move;
            	Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.turn_opponent);
    				}
    			});
            	while (true)
        		{
        			if(!input.isEmpty() && !(input.get(0).split("\\|")[0].charAt(0) == '3'))
        				 input.remove(0);
        			 else if(input.isEmpty()|| !(input.get(0).split("\\|")[0].charAt(0) == '3'))    				 
    	    			synchronized(gameClientThread)
    	        		{
    	                    try
    	                    {
    	                        gameClientThread.wait();
    	                    }catch(InterruptedException e){
    	                        e.printStackTrace();
    	                    }
    	        		}
        			 else if(!mBoardButtons[Integer.parseInt(input.get(0).split("\\|")[1])].isEnabled())
        			      input.remove(0);
        			 else
        				 break;
        		}
            	msgLog1 = "";
    			msgLog1 = input.get(0).split("\\|")[1];
    			Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					int move = Integer.parseInt(msgLog1);  
    	                Client.this.setMove(Client.this.mGame.OPPONENT_PLAYER, move);
    	                temp1 = mGame.checkForWinner();
    				}
    			}); 
    			try 
    			{
    				Thread.sleep(100);
                } catch (InterruptedException ex) { }
                winner = temp1;                             
            } 
                   
            if (winner == 0)  
            {
                Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.turn_human);	
    				}
    			});
            }
            else if (winner == 1)  
            {  
            	Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.result_tie);	
    				}
    			});	
            	mTieCounter++;  
            	Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mTieCount.setText(Integer.toString(mTieCounter));  
    				}
    			});
            	mGameOver = true;  
            }  
            else if (winner == 2)  
            { 
            	Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.result_human_wins); 
    				}
    			});
            	mHumanCounter++; 
            	Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mHumanCount.setText(Integer.toString(mHumanCounter));  
    				}
    			});  
        		mGameOver = true;  
            }  
            else  
            {  
            	Client.this.runOnUiThread(new Runnable() 
    			{
    				@Override
    				public void run() 
    				{
    					mInfoTextView.setText(R.string.result_opponent_wins);  
    				}
    			});
            	 mOpponentCounter++; 
            	 Client.this.runOnUiThread(new Runnable() 
     			{
     				@Override
     				public void run() 
     				{
     					mOpponentCount.setText(Integer.toString(mOpponentCounter)); 
     				}
     			});  
            	 mGameOver = true;  
            } 
            
           /* Client.this.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mInfoTextView.setText(temp1+"*"+mGame.mBoard[0]+"*"+mGame.mBoard[1]+"*"+mGame.mBoard[2]+"*"+mGame.mBoard[3]+"*"+mGame.mBoard[4]+"*"+mGame.mBoard[5]+"*"+mGame.mBoard[6]+"*"+mGame.mBoard[7]+"*"+mGame.mBoard[8]);  
				}
			});*/
    	}
    }
}



