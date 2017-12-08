package com.pacho.geopost.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.pacho.geopost.fragments.AddFriendFragment;
import com.pacho.geopost.fragments.DashboardFragment;
import com.pacho.geopost.fragments.MapFragment;
import com.pacho.geopost.R;
import com.pacho.geopost.fragments.ProfileFragment;
import com.pacho.geopost.fragments.UpdateStateFragment;
import com.pacho.geopost.utilities.BottomNavigationViewHelper;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    MapFragment mapFragmentInstance = new MapFragment();
                    mapFragmentInstance.setRetainInstance(true);
                    transaction.replace(R.id.content, mapFragmentInstance).commit();
                    return true;
                case R.id.navigation_update_state:
                    transaction.replace(R.id.content, new UpdateStateFragment()).commit();
                    return true;
                case R.id.navigation_add_friend:
                    transaction.replace(R.id.content, new AddFriendFragment()).commit();
                    return true;
                case R.id.navigation_profile:
                    transaction.replace(R.id.content, new ProfileFragment()).commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.content, new MapFragment()).commit();
    }

}
