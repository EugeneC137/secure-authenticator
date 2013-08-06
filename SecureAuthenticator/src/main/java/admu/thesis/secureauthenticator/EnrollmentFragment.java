package admu.thesis.secureauthenticator;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Ian on 7/16/13.
 */
public class EnrollmentFragment extends Fragment{
    public EnrollmentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_enrollment, container, false);

        Button submitButton = (Button) rootView.findViewById(R.id.submitButton);

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                startActivity(new Intent("android.credentials.UNLOCK"));
            } else {
                startActivity(new Intent("com.android.credentials.UNLOCK"));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

        //Turn off Strict Mode for now
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitData();
            }
        });


        return rootView;
    }

    public void submitData() {
        EditText usernameField = (EditText) getActivity().findViewById(R.id.usernameField);
        EditText passwordField = (EditText) getActivity().findViewById(R.id.passwordField);

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        try {
            HttpClient client = new DefaultHttpClient();

            KeyStore trustStore  = KeyStore.getInstance("PKCS12");
            InputStream instream = getActivity().getResources().openRawResource(R.raw.keystore);
            try {
                trustStore.load(instream, "adminpass".toCharArray());
            } finally {
                try { instream.close(); } catch (Exception ignore) {}
            }

            SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
            Scheme sch = new Scheme("https", socketFactory, 8443);
            client.getConnectionManager().getSchemeRegistry().register(sch);


            System.out.println("Setting Connection");

            HttpPost httppost = new HttpPost("https://192.168.254.115:8443/SecureAuthenticatorServer/enroll");

            //Add username and password to HTTP Post
            List<NameValuePair> values = new ArrayList<NameValuePair>(2);
            values.add(new BasicNameValuePair("enrollUsername",username));
            values.add(new BasicNameValuePair("enrollPassword",password));
            httppost.setEntity(new UrlEncodedFormEntity(values));

            System.out.println("Sending Post");

            //Execute Post Request and Obtain Response
            HttpResponse response = client.execute(httppost);

            System.out.println("Post Sent");

            //Read contents of the response
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            System.out.println(responseString);

            //Index 0 == Error Type; Index 1 == Seed (if any)
            String messages[] = responseString.split("\\|");

            if(!messages[0].substring(6,messages[0].length()).equals("false")) { //Check if an error exists
                //Display Error Message
            } else {
                //Continue with procedure

                String seed = messages[1].substring(5,messages[1].length());

                //Temporary storage as a variable in memory (for testing only)
                OTPItem otpItem = (OTPItem) getActivity().getApplicationContext();
                otpItem.setSeed(seed);

                System.out.println("Things are fine.");

                //Permenently store the seed inside the phone

                //Flag determining that keystore already exists
                //boolean existFlag;
                KeyStore seedStore;

                //Check if keystore already exists
                /*
                if(getActivity().getApplicationContext().getFileStreamPath("/Android/data/admu.thesis.secureauthenticator/files/seedStore.p12").exists()) {
                    seedStore  = KeyStore.getInstance("PKCS12");
                    FileInputStream fis = new FileInputStream("Android/data/admu.thesis.secureauthenticator/files/seedStore.p12");
                    try {
                        seedStore.load(fis, "adminpass".toCharArray());
                    } catch (Exception e) { e.printStackTrace(); }
                } else {}
                */

                seedStore = KeyStore.getInstance(KeyStore.getDefaultType());
                char[] newKeyPass = "some arbitrary password".toCharArray();
                seedStore.load(null, newKeyPass);

                System.out.println("KeyStore Initialized");

                try{
                    //seedStore.setKeyEntry("otpSeed", seed.getBytes("UTF-8"), null);

                    byte[] encodedKey     = seed.getBytes("UTF-8");
                    SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
                    seedStore.setKeyEntry("otpSeed", key, newKeyPass, null);

                    //Test Code
                    String seedCopy = new String(seedStore.getKey("otpSeed", newKeyPass).getEncoded(), "UTF-8");
                    System.out.println(seedCopy);

                    String fileName = "seedStore.bks";

                    FileOutputStream fos = getActivity().getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                    seedStore.store(fos, newKeyPass);
                    fos.close();
                } catch(Exception e) {
                    System.out.println("FAILED HERE");

                    e.printStackTrace();
                }

                System.out.println("Write Complete at: " + getActivity().getApplicationContext().getFilesDir());

                System.out.println("Went Here");

                //Display Success Message (using dialog)

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String appendZeros(String s) {
        if(s.length() < 6) {
            return(appendZeros("0".concat(s)));
        } else {
            return s;
        }
    }
}
