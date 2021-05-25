package com.aphex.mytourassistent.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.viewmodels.ToursViewModel;
import com.aphex.mytourassistent.databinding.ActivityTourBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class TourActivity extends AppCompatActivity {

    private ActivityTourBinding binding;
    private int backButtonCount;
    private ToursViewModel toursViewModel;
    private NavController navController;
    private NavHostFragment navHostFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityTourBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);


        toursViewModel = new ViewModelProvider(this).get(ToursViewModel.class);


        // Henter referanse til NavController-objektet.
        // Gjøres ulikt avhengig av om man bruker <fragment../> eller <FragmentContainerView.../>
        // i activity_main.xml. Her brukes vi FragmentContainerView i activity_main.xml
        // og finner derfor objektet slik:
         navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(binding.tourNavHostFragment.getId());
        navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        // Kopler til nav drawer:
        // NB! Kopler til Toolbar (sørger for tilbakeknapper):
        NavigationUI.setupWithNavController(binding.myToolbar, navController, appBarConfiguration);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { ;

        switch (item.getItemId()) {
            case R.id.logout:
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            });
                break;
            case R.id.settings:
                Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
                return super.onOptionsItemSelected(item);
            default:
                Log.d("DebugNav", "On menu selected:"+navController.getCurrentDestination().getLabel());
                return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (navHostFragment.getChildFragmentManager().getBackStackEntryCount() == 0) {
            Log.d("DebugNav", "onBackPressed:stack is 0 ");
            if (backButtonCount >= 1) {
                finishAffinity();
                System.exit(0);
            } else {
                Toast.makeText(this, getString(R.string.toast_press_back_again_to_finish), Toast.LENGTH_SHORT).show();
                backButtonCount++;
            }
        } else {
            navController.popBackStack();
            backButtonCount = 0;
        }
    }
}