package admu.thesis.secureauthenticator;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Menu menu;

    public static final String disablePassword = "Disable Password";
    public static final String enablePassword = "Enable Password";

    OTPItem otpItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        otpItem = (OTPItem) getApplicationContext();

        //Force change the upper title
        this.setTitle(getString(R.string.app_name));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    public void updateMenu() {
        System.out.println("Chaning Menu Item");
        MenuItem toggleMenuItem = menu.findItem(R.id.action_toggle);
        if (otpItem.getPasswordEnabled()) {
            toggleMenuItem.setTitle("Disable Password");
        } else {
            toggleMenuItem.setTitle("Enable Password");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        updateMenu();
        return true;
    }


    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        MenuItem toggleMenuItem = menu.findItem(R.id.action_toggle);
        if (otpItem.getPasswordEnabled()) {
            toggleMenuItem.setTitle("Disable Password");
        } else {
            toggleMenuItem.setTitle("Enable Password");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_toggle:
                //Toggle Password Settings
                System.out.println("Password: " + otpItem.getAppPassword());
                if(otpItem.getPasswordEnabled()) {
                    try {
                        String fileName = "login.dat";
                        OutputStreamWriter out = new OutputStreamWriter(openFileOutput(fileName, 0));
                        out.write("0\n"); //Disable Password Prompt
                        out.write(otpItem.getAppPassword()); //Store the password in plaintext
                        out.close();

                        otpItem.setPasswordEnabled(false);
                    } catch(Exception e) { e.printStackTrace(); }
                } else {
                    try {
                        String hash = BCrypt.hashpw(otpItem.getAppPassword(),BCrypt.gensalt());
                        System.out.println(hash);

                        String fileName = "login.dat";
                        OutputStreamWriter out = new OutputStreamWriter(openFileOutput(fileName, 0));
                        out.write("1\n"); //Enable Password Prompt
                        out.write(hash); //Store the password's bcrypt hash
                        out.close();

                        otpItem.setPasswordEnabled(true);
                    } catch(Exception e) { e.printStackTrace(); }
                }
                updateMenu(); //Refresh Menu Item
                return true;
            case R.id.action_clear:
                //Clear Data and Return to Login Page
                try {
                    File file = getApplicationContext().getFileStreamPath("login.dat");
                    file.delete();
                    File file2 = getApplicationContext().getFileStreamPath("seedStore.bks");
                    file2.delete();

                    Intent intent = new Intent(this, AppPasswordActivity.class);
                    startActivity(intent);
                    finish();
                } catch(Exception e) { e.printStackTrace(); }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            Fragment fragment;

            switch(position) {
                case 0:
                    fragment = new EnrollmentFragment();
                    break;
                case 1:
                    fragment = new OTPFragment();
                    break;
                default:
                    fragment = new Fragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.enroll_section).toUpperCase(l);
                case 1:
                    return getString(R.string.otp_section).toUpperCase(l);
            }
            return null;
        }
    }
}
