package org.grapheneos.gmscompat;

import android.os.Binder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BinderProvider extends AbsContentProvider {
    private static final String KEY_BINDER = "binder";

    private static final Binder dummyBinder = new Binder();

    @Nullable
    public Bundle call(@NonNull String pkg, @Nullable String arg, @Nullable Bundle extras) {
        // UtilsKt.logds("callingPkg " + pkg + " callingPid " + Binder.getCallingPid());

        PersistentFgService.maybeStart(getContext(), pkg);

        var res = new Bundle(1);
        res.putBinder(KEY_BINDER, dummyBinder);
        return res;
    }
}
