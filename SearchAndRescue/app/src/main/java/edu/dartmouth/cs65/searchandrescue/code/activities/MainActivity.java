/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.dartmouth.cs65.searchandrescue.R;
import edu.dartmouth.cs65.searchandrescue.code.fragments.MapsFragment;
import edu.dartmouth.cs65.searchandrescue.code.fragments.ProfileFragment;
import edu.dartmouth.cs65.searchandrescue.code.structures.VPAdapter;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBotMainView;
    private ViewPager vPager;
    private MapsFragment mapFrag;
    private ProfileFragment profFragment;
    private MenuItem lastMenuItem;
    private VPAdapter vpAdapter;
    private String activeCode;


    //Fragment listener
    private BottomNavigationView.OnNavigationItemSelectedListener mMainItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    //Switches currently displayed fragment to ProfileFragment
                    vPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_map:
                    //Switches currently displayed fragment to MapsFragment
                    vPager.setCurrentItem(0);
                    return true;
            }
            return false;
        }
    };

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createActionBar();
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_maps);
        Intent codePass = getIntent();
        activeCode = codePass.getStringExtra("current code");
        //Wire views
        mBotMainView = findViewById(R.id.mainBotNavView);
        vPager = findViewById(R.id.viewpager);
        mBotMainView.setOnNavigationItemSelectedListener(mMainItemSelectedListener);

        mBotMainView.setItemIconTintList(ColorStateList.valueOf(
                Color.parseColor("#3F51B5")));

        //Boilerplate for viewPager
        vPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) { }


            //Tracks last position of menu
            @Override
            public void onPageSelected(int position) {
                if (lastMenuItem != null)
                    lastMenuItem.setChecked(false);
                else
                    mBotMainView.getMenu().getItem(0).setChecked(false);
                mBotMainView.getMenu().getItem(position).setChecked(true);
                lastMenuItem = mBotMainView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

        });
        loadViewPager(vPager);
    }


    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Search and Rescue");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    //Action bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Return to Party and clear activity stack
            case android.R.id.home:
                Intent mainIntent = new Intent(MainActivity.this, PartyActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                finish();
                return true;
            //Open edit profile activity
            case R.id.action_edit_profile:
                Intent profIntent = new Intent(MainActivity.this,
                        UpdateMissingProfile.class);
                profIntent.putExtra("current code", activeCode);
                startActivity(profIntent);
                finish();
                return true;
            //Open chat for specific code
            case R.id.action_chat:
                Intent chatIntent = new Intent(MainActivity.this,
                        ChatActivity.class);
                chatIntent.putExtra("current code", activeCode);
                startActivity(chatIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Edit profile and chat button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //Loads the fragments into the viewpager
    private void loadViewPager(ViewPager viewPager) {
        vpAdapter = new VPAdapter(getSupportFragmentManager());
        mapFrag = new MapsFragment();
        profFragment = new ProfileFragment();
        vpAdapter.addFrag(mapFrag);
        vpAdapter.addFrag(profFragment);
        viewPager.setAdapter(vpAdapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadViewPager(vPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
