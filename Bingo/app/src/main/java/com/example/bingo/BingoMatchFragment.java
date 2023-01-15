package com.example.bingo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.bingo.databinding.FragmentBingoMatchBinding;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import tech.gusavila92.websocketclient.WebSocketClient;

public class BingoMatchFragment extends Fragment {
    private static FragmentBingoMatchBinding binding;
    private static WebSocketClient socketInstance;
    private static boolean won;
    private int numCards;
    private Integer[][] board = new Integer[9][10];
    private ArrayList<Integer[][]> myCards = new ArrayList<>();
    private static ArrayList<TableLayout> myTables;
    private static String matchId;
    private static int turn;
    private static int nextGoal; //number to align for the next score. Ambo is the first.
    private static int index;
    private final static int[] goals = {2,3,4,5,15};
    private final static String[] scores = {"Ambo", "Terno", "Quaterno", "Cinquino", "Bingo"};
    private static Hashtable<TableLayout, Boolean[][]> checkedCells;
    private static String winner;

    public static void setWinner(String name){ winner = name;}

    public static String getWinner(){ return winner; }

    public BingoMatchFragment() {

    }

    public static boolean hasWon(){ return won; }

    public static void setNextGoal(){
        if(index < 5) nextGoal = goals[index++];
    }

    public static void setScoreText(String msg){
        binding.extractedText.setText(msg);
    }

    private static void checkGoal(TableLayout table){
        Boolean[][] checked = checkedCells.get(table);

        if(nextGoal == 15){
            int count = 0;
            for(int i = 0; i < 3; i++){
                for(int j = 0; j < 9; j++){
                    if(checked[i][j]) count++;
                }
            }
            if(count == nextGoal) {
                socketInstance.send("score=" + scores[index - 1] + ";matchId=" + matchId);
                won = true;
            }

        }
        else {
            for (int i = 0; i < 3; i++) {
                int count = 0;
                for (int j = 0; j < 9; j++) {
                    if (checked[i][j]) count++;
                }
                if (count == nextGoal) {
                    socketInstance.send("score=" + scores[index - 1] + ";matchId="+matchId);
                }
            }
        }
    }

    private void generateBoard(){
        for(int i = 1; i < 10; i++){
            for(int j = 1; j <= 10; j++){
                board[i-1][j-1] = (i-1)*10 + j;
            }
        }
    }

    public static void nextTurn(){ turn++; }

    public static String getTurn(){ return Integer.toString(turn); }

    public static void addTable(TableLayout table){ myTables.add(table); }

    public static void showExtracted(int extraction){
        binding.extractedText.setText("Extracted Number");
        binding.extractedNum.setText(Integer.toString(extraction));
    }

    public static void checkOnAllTables(int n){
        for(int i = 0; i < myTables.size(); i++){
            checkNumberOnTable(myTables.get(i), n);
        }
    }

    public static void checkNumberOnTable(TableLayout table, int n){
        for(int i = 0; i < table.getChildCount(); i++){
            TableRow r = (TableRow) table.getChildAt(i);
            for(int j = 0; j < r.getChildCount(); j++){
                TextView tv = (TextView) r.getChildAt(j);
                String arg = tv.getText().toString();
                if(!arg.isEmpty() && Integer.parseInt(arg) == n) {
                    tv.setBackground(BingoApp.getContext().getResources().getDrawable(R.drawable.card_cell_ticked, null));
                    if(table.getChildCount() == 3) {
                        Boolean[][] oldChecked = checkedCells.get(table);
                        oldChecked[i][j] = true;
                        checkedCells.replace(table, oldChecked);
                        checkGoal(table);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socketInstance = WaitingRoomFragment.getWebSocketClient();
        numCards = CardFragment.getNumCards();
        matchId = WaitingRoomFragment.getMatchId();
        turn = 0;
        index = 0;
        won = false;
        myTables = new ArrayList<>();
        checkedCells = new Hashtable<>();
        setNextGoal();
        for(int i = 0; i < numCards; i++) myCards.add(CardFragment.getCard());
        generateBoard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBingoMatchBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CardFragment.generateMatrix((ConstraintLayout) view, board, getContext(), 9,10, 25, 490, true);

        for(int i = 0; i < myCards.size(); i++) {
            TableLayout card = CardFragment.generateMatrix(view.findViewById(R.id.myCards_view), myCards.get(i), getContext(),
                    3, 9, 85, i * 300 + 70 * (i + 1), true);
            Boolean[][] checked = new Boolean[3][9];
            for(int k = 0; k < 3; k++){
                for(int j = 0; j < 9; j++){
                    checked[k][j] = false;
                }
            }
            checkedCells.put(card, checked);
        }

        Timer timer = new Timer();
        TimerTask startMatch = new TimerTask() {
            @Override
            public void run() {
                socketInstance.send("inGame;matchId="+matchId+";turn="+getTurn());
                nextTurn();
            }
        };
        timer.schedule(startMatch,2000L);
    }
}