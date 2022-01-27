package org.grapheneos.gmscompat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class App extends Application {
    private static Context ctx;
    private static SharedPreferences preferences;

    private static final String PREFS_FILE = "prefs";
    private static final String PREF_KEY_ENABLED_GSERVICES = "enabled_gservices";
    private static int enabledGservices;

    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
        if (!Application.getProcessName().endsWith(getString(R.string.persistent_process))) {
            preferences = ctx
                // to support running in Direct Boot mode
                .createDeviceProtectedStorageContext()
                .getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
            enabledGservices = preferences.getInt(PREF_KEY_ENABLED_GSERVICES, -1);
        }
    }

    public static boolean isGserviceEnabled(int id) {
        return (enabledGservices & (1 << id)) != 0;
    }

    // shared prefs are used by multiple processes, apply() wouldn't work
    @SuppressLint("ApplySharedPref")
    public static void setGserviceState(int id, boolean enabled) {
        int flag = 1 << id;
        enabledGservices = (enabledGservices & (~ flag)) | (enabled? flag : 0);
        preferences.edit().putInt(PREF_KEY_ENABLED_GSERVICES, enabledGservices).commit();

        ctx.sendBroadcast(new Intent(ctx, ResetGservices.class));
    }

    public static Context ctx() {
        return ctx;
    }

    public static SharedPreferences preferences() {
        return preferences;
    }

    public interface NotificationChannels {
        String PERSISTENT_FG_SERVICE = "persistent_fg_service";
    }

    public interface NotificationIds {
        int PERSISTENT_FG_SERVICE = 1;
    }
}
