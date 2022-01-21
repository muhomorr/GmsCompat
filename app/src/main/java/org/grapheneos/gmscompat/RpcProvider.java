package org.grapheneos.gmscompat;

import android.os.Binder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RpcProvider extends AbsContentProvider {

    private static final int CMD_GET_DUMMY_BINDER = 0;

    private static final String KEY_BINDER = "binder";

    @Nullable
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        int cmd = Integer.parseInt(method);

        switch (cmd) {
            case CMD_GET_DUMMY_BINDER: {
                var res = new Bundle(1);
                // gives caller something to call linkToDeath() on
                res.putBinder(KEY_BINDER, new Binder());
                return res;
            }
        }
        return null;
    }
}
