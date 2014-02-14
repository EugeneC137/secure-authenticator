package admu.thesis.secureauthenticator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.crypto.SecretKey;

/**
 * Created by Ian on 7/16/13.
 */
public class OTPFragment extends Fragment implements Runnable{
    //Base Time determines the counter value during TOTP generation
    double baseTime; //Base time counter remains unchanged for 30 seconds
    double newBaseTime;
    double newTime; //Always keeps track of current time;

    int progress; //Progress until next 30 seconds

    ProgressBar timeProgress;
    TextView timeCounter; //Displays base time counter

    String keyString;
    byte[] key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_otp, container, false);

        //Establish a thread for tracking current time
        new Thread(this).start();

        return rootView;
    }

    //Thread code
    public void run() {
        try {
            baseTime = Math.floor(System.currentTimeMillis() / 30000);
            OTPItem otpItem = (OTPItem) getActivity().getApplicationContext();

            KeyStore seedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] keyPass = otpItem.getAppPassword().toCharArray();
            String fileName = "seedStore.bks";
            //String fileName = "seedStore.ubr";
            try {
                File file = getActivity().getApplicationContext().getFileStreamPath(fileName);
                if(file.exists()) {
                    FileInputStream fis = getActivity().getApplicationContext().openFileInput(fileName);
                    //FileInputStream fis = getActivity().openFileInput(fileName);
                    seedStore.load(fis, keyPass);

                    SecretKey getKey = (SecretKey)seedStore.getKey("otpSeed", keyPass);
                    String keyString = new String(getKey.getEncoded(),"UTF-8");

                    System.out.println(keyString);

                    otpItem.setSeed(keyString);
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                keyString = otpItem.getSeed();
                key = keyString.getBytes("UTF-8");
            }

            //Immediately set OTP upon thread start
            if(!keyString.equals("null")) { //Only change the time counter if a seed exists (for aesthetic purposes)
                getActivity().runOnUiThread(new Runnable() {

                    public void run() {
                        System.out.println("Inside Thread: " + keyString);

                        timeCounter = (TextView) getActivity().findViewById(R.id.timeCounter);
                        int otpValue = TOTP.generateTOTP(key, (long) baseTime);
                        String currTime = String.format("%.0f", baseTime);

                        String otp = Integer.valueOf(otpValue).toString();
                        //Append any missing zeroes
                        while (otp.length() < 6) {
                            otp = "0".concat(otp);
                        }
                        timeCounter.setText(otp);

                        System.out.println(currTime + ": " + otpValue);
                    }
                });
            }

            while(true) {
                newTime = System.currentTimeMillis();

                newBaseTime = Math.floor(newTime / 30000);

                if(newBaseTime > baseTime) {
                    baseTime = newBaseTime;

                    //Make sure seed value is updated to the latest one
                    keyString = otpItem.getSeed();
                    key = keyString.getBytes("UTF-8");

                    System.out.println("Outside Thread: " + keyString);

                    if(!keyString.equals("null")) { //Only change the time counter if a seed exists (for aesthetic purposes)
                        getActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                System.out.println("Inside Thread: " + keyString);

                                timeCounter = (TextView) getActivity().findViewById(R.id.timeCounter);
                                int otpValue = TOTP.generateTOTP(key, (long) baseTime);
                                String currTime = String.format("%.0f", baseTime);

                                String otp = Integer.valueOf(otpValue).toString();
                                //Append any missing zeroes
                                while (otp.length() < 6) {
                                    otp = "0".concat(otp);
                                }
                                timeCounter.setText(otp);

                                System.out.println(currTime + ": " + otpValue);
                            }
                        });
                    }
                }

                progress = (int) ((newTime % 30000.0) / 30000 * 500);

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    try {
                        timeProgress = (ProgressBar) getActivity().findViewById(R.id.timeProgress);
                        timeProgress.setProgress(progress);
                    } catch(Exception e) { e.printStackTrace(); }
                    }
                });

                Thread.sleep(20); //Allow thread to sleep to reduce performance penalty
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
