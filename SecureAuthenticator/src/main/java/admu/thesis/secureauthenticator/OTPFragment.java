package admu.thesis.secureauthenticator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

/**
 * Created by Ian on 7/16/13.
 */
public class OTPFragment extends Fragment {
    public OTPFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_otp, container, false);

        return rootView;
    }
}
