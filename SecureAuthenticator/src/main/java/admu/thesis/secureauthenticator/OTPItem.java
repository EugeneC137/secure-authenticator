package admu.thesis.secureauthenticator;

import android.app.Application;

/**
 * Created by Ian on 7/18/13.
 */
public class OTPItem extends Application {
    private String seed = "null";

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }
}
