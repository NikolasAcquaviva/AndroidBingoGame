package com.example.bingo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bingo.data.LoginRepository;
import com.example.bingo.databinding.FragmentWaitingRoomBinding;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import tech.gusavila92.websocketclient.WebSocketClient;

public class WaitingRoomFragment extends Fragment {
    private FragmentWaitingRoomBinding binding;
    private static WebSocketClient webSocketClient;
    private Integer secondsLeft;
    private MutableLiveData<Integer> secondsLeftObservable = new MutableLiveData<>();
    private MutableLiveData<Integer> joinedUsersNumber = new MutableLiveData<>();
    private ArrayList<String> joinedUsers = new ArrayList<>();
    private static String matchId;
    private Timer timer;
    private Timer timerError = new Timer();
    private Integer seconds_toStart = 5;
    private MutableLiveData<Boolean> goToMatch = new MutableLiveData<>();

    private void decreaseTimer(){ secondsLeft--; }

    public static String getMatchId(){ return matchId; }

    public static WebSocketClient getWebSocketClient(){ return webSocketClient; }

    private TextView getTextViewById(int index){
        switch(index){
            case 0:
                return binding.tv1;
            case 1:
                return binding.tv2;
            case 2:
                return binding.tv3;
            case 3:
                return binding.tv4;
            case 4:
                return binding.tv5;
            case 5:
                return binding.tv6;
            default:
                break;
        }
        return null;
    }

    public WaitingRoomFragment() {}

    private void createWebSocketClient(){
        URI uri;
        try{
            uri = new URI("ws://127.0.0.1:8080");
        }catch (URISyntaxException e){
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session started");
                webSocketClient.send("Hello World!");
            }

            @Override
            public void onTextReceived(String message) {
                Log.i("WebSocket", "Message received");
                final String msg = message;
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            if(msg.startsWith("matchUsers")){
                                String usersSequence = msg.replace("matchUsers;","");
                                joinedUsers.addAll(Arrays.asList(usersSequence.split(",")));
                                joinedUsersNumber.setValue(joinedUsers.size());
                                if(joinedUsersNumber.getValue() == 6) {
                                    webSocketClient.send("getMatchId");
                                    if(timer != null) {
                                        timer.cancel();
                                        timer.purge();
                                    }
                                }
                            }
                            else if(msg.startsWith("timeStarter")){
                                secondsLeftObservable.setValue(Integer.valueOf(msg.replace("timeStarter;","")));
                            }
                            else if(msg.startsWith("matchId")){
                                matchId = msg.replace("matchId;","");
                                timerError.cancel(); timerError.purge();
                                TextView tv = binding.secondsLeft;
                                Timer start = new Timer();
                                TimerTask isStarting = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if(seconds_toStart > 0){
                                            tv.setText(seconds_toStart.toString());
                                            seconds_toStart--;
                                        }
                                        else {
                                            start.cancel(); start.purge();
                                            goToMatch.postValue(true);
                                        }
                                    }
                                };
                                start.schedule(isStarting,0L,1000L);
                            }
                            else if(msg.equals("errorTimer")){
                                TimerTask retry = new TimerTask() {
                                    @Override
                                    public void run() {
                                        webSocketClient.send("timerStart");
                                    }
                                };
                                timerError.schedule(retry, 2000L);
                            }
                            else if(msg.equals("errorUsername")){
                                TimerTask retry = new TimerTask(){
                                    @Override
                                    public void run() {
                                        webSocketClient.send("username;" + LoginRepository.getUser().getDisplayName());
                                    }
                                };
                                timerError.schedule(retry,2000L);
                            }
                            else if(msg.startsWith("extracted") && !BingoMatchFragment.hasWon()){
                                int extraction = Integer.parseInt(msg.split("=")[1]);
                                BingoMatchFragment.showExtracted(extraction);
                                BingoMatchFragment.checkOnAllTables(extraction);
                                Timer timer = new Timer();
                                TimerTask nextExtraction = new TimerTask() {
                                    @Override
                                    public void run() {
                                        webSocketClient.send("inGame;matchId="+matchId+";turn="+BingoMatchFragment.getTurn());
                                        BingoMatchFragment.nextTurn();
                                    }
                                };
                                timer.schedule(nextExtraction,2500L);
                            }
                            else if(msg.startsWith("statement")){
                                String state = msg.split(";")[1];
                                String score = msg.split(" ")[2];
                                String user = state.split(" ")[0];
                                Timer scoreText = new Timer();
                                Timer win = new Timer();

                                TimerTask set = new TimerTask() {
                                    @Override
                                    public void run() {
                                        BingoMatchFragment.setScoreText(state);
                                        BingoMatchFragment.setNextGoal();
                                        scoreText.cancel(); scoreText.purge();
                                    }
                                };
                                if(score.equals("Bingo")){
                                    TimerTask winner = new TimerTask() {
                                        @Override
                                        public void run() {
                                            BingoMatchFragment.setScoreText(user + " is the winner!");
                                            BingoMatchFragment.setWinner(user);

                                        }
                                    };
                                    TimerTask matchEnd = new TimerTask() {
                                        @Override
                                        public void run() {
                                            win.cancel(); win.purge();
                                            Handler handler = new Handler(Looper.getMainLooper());
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    Fragment navHostFragment = getActivity().getSupportFragmentManager().getPrimaryNavigationFragment();
                                                    Fragment f = navHostFragment.getChildFragmentManager().getFragments().get(0);
                                                    if(f instanceof BingoMatchFragment)
                                                        NavHostFragment.findNavController(f).navigate(R.id.action_BingoMatch_to_MatchEnd);
                                                }
                                            };
                                            handler.post(runnable);
                                        }
                                    };
                                    win.schedule(winner,2500);
                                    win.schedule(matchEnd,4000);
                                }
                                scoreText.schedule(set,500L);
                            }
                            else if(msg.startsWith("matchEndData")){
                                Log.i("MATCH END", msg);
                                MatchEndFragment.setReceivedData(msg);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {

            }

            @Override
            public void onPingReceived(byte[] data) {

            }

            @Override
            public void onPongReceived(byte[] data) {

            }

            @Override
            public void onException(Exception e) {
                Log.e("exception", e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed");
                webSocketClient.close();
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goToMatch.setValue(false);
        createWebSocketClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        webSocketClient.send("timerStart");
        webSocketClient.send("username;" + LoginRepository.getUser().getDisplayName());
        binding = FragmentWaitingRoomBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tv = binding.secondsLeft;
        secondsLeftObservable.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                secondsLeft = integer;
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    public void run() {
                        decreaseTimer();
                        if(secondsLeft > 0)
                            tv.setText(secondsLeft.toString());
                        else {
                            timer.cancel(); timer.purge();
                            webSocketClient.send("getMatchId");
                        }
                    }
                };
                timer.schedule(task,0L,1000L);
            }
        });

        joinedUsersNumber.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                for(int i = 0; i < integer; i++){
                    TextView tvx = getTextViewById(i);
                    tvx.setText(joinedUsers.get(i));
                }
            }
        });

        goToMatch.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean)
                    NavHostFragment.findNavController(WaitingRoomFragment.this)
                        .navigate(R.id.action_WaitingRoom_to_BingoMatch);
            }
        });
    }
}