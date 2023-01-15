package com.example.bingo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.example.bingo.databinding.FragmentStatisticsBinding;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private StatisticsRepository statRepository;

    public StatisticsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater);
        statRepository = LoginDataSource.getStatRepository();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String myUsername = LoginRepository.getUser().getDisplayName();
        binding.statTitle.setText("Statistics of user " + myUsername);
        Statistics statistics = statRepository.findByUsername(myUsername);
        int[] statFields = {statistics.matchesPlayed, statistics.matchesWon,
                statistics.ambos, statistics.ternos, statistics.quaternos, statistics.cinquinos, statistics.matchesWon};
        TableLayout table = binding.statTable;
        for(int i = 0; i < table.getChildCount(); i++){
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            TableRow tableRow = (TableRow) table.getChildAt(i);
            TextView tv = new TextView(view.getContext());
            tv.setLayoutParams(params);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(24);
            tv.setPadding(24,24,24,24);
            tv.setBackground(BingoApp.getContext().getResources().getDrawable(R.drawable.card_cell, null));
            tv.setText(Integer.toString(statFields[i]));
            tableRow.addView(tv);
        }
    }
}