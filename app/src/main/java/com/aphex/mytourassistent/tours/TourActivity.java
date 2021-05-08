package com.aphex.mytourassistent.tours;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.activetour.ActiveTourActivity;
import com.aphex.mytourassistent.databinding.ActivityTourBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class TourActivity extends AppCompatActivity {

    private ActivityTourBinding binding;
    private int backButtonCount;
    private ToursViewModel toursViewModel;



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
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(binding.tourNavHostFragment.getId());
        NavController navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        // Kopler til nav drawer:
        // NB! Kopler til Toolbar (søreger for hamburger-menyknapp og tilbakeknapper):
        NavigationUI.setupWithNavController(binding.myToolbar, navController, appBarConfiguration);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getBaseContext(), ActiveTourActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }

                }
            });
            case R.id.newTour:
                Navigation.findNavController(binding.tourNavHostFragment).navigate(R.id.addTourFragment);
                /*NavController navController = Navigation.findNavController(this, binding.tourNavHostFragment.getId());
                return NavigationUI.onNavDestinationSelected(item, navController)
                        || super.onOptionsItemSelected(item);*/
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed()
    {
        if(Navigation.findNavController(binding.tourNavHostFragment).popBackStack()) {
        } else {
            if(backButtonCount >= 1)
            {
                finishAffinity();
                System.exit(0);
            }
            else
            {
                Toast.makeText(this, "Press bak knappen igjen for å avslutte applikasjonen", Toast.LENGTH_SHORT).show();
                backButtonCount++;
            }
        }
    }
}