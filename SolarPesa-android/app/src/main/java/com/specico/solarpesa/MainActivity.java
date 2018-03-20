package com.specico.solarpesa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.specico.solarpesa.Utils.Constants;
import com.specico.solarpesa.Utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        View header=navigationView.getHeaderView(0);

        String user_name = SharedPrefUtil.getStringPreference(MainActivity.this, Constants.USER_NAME);
        TextView tvName = header.findViewById(R.id.nameTextView);
        tvName.setText(user_name);

        String user_image = SharedPrefUtil.getStringPreference(MainActivity.this, Constants.USER_PHOTO);
        if("" != user_image) {
            Uri photoUri = Uri.parse(user_image);
            ImageView imgUser = header.findViewById(R.id.imgProfile);
            Glide.with(MainActivity.this)
                    .load(photoUri)
                    .centerCrop()
                    .into(imgUser);
        }

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);


        fragmentList = new ArrayList<>();

        GenerationFragment fragment1 = new GenerationFragment();
        Bundle args1 = new Bundle();
        args1.putString("title", "Generation");
        fragment1.setArguments(args1);

        ConsumptionFragment fragment2 = new ConsumptionFragment();
        Bundle args2 = new Bundle();
        args2.putString("title", "Tutorial2");
        fragment2.setArguments(args2);

//        ConsumptionFragment fragment3 = new ConsumptionFragment();
//        Bundle args3 = new Bundle();
//        args3.putString("title", "Tutorial3");
//        fragment3.setArguments(args3);

        fragmentList.add(fragment1);
        fragmentList.add(fragment2);
//        fragmentList.add(fragment3);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("TAB",""+tab.getPosition());
                if(tab.getPosition()==3){

                }

                //startActivity(new Intent(SplashActivity.this,MainActivity.class));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList;
        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {

            //return PlaceholderFragment.newInstance(position+1);
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {

            return fragmentList.size();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPrefUtil.clearPreferences(MainActivity.this);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finishAffinity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
