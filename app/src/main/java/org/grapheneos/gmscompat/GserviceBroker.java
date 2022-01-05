package org.grapheneos.gmscompat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;

import org.grapheneos.gmscompat.location.GLocationService;

public class GserviceBroker extends Service {
    public static final int ID_GoogleLocationManagerService = 0;
    private static final int ID_COUNT = 1;

    static class BinderImpl extends Binder {
        static final BinderImpl INSTANCE = new BinderImpl();

        private BinderImpl() {}

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            int id = code - FIRST_CALL_TRANSACTION;
            if (!App.isGserviceEnabled(id)) {
                return false;
            }
            IBinder gservice;
            int[] gserviceCodes;
            switch (id) {
                case ID_GoogleLocationManagerService:
                    gservice = GLocationService.INSTANCE;
                    gserviceCodes = GLocationService.CODES;
                    break;
                default:
                    return false;
            }
            reply.writeStrongBinder(gservice);
            reply.writeIntArray(gserviceCodes);
            return true;
        }
    }

    public IBinder onBind(Intent intent) {
        return BinderImpl.INSTANCE;
    }
}
