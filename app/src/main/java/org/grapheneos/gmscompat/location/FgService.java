package org.grapheneos.gmscompat.location;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import org.grapheneos.gmscompat.App;
import org.grapheneos.gmscompat.R;
import org.grapheneos.gmscompat.UtilsKt;

import androidx.annotation.NonNull;

public class FgService extends Service {
    private static final String NOTIF_CH_ID = "01";
    private static final int NOTIF_ID = 1;

    private boolean foreground;

    static void setState(boolean foreground) {
        var m = Message.obtain();
        m.arg1 = foreground? ACTION_START_FG : ACTION_STOP_FG;
        AHandler.INSTANCE.sendMessage(m);
    }

    public void onCreate() {
        var title = getString(R.string.monitoring_location);
        var nc = new NotificationChannel(NOTIF_CH_ID, title, NotificationManager.IMPORTANCE_LOW);
        nc.setShowBadge(false);
        getSystemService(NotificationManager.class).createNotificationChannel(nc);
    }

    private static final int ACTION_STOP_FG = 0;
    private static final int ACTION_START_FG = 1;

    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = Integer.parseInt(intent.getAction());
        if (action == ACTION_STOP_FG) {
            if (foreground) {
                UtilsKt.logds("stopForeground");
                stopForeground(true);
                foreground = false;
            }
        } else if (action == ACTION_START_FG) {
            if (!foreground) {
                UtilsKt.logds("startForeground");
                var nb = new Notification.Builder(this, NOTIF_CH_ID);
                nb.setSmallIcon(R.drawable.ic_location_24);
                nb.setContentTitle(getString(R.string.monitoring_location));
                // nb.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
                startForeground(NOTIF_ID, nb.build());
                foreground = true;
            }
        } else {
            throw new IllegalStateException(intent.toString());
        }
        return START_NOT_STICKY;
    }

    static class AHandler extends Handler {
        static final AHandler INSTANCE = new AHandler();

        private AHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(@NonNull Message msg) {
            if (hasMessages(0)) {
                // to avoid thrashing
                return;
            }
            Context ctx = App.ctx;
            var i = new Intent(ctx, FgService.class);
            int action = msg.arg1;
            i.setAction(Integer.toString(action));
            if (action == ACTION_START_FG) {
                ctx.startForegroundService(i);
            } else {
                ctx.startService(i);
            }
        }
    }

    public IBinder onBind(Intent intent) { return null; }
}
