package com.example.bingo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bingo.data.LoginRepository;
import com.example.bingo.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        stopService(intent);
    }

    /*
    MUSIC LICENSE
    Acid Trumpet by Kevin MacLeod | https://incompetech.com/
    Music promoted by https://www.chosic.com/free-music/all/
    Creative Commons Creative Commons: By Attribution 3.0 License
    http://creativecommons.org/licenses/by/3.0/
     */

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        startService(intent);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        CoordinatorLayout cl = findViewById(R.id.cl);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(LoginRepository.isLoggedIn()){
                String username = LoginRepository.getUser().getDisplayName();
                Snackbar snack = Snackbar.make(binding.getRoot(),"You're logged in as " + username, Snackbar.LENGTH_INDEFINITE);
                snack.setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snack.dismiss();
                    }
                });
                snack.show();
            }
            else{
                Snackbar snack = Snackbar.make(binding.getRoot(), "Sign in to be able to play a game, or read statistics of your past games!",
                        Snackbar.LENGTH_INDEFINITE);
                snack.setAction("Got it", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snack.dismiss();
                    }
                });
                snack.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment f = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if(f instanceof MatchEndFragment){
            NavHostFragment.findNavController(f).navigate(R.id.action_MatchEnd_to_Menu);
            return false;
        }
        if(f instanceof WaitingRoomFragment || f instanceof BingoMatchFragment) {
            int action = (f instanceof WaitingRoomFragment)
                    ? R.id.action_WaitingRoom_to_Menu
                    : R.id.action_BingoMatch_to_Menu;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.exit_match)
                    .setMessage(R.string.exit_dialogMsg);
            builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    WaitingRoomFragment.getWebSocketClient().close();
                    NavHostFragment.findNavController(f).navigate(action);
                }
            });
            builder.setNegativeButton("Remain", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog dialog = builder.create(); dialog.show();
            return false;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}