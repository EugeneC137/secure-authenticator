package admu.thesis.secureauthenticator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Ian on 7/16/13.
 */
public class OTPFragment extends Fragment implements Runnable{
    //Base Time determines the counter value during TOTP generation
    double baseTime; //Base time counter remains unchanged for 30 seconds
    double newBaseTime;
    double newTime; //Always keeps track of current time;

    double percent;
    int progress; //Progress until next 30 seconds

    ProgressBar timeProgress;
    TextView timeCounter; //Displays base time counter

    public OTPFragment() {
    }

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

            System.out.println(baseTime);
            while(true) {
                newTime = System.currentTimeMillis();

                //System.out.println(newTime);

                newBaseTime = Math.floor(newTime / 30000);

                if(newBaseTime > baseTime) {
                    baseTime = newBaseTime;

                    System.out.println(baseTime);

                    getActivity().runOnUiThread(new Runnable() {

                        public void run() {
                            timeCounter = (TextView) getActivity().findViewById(R.id.timeCounter);
                            timeCounter.setText(Double.valueOf(baseTime).toString());
                        }
                    });
                }

                progress = (int) ((newTime % 30000.0) / 30000 * 500);

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        timeProgress = (ProgressBar) getActivity().findViewById(R.id.timeProgress);
                        timeProgress.setProgress(progress);
                    }
                });

                Thread.sleep(20); //Allow thread to sleep to reduce performance penalty
            }
        } catch(Exception e) {}
    }
}
