package com.example.clientother12;
  
public class TicTacToeGame 
{         
     private char mBoard[];  
     private final static int BOARD_SIZE = 9;  
       
     // initializing to the default values
     public static char CURRENT_PLAYER = 'X';  
     public static char OPPONENT_PLAYER = 'O';  
     public static final char EMPTY_SPACE = ' ';   
       
     public static int getBOARD_SIZE() 
     {  
          return BOARD_SIZE;  
     }  
       
     public TicTacToeGame()
     {              
          mBoard = new char[BOARD_SIZE];  
            
          for (int i = 0; i < BOARD_SIZE; i++)  
               mBoard[i] = EMPTY_SPACE;    
     }  
       
     public void clearBoard()  
     {  
          for (int i = 0; i < BOARD_SIZE; i++)  
          {  
               mBoard[i] = EMPTY_SPACE;  
          }  
     }  
       
     public void setMove(char player, int location)  
     {  
          mBoard[location] = player;  
     }  
     
     // 2 - if current player wins, 3 - if opponent wins, 1 - if there is a tie, 0 - if the game is to be continued
     public int checkForWinner()  
     {  
          for (int i = 0; i <= 6; i += 3)  
          {  
               if (mBoard[i] == CURRENT_PLAYER &&  
                 mBoard[i+1] == CURRENT_PLAYER &&  
                 mBoard[i+2] == CURRENT_PLAYER)  
                    return 2;  
               if (mBoard[i] == OPPONENT_PLAYER &&  
                    mBoard[i+1] == OPPONENT_PLAYER &&  
                    mBoard[i+2] == OPPONENT_PLAYER)  
                    return 3;  
          }  
            
          for (int i = 0; i <= 2; i++)  
          {  
               if (mBoard[i] == CURRENT_PLAYER &&  
                    mBoard[i+3] == CURRENT_PLAYER &&  
                    mBoard[i+6] == CURRENT_PLAYER)  
                    return 2;  
               if (mBoard[i] == OPPONENT_PLAYER &&  
                    mBoard[i+3] == OPPONENT_PLAYER &&  
                    mBoard[i+6] == OPPONENT_PLAYER)  
                    return 3;  
          }  
            
          if ((mBoard[0] == CURRENT_PLAYER &&  
                mBoard[4] == CURRENT_PLAYER &&  
                mBoard[8] == CURRENT_PLAYER) ||  
                mBoard[2] == CURRENT_PLAYER &&  
                mBoard[4] == CURRENT_PLAYER &&  
                mBoard[6] == CURRENT_PLAYER)  
                return 2;  
          if ((mBoard[0] == OPPONENT_PLAYER &&  
                mBoard[4] == OPPONENT_PLAYER &&  
                mBoard[8] == OPPONENT_PLAYER) ||  
                mBoard[2] == OPPONENT_PLAYER &&  
                mBoard[4] == OPPONENT_PLAYER &&  
                mBoard[6] == OPPONENT_PLAYER)  
                return 3;  
            
          // 1 if there is a tie
          for (int i = 0; i < getBOARD_SIZE(); i++)  
          {  
               if (mBoard[i] != CURRENT_PLAYER && mBoard[i] != OPPONENT_PLAYER)  
                    return 0;  
          }  
            
          return 1;  
     }  
} 
