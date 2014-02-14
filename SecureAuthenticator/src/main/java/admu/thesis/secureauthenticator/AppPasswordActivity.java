package admu.thesis.secureauthenticator;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.UnrecoverableEntryException;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class AppPasswordActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Check whether or not password was disabled
        try {
            System.out.println("Searching for Flag File");

            String fileName = "login.dat";
            File file = getApplicationContext().getFileStreamPath(fileName);
            OTPItem otpItem = (OTPItem) getApplicationContext();

            if(file.exists()) {
                System.out.println("Reading Flag Data");

                InputStreamReader in  = new InputStreamReader(getApplicationContext().openFileInput(fileName));
                BufferedReader buffer = new BufferedReader(in);
                String line = buffer.readLine();
                String line2 = buffer.readLine(); //Relevant only if password was disabled
                buffer.close();

                System.out.println("Flag Data Read");

                try {
                    System.out.println(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int passwordEnabled = Integer.parseInt(line);


                if(passwordEnabled == 0) { //If password was disabled by the user, move to main activity
                    System.out.println("Password Disabled; Skipping to Main Activity");
                    otpItem.setPasswordEnabled(false);
                    otpItem.setAppPassword(line2);

                    super.onCreate(savedInstanceState);
                    Intent intent = new Intent(AppPasswordActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(passwordEnabled == 1) { //Start Password Prompt
                    System.out.println("Password Enabled; Running Password Prompt");
                    otpItem.setPasswordEnabled(true);
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.activity_app_password);

                    if (savedInstanceState == null) {
                        getFragmentManager().beginTransaction()
                                .add(R.id.container, new AppPasswordFragment())
                                .commit();
                    }
                }
            } else { //Run Password Prompt for the First Time
                System.out.println("Running First Time Password Prompt");
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_app_password);

                if (savedInstanceState == null) {
                    getFragmentManager().beginTransaction()
                            .add(R.id.container, new AppPasswordFirstFragment())
                            .commit();
                }
            }
        } catch(Exception e) {
            System.out.println("Something Terrible Happened!");
        }


    }

    public void incorrectDialog() {
        DialogFragment dialog = new PasswordIncorrectDialogFragment();
        dialog.show(getSupportFragmentManager(),"incorrect");

    }

    public void mismatchDialog() {
        DialogFragment newFragment = new PasswordMismatchDialogFragment();
        newFragment.show(getSupportFragmentManager(),"mismatch");
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    public static class AppPasswordFragment extends Fragment {

        public AppPasswordFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_app_password, container, false);

            Button enterButton = (Button) rootView.findViewById(R.id.EnterButton);
            enterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    authenticate();
                }
            });

            return rootView;
        }

        public void authenticate() {
            EditText pField = (EditText) getActivity().findViewById(R.id.AppPasswordField);
            String password = pField.getText().toString();

            System.out.println("Authenticating Password");

            try {
                String fileName = "login.dat";
                File file = getActivity().getApplicationContext().getFileStreamPath(fileName);

                if(file.exists()) {
                    System.out.println("Reading Data");

                    InputStreamReader in  = new InputStreamReader(getActivity().openFileInput(fileName));
                    BufferedReader buffer = new BufferedReader(in);
                    buffer.readLine(); //Skip the first line
                    String hash = buffer.readLine(); //Read the stored hash
                    buffer.close();

                    System.out.println("Input Password: " + password);
                    System.out.println("Hash: " + hash);

                    if(BCrypt.checkpw(password,hash)) { //Password is correct; proceed to main activity

                        System.out.println("Password Match!");
                        OTPItem item = (OTPItem) getActivity().getApplicationContext();
                        item.setAppPassword(password);

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        ((AppPasswordActivity) getActivity()).incorrectDialog();
                    }
                }

            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class AppPasswordFirstFragment extends Fragment {
        public AppPasswordFirstFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_app_password_first, container, false);

            Button enterFirstButton = (Button) rootView.findViewById(R.id.EnterFirstButton);

            enterFirstButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    checkPasswords();
                }
            });

            return rootView;
        }

        public void checkPasswords() {
            EditText firstField = (EditText) getActivity().findViewById(R.id.AppPasswordFieldFirstTime);
            EditText secondField = (EditText) getActivity().findViewById(R.id.confirmAppPasswordField);

            String p1 = firstField.getText().toString();
            String p2 = secondField.getText().toString();

            if(p1.equals(p2)) {
                System.out.println("Found a Match");

                //Save password hash and proceed to Main Activity
                try {
                    System.out.println("Saving Password Preference");

                    String fileName = "login.dat";
                    OutputStreamWriter out = new OutputStreamWriter(getActivity().openFileOutput(fileName, 0));
                    out.write("1\n"); //Password-enabled by default during initial setup

                    System.out.println("Saving Password Hash");

                    String salt = BCrypt.gensalt();
                    out.write(BCrypt.hashpw(p1,salt)); //Store a bcrypt hash of the password (Work Factor: 6)
                    out.close();

                    System.out.println("Password Preference Saved");

                    OTPItem item = (OTPItem) getActivity().getApplicationContext();
                    item.setAppPassword(p1);
                    item.setPasswordEnabled(true);

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();

                } catch(Exception e) {
                    e.printStackTrace();
                }

            } else { //Throw error dialog
                ((AppPasswordActivity) getActivity()).mismatchDialog();
            }
        }
    }

    public static class PasswordMismatchDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.passwordMismatch)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class PasswordIncorrectDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.passwordIncorrect)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
