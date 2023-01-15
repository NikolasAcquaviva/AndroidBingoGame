package com.example.bingo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.bingo.databinding.FragmentCardBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CardFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private FragmentCardBinding binding;
    private static int numCardsPlaying;

    public static int getNumCards(){ return numCardsPlaying; }

    public static Integer[][] getCard(){
        Integer[][] card = new Integer[3][9];
        ArrayList<Integer> inserted = new ArrayList<>();
        ArrayList<Integer> excluded = new ArrayList<>();
        List<Integer> toExclude = Arrays.asList(1,2,3,4,5,6,7,8,9);
        for(int i = 0; i < 3; i++){
            ArrayList<Integer> excludes = new ArrayList<>();
            for(int k = 0; k < 4; k++){
                Integer rng;
                do {
                    rng = ThreadLocalRandom.current().nextInt(1, 10);
                }while(excludes.contains(rng) || (!excluded.containsAll(toExclude) && excluded.contains(rng)));
                excludes.add(rng); excluded.add(rng);
            }

            for(int j = 1; j < 10; j++){
                if(excludes.contains(j)) card[i][j-1] = -1;
                else {
                    if(j == 1) {
                        Integer rng;
                        do{
                            rng = ThreadLocalRandom.current().nextInt(1, 11);
                        } while(inserted.contains(rng));
                        card[i][j - 1] = rng; inserted.add(rng);
                    }
                    else {
                        Integer rng;
                        do{
                           rng = ThreadLocalRandom.current().nextInt((j - 1) * 10 + 1, j * 10 + 1);
                        } while (inserted.contains(rng));
                        card[i][j - 1] = rng; inserted.add(rng);
                    }
                }
            }
        }
        return card;
    }

    public CardFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCardBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        generateMatrix((ConstraintLayout) view,getCard(),view.getContext(), 3, 9, 85, 120, false);
        Spinner spinner = binding.cardChoice;
        spinner.setOnItemSelectedListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.numCards_choice, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        binding.buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CardFragment.this).navigate(R.id.action_Card_to_WaitingRoom);
            }
        });
    }

    public static TableLayout generateMatrix(ConstraintLayout view, Integer[][] matrix, Context context,
                                      int rows, int cols, int leftMargin, int topMargin, boolean addTable){
           TableLayout tl = new TableLayout(context);
           TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                   TableRow.LayoutParams.MATCH_PARENT,
                   TableRow.LayoutParams.WRAP_CONTENT);

           for(int i = 0; i < rows; i++){
               TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                       TableLayout.LayoutParams.MATCH_PARENT,
                       TableLayout.LayoutParams.WRAP_CONTENT);

               TableRow tr = new TableRow(context);
               if(i==0) tableParams.setMargins(leftMargin,topMargin,0,0);
               else tableParams.setMargins(leftMargin,0,0,0);
               tr.setLayoutParams(tableParams);
               for(int j = 0; j < cols; j++){
                   String toAppend = (matrix[i][j] == -1) ? "" : matrix[i][j].toString();
                   TextView tv = new TextView(context);
                   tv.setLayoutParams(rowParams);
                   tv.setTextSize(18);
                   tv.setPadding(20,0,20,0);
                   tv.setBackground(BingoApp.getContext().getResources().getDrawable(R.drawable.card_cell, null));
                   tv.append(toAppend);
                   tr.addView(tv);
               }
               tl.addView(tr);
           }
           view.addView(tl);
           if(addTable) BingoMatchFragment.addTable(tl);
           return tl;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        numCardsPlaying = i+1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}