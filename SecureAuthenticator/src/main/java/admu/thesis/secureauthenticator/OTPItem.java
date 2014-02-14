package admu.thesis.secureauthenticator;

import android.app.Application;

/**
 * Created by Ian on 7/18/13.
 */
public class OTPItem extends Application {
    private String seed = "null";
    private String appPassword = "";
    private boolean passwordEnabled;

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getAppPassword() { return appPassword; }

    public void setAppPassword(String appPassword) { this.appPassword = appPassword; }

    public boolean getPasswordEnabled() { return passwordEnabled; }

    public void setPasswordEnabled(boolean passwordEnabled) { this.passwordEnabled = passwordEnabled; }
}
