package com.example.bingo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.bingo.data.LoginRepository;
import com.example.bingo.databinding.FragmentMenuBinding;
import com.example.bingo.ui.login.LoginViewModel;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MenuFragment extends Fragment {

    private static FragmentMenuBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        if(LoginRepository.isLoggedIn()) binding.buttonLogin.setText("Logout");
        CoordinatorLayout cl = view.findViewById(R.id.clm);

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!LoginRepository.isLoggedIn())
                    NavHostFragment.findNavController(MenuFragment.this)
                        .navigate(R.id.action_Menu_to_Login);
                else {
                    LoginViewModel.logout();
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            "You logged out successfully! Bye bye :(",
                            Toast.LENGTH_LONG).show();
                    binding.buttonLogin.setText("Signup/Login");
                }
            }
        });

        binding.buttonNewgame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LoginRepository.isLoggedIn())
                    NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_Menu_to_NewGame);
                else{
                    Snackbar snack = Snackbar.make(cl ,"To join a game you have to sign in!", Snackbar.LENGTH_INDEFINITE);
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

        binding.buttonStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LoginRepository.isLoggedIn())
                    NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_Menu_to_Statistics);
                else {
                    Snackbar snack = Snackbar.make(cl, "To read your statistics you have to sign in!", Snackbar.LENGTH_INDEFINITE);
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

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }

}