package com.example.bingoserver;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.ui.AppBarConfiguration;
import com.example.bingoserver.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {
    private static ActivityMainBinding binding;
    private WSocServer server;
    private int port = 8080;
    private boolean isOnline = false;
    private static ArrayList<String> logs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.openConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOnline) {
                    isOnline = true;
                    server = new WSocServer(port);
                    server.start();
                    Toast toast = Toast.makeText(view.getContext(),"Server is on", Toast.LENGTH_LONG);
                    toast.show();
                    Log.i("WebSocket Server", "Started on port " + server.getPort());
                }
                else{
                    Snackbar snack = Snackbar.make(view ,"We are already online!", Snackbar.LENGTH_INDEFINITE);
                    snack.setAction("Got it", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snack.dismiss();
                        }
                    });
                    snack.show();
                }
            }
        });

        binding.closeConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline) {
                    isOnline = false;
                    logs.clear();
                    Toast toast = Toast.makeText(view.getContext(),"Server is off", Toast.LENGTH_LONG);
                    toast.show();
                    try {
                        server.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Snackbar snack = Snackbar.make(view ,"We are already offline!", Snackbar.LENGTH_INDEFINITE);
                    snack.setAction("Got it", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snack.dismiss();
                        }
                    });
                    snack.show();
                }
            }
        });
    }

    private static void clearView(){
        ConstraintLayout cl = binding.logsView;
        for(int i = 0; i < cl.getChildCount(); i++){
            TextView tv = (TextView) cl.getChildAt(i); tv.setText("");
        }
    }

    private static void addOnView(){
        ConstraintLayout cl = binding.logsView;
        Handler h = new Handler(Looper.getMainLooper());

        final TextView tv = new TextView(binding.getRoot().getContext());
        tv.setTextSize(16);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(40,180*(logs.size()+1),40,0);
        tv.setText(logs.get(logs.size()-1));
        Runnable r = new Runnable() {
            @Override
            public void run() {
                cl.addView(tv);

            }
        };
        h.post(r);
        h = null;
        r = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class WSocServer extends WebSocketServer {
        private List<String> matchUsers = new ArrayList<>();
        private Integer timerSeconds;
        private UUID matchId;

        //a key represents a match to which an array of extracted numbers is associated
        private Hashtable<String,Integer[]> matchExtractedNumbers = new Hashtable<>();
        private Hashtable<String, Collection<WebSocket>> matchClients = new Hashtable<>();
        private Hashtable<String,Hashtable<String,ArrayList<String>>> users_scorePerMatch = new Hashtable<>();
        private Hashtable<String,WebSocket> clientConnection = new Hashtable<>();

        private void initTimer(){
            timerSeconds = 60;
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                public void run() {
                    if(timerSeconds > 0) timerSeconds--;
                    else {
                        timerSeconds = 60;
                    }
                }
            };
            timer.schedule(task,0L,1000L);
        }

        private String UsersListToString(List list){
            return list.toString().replace("[","").replace("]","");
        }

        private Integer[] generateExtractedNumbers(){
            Integer[] callerBoard = new Integer[90];
            List<Integer> boardPool = new ArrayList<>();
            boardPool.addAll(Arrays.asList(IntStream.rangeClosed(1,90).boxed().toArray(Integer[]::new)));
            for(int i = 0; i < 90; i++){
                int rng = ThreadLocalRandom.current().nextInt(0,90-i);
                callerBoard[i] = boardPool.remove(rng);
            }
            return callerBoard;
        }

        private void initMatch(){
            matchId = UUID.randomUUID();
            Integer[] matchBoard = generateExtractedNumbers();
            matchExtractedNumbers.put(matchId.toString(),matchBoard);
            matchClients.put(matchId.toString(),clientConnection.values());
            Hashtable<String,ArrayList<String>> matchData = new Hashtable<>();
            for(String user: matchUsers) matchData.put(user,new ArrayList<>());
            users_scorePerMatch.put(matchId.toString(), matchData);
        }

        private Integer getExtractedNumber(String match, Integer turn){
            if(turn >= 90) return -1;
            Integer[] thisMatchExtractedNumbers = matchExtractedNumbers.get(match);
            Integer returning = thisMatchExtractedNumbers[turn];
            thisMatchExtractedNumbers = null;
            return returning;
        }

        

        public WSocServer(int port){
            super(new InetSocketAddress(port));
        }

        public WSocServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("WebSocket(open)", conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
                    logs.add(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
                    MainActivity.addOnView();
                    matchUsers.addAll(Arrays.asList("user1","user2","user3","user4","user5"));
                }
            });
            thread.start();
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("WebSocket(close)", conn + " has left the room! Reason: " + reason);
                    logs.add(conn + " has left the room!");
                    MainActivity.addOnView();
                }
            });
            thread.start();
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!message.startsWith("inGame"))
                        logs.add(message + " from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
                    Log.i("WebSocket(message)", conn + ": " + message);
                    MainActivity.addOnView();

                    if(message.startsWith("username")){
                        if(matchUsers.size() < 6){
                            String user = message.replace("username;","");
                            if(!matchUsers.contains(user)) {
                                matchUsers.add(user);
                                clientConnection.put(user,conn);
                            }
                            String sending = "matchUsers;" + UsersListToString(matchUsers);
                            conn.send(sending);
                        }
                        else conn.send("errorUsername");
                    }
                    else if(message.equals("timerStart")){
                        initTimer();
                        if(matchUsers.size() < 6){
                            String sending = "timeStarter;" + timerSeconds.toString();
                            conn.send(sending);
                        }
                        else conn.send("errorTimer");
                    }
                    else if(message.equals("getMatchId")){
                        if(!matchUsers.isEmpty()){
                            initMatch();
                            matchUsers.clear();
                        }
                        String sending = "matchId;" + matchId.toString();
                        conn.send(sending);
                    }
                    else if(message.startsWith("inGame")){
                        String[] fields = message.split(";");
                        String matchId = fields[1].split("=")[1];
                        int turn = Integer.parseInt(fields[2].split("=")[1]);
                        Integer extraction = getExtractedNumber(matchId,turn);
                        fields = null;
                        conn.send("extracted=" + extraction.toString());
                    }
                    else if(message.startsWith("score")){
                        String matchId = message.split(";")[1].split("=")[1];
                        String score = message.split(";")[0].split("=")[1];
                        WebSocket[] clients = matchClients.get(matchId).toArray(new WebSocket[0]);
                        String user = "";
                        Enumeration<String> keys = clientConnection.keys();
                        String key = keys.nextElement();
                        while(!key.isEmpty()){
                            if(clientConnection.get(key) == conn) {
                                user = key;
                                break;
                            }
                            key = keys.nextElement();
                        }

                        keys = null;
                        Hashtable<String,ArrayList<String>> tmp = users_scorePerMatch.get(matchId);
                        ArrayList<String> tmp_list = tmp.get(user);
                        tmp_list.add(score);
                        tmp.replace(user,tmp_list);
                        users_scorePerMatch.replace(matchId,tmp);

                        for(int i = 0; i < clients.length; i++){
                            clients[i].send("statement;" + user + " got " + score + " with");
                        }
                        clients = null;
                    }
                    else if(message.startsWith("endMatchData")){
                        String matchId = message.split(";")[1].split("=")[1];
                        Hashtable<String,ArrayList<String>> users_ofMatch = users_scorePerMatch.get(matchId);
                        ArrayList<String> users = new ArrayList<>();
                        Enumeration<String> e = users_ofMatch.keys();

                        while(e.hasMoreElements()){
                            Log.e("endmatchdata","a");
                            users.add(e.nextElement());
                        }
                        e = null;
                        String sending = "matchEndData;";
                        for(String user: users) sending += user + "=" + UsersListToString(users_ofMatch.get(user)) + ":";
                        users_ofMatch = null;
                        conn.send(sending);
                    }
                    else if(message.startsWith("totalEnd")){
                        clearView();
                        logs.clear();
                        String matchId = message.split(";")[1].split("=")[1];
                        if(matchClients.get(matchId)!=null) {
                            WebSocket[] clients = matchClients.get(matchId).toArray(new WebSocket[0]);
                            for (WebSocket client : clients) client.close();
                            Enumeration<String> e = clientConnection.keys();
                            boolean exit = false;
                            while (e.hasMoreElements() && !exit) {
                                Log.e("totalend", "while");
                                for (WebSocket client : clients) {
                                    Log.e("totalend", "for");
                                    String tmp = e.nextElement();
                                    if (clientConnection.get(tmp) == client) {
                                        clientConnection.remove(tmp);
                                        exit = true;
                                        break;
                                    }
                                }
                            }
                            e = null; clients = null;
                            matchClients.remove(matchId);
                            users_scorePerMatch.remove(matchId);
                            matchExtractedNumbers.remove(matchId);
                        }
                    }
                }
            });

            thread.start();
        }
        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("WebSocket(message)", conn + ": " + message );

                }
            });
            thread.start();
        }

        public static void main(String[] args){
        }
        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("WebSocket", "Server started!");
                }
            });
            thread.start();
        }
    }
}