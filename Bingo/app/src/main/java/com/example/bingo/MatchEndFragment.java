package com.example.bingo;

import android.app.Application;
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

import com.example.bingo.data.LoginDataSource;
import com.example.bingo.data.LoginRepository;
import com.example.bingo.data.StatisticsRepository;
import com.example.bingo.data.model.Statistics;
import com.example.bingo.databinding.FragmentMatchEndBinding;

import java.util.ArrayList;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MatchEndFragment extends Fragment {
    private static FragmentMatchEndBinding binding;
    private static WebSocketClient socketInstance;
    private static String receivedData;
    private static StatisticsRepository statRepository = LoginDataSource.getStatRepository();

    public static void setReceivedData(String data) {
        receivedData = data;
        TableLayout table = binding.usersScore;
        String dataset = receivedData.split(";")[1];
        String[] sets = dataset.split(":");
        ArrayList<String> users = new ArrayList<>();
        ArrayList<String> scores = new ArrayList<>();
        for(int i = 0; i < sets.length; i++){
            users.add(sets[i].split("=")[0]);
            if(sets[i].split("=").length > 1) {
                String formatting = sets[i].split("=")[1].replace(",", " -");
                scores.add(formatting);
            }
            else scores.add("");
        }
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT);

        for(int i = 0; i < users.size(); i++){
            TableRow row = new TableRow(binding.getRoot().getContext());
            row.setLayoutParams(tableParams);
            TextView tv1 = new TextView(binding.getRoot().getContext());
            TextView tv2 = new TextView(binding.getRoot().getContext());
            tv1.setLayoutParams(rowParams);
            tv2.setLayoutParams(rowParams);
            tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv1.setText(users.get(i));
            tv2.setText(scores.get(i));
            tv1.setBackground(BingoApp.getContext().getResources().getDrawable(R.drawable.card_cell,null));
            tv2.setBackground(BingoApp.getContext().getResources().getDrawable(R.drawable.card_cell,null));
            tv1.setTextSize(14);
            tv2.setTextSize(14);
            tv1.setPadding(8,8,8,8);
            tv2.setPadding(8,8,8,8);

            row.addView(tv1);
            row.addView(tv2);
            table.addView(row);
        }

        String myUser = LoginRepository.getUser().getDisplayName();
        Statistics myStat = statRepository.findByUsername(myUser);
        int index = users.indexOf(myUser);
        String myUser_scores = scores.get(index);
        myStat.matchesPlayed++;
        if(BingoMatchFragment.getWinner().equals(myUser)) myStat.matchesWon++;
        if(myUser_scores.contains("Ambo")) myStat.ambos++;
        if(myUser_scores.contains("Terno")) myStat.ternos++;
        if(myUser_scores.contains("Quaterno")) myStat.quaternos++;
        if(myUser_scores.contains("Cinquino")) myStat.cinquinos++;
        statRepository.update(myStat);
    }

    public MatchEndFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socketInstance = WaitingRoomFragment.getWebSocketClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatchEndBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socketInstance.send("endMatchData;matchId=" + WaitingRoomFragment.getMatchId());
        binding.winner.setText("The Winner is " + BingoMatchFragment.getWinner());
        binding.backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketInstance.send("totalEnd;matchId="+WaitingRoomFragment.getMatchId());
                NavHostFragment.findNavController(MatchEndFragment.this)
                        .navigate(R.id.action_MatchEnd_to_Menu);
            }
        });
    }
}