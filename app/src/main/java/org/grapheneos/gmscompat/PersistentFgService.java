package org.grapheneos.gmscompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

// raises priority of Play Services and Play Store, thereby allowing them to start services when they need to
public class PersistentFgService extends Service {
    private static final String TAG = "PersistentFgService";

    private static final int PLAY_SERVICES = 1;
    private static final int PLAY_STORE = 1 << 1;

    private static final String PLAY_SERVICES_PKG = "com.google.android.gms";
    private static final String PLAY_STORE_PKG = "com.android.vending";

    int boundPkgs;

    public void onCreate() {
        var title = getString(R.string.persistent_fg_service_notif);
        var nc = new NotificationChannel(App.NotificationChannels.PERSISTENT_FG_SERVICE, title, NotificationManager.IMPORTANCE_LOW);
        nc.setShowBadge(false);
        getSystemService(NotificationManager.class).createNotificationChannel(nc);

        var nb = new Notification.Builder(this, App.NotificationChannels.PERSISTENT_FG_SERVICE);
        nb.setSmallIcon(android.R.drawable.ic_dialog_dialer);
        nb.setContentTitle(title);
        nb.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        startForeground(App.NotificationIds.PERSISTENT_FG_SERVICE, nb.build());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.d(TAG, "received null intent, rechecking bound services");
            bindPlayServices();
            bindPlayStore();
            if (boundPkgs == 0) {
                Log.d(TAG, "no bound packages, calling stopSelf()");
                stopSelf();
            }
        } else {
            String pkg = intent.getAction();
            if (PLAY_SERVICES_PKG.equals(pkg)) {
                if (!bindPlayServices()) {
                    Log.w(TAG, "unable to bind to " + pkg);
                }
            } else if (PLAY_STORE_PKG.equals(pkg)) {
                if (!bindPlayStore()) {
                    Log.w(TAG, "unable to bind to " + pkg);
                }
            } else {
                // this service is protected by the signature-level permission
                throw new SecurityException("unathourized intent " + intent);
            }
        }
        // important, grants permission to call startForeground() again in case process gets recreated
        return START_STICKY;
    }

    private boolean bindPlayServices() {
        return bind(PLAY_SERVICES, PLAY_SERVICES_PKG, "com.google.android.gms.chimera.PersistentDirectBootAwareApiService");
    }

    private boolean bindPlayStore() {
        return bind(PLAY_STORE, PLAY_STORE_PKG, "com.google.android.finsky.ipcservers.main.MainGrpcServerAndroidService");
    }

    private boolean bind(int pkgId, String pkg, String cls) {
        if ((boundPkgs & pkgId) != 0) {
            Log.d(TAG, pkg + " is already bound");
            return true;
        }
        var i = new Intent();
        i.setClassName(pkg, cls);

        // BIND_INCLUDE_CAPABILITIES isn't needed, at least for now
        int flags = BIND_AUTO_CREATE | BIND_IMPORTANT;
        boolean r = bindService(i, new Connection(pkgId, this), flags);
        if (r) {
            boundPkgs |= pkgId;
        }
        return r;
    }

    static class Connection implements ServiceConnection {
        final int pkgId;
        final PersistentFgService svc;

        Connection(int pkgId, PersistentFgService svc) {
            this.pkgId = pkgId;
            this.svc = svc;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected " + name);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected " + name);
            // docs promise that onServiceConnected will be called when the other app is restarted
        }

        public void onBindingDied(ComponentName name) {
            Log.d(TAG, "onBindingDied " + name);
            // see the onBindingDied doc
            svc.unbindService(this);
            svc.boundPkgs &= ~pkgId;

            if (svc.boundPkgs == 0) {
                Log.d(TAG, "no bound pkgs, calling stopSelf()");
                svc.stopSelf();
            }
        }

        public void onNullBinding(ComponentName name) {
            throw new IllegalStateException("unable to bind " + name);
        }
    }

    @Nullable public IBinder onBind(Intent intent) { return null; }
}
