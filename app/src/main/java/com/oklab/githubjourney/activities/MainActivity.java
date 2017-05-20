package com.oklab.githubjourney.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.oklab.githubjourney.R;
import com.oklab.githubjourney.adapters.FirebaseAnalyticsWrapper;
import com.oklab.githubjourney.asynctasks.DeleteUserAuthorizationAsyncTask;
import com.oklab.githubjourney.data.UpdaterService;
import com.oklab.githubjourney.fragments.ContributionsByDateListFragment;
import com.oklab.githubjourney.fragments.MainViewFragment;
import com.oklab.githubjourney.services.TakeScreenshotService;
import com.oklab.githubjourney.utils.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ContributionsByDateListFragment.OnListFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences prefs;
    private ViewPager calendarYearviewPager;
    private CalendarYearPagerAdapter calendarYearPagerAdapter;
    private TakeScreenshotService takeScreenshotService;
    private FirebaseAnalyticsWrapper firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        firebaseAnalytics = new FirebaseAnalyticsWrapper(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        calendarYearPagerAdapter = new CalendarYearPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        calendarYearviewPager = (ViewPager) findViewById(R.id.pager);
        calendarYearviewPager.setAdapter(calendarYearPagerAdapter);
        ContributionsByDateListFragment contributionsActivityFragment = ContributionsByDateListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.contrib_fragment, contributionsActivityFragment).commit();
        prefs = this.getSharedPreferences(Utils.SHARED_PREF_NAME, 0);
        String currentSessionData = prefs.getString("userSessionData", null);
        if (currentSessionData == null) {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        this.startService(new Intent(this, UpdaterService.class));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        takeScreenshotService = new TakeScreenshotService(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeScreenshotService.takeScreenShot();
            }
        });
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
    protected void onStart() {
        super.onStart();
        firebaseAnalytics.setCurrentScreen(this, "MainActivityFirebaseAnalytics");
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(item.getItemId()));
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "MainActivityNavigationItemSelected");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        if (id == R.id.github_events) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.profile) {
            String currentSessionDataGeneral = prefs.getString("userSessionData", null);
            if (currentSessionDataGeneral != null) {
                Intent intent = new Intent(this, GeneralActivity.class);
                startActivity(intent);
                return true;
            }
        } else if (id == R.id.personal) {
            String currentSessionDataPersonal = prefs.getString("userSessionData", null);
            if (currentSessionDataPersonal != null) {
                Intent intent = new Intent(this, PersonalActivity.class);
                startActivity(intent);
                return true;
            }
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.sing_out) {
            new DeleteUserAuthorizationAsyncTask(this).execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class CalendarYearPagerAdapter extends FragmentPagerAdapter {

        public CalendarYearPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            Log.v(TAG, "position - " + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return MainViewFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 12;
        }
    }
}
