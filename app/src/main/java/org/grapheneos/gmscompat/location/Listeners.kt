package org.grapheneos.gmscompat.location

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PendingIntent
import android.os.RemoteException
import android.util.ArrayMap
import android.util.Log
import org.grapheneos.gmscompat.logd
import org.grapheneos.gmscompat.opModeToString

// all listeners registered by a given uid. key is either Binder or PendingIntent
typealias Listeners = ArrayMap<Any, OsLocationListener>

fun Listeners.all(): List<OsLocationListener> {
    synchronized(this) {
        val size = this.size
        val res = ArrayList<OsLocationListener>(size)
        for (i in 0 until size) {
            res.add(valueAt(i))
        }
        return res
    }
}

@SuppressLint("MissingPermission")
fun Listeners.update(client: Client, key: Any, listener: OsLocationListener) {
    synchronized(this) {
        try {
            listener.forwarder.prepareForRegistration()
        } catch (e: RemoteException) {
            logd{e}
            throw e.cause!!
        }
        if (size == 0) {
            val opMode = client.startMonitorAppOp()
            if (opMode != AppOpsManager.MODE_ALLOWED) {
                throw SecurityException("startMonitorAppOp returned " + opModeToString(opMode))
            }
        }
        client.locationManager.requestLocationUpdates(listener.provider, listener.request, client.gls.listenerCallbacksExecutor, listener)

        val curIdx = indexOfKey(key)
        if (curIdx >= 0) {
            removeInternal(valueAt(curIdx))
            setValueAt(curIdx, listener)
            logd{"client ${client.packageName} updated listener $key, listenerCount $size"}
        } else {
            put(key, listener)
            logd{"client ${client.packageName} added listener $key, listenerCount $size"}
        }
    }
}

private fun removeInternal(listener: OsLocationListener) {
    try {
        listener.client.locationManager.removeUpdates(listener)
        listener.forwarder.callbackAfterUnregistration()
    } catch (e: Throwable) {
        Log.e("Listeners", "listener removal should never fail", e)
        System.exit(1)
    }
}

fun Listeners.remove(client: Client, key: Any) {
    var gc = false
    synchronized(this) {
        val idx = indexOfKey(key)
        if (idx < 0) {
            return
        }
        removeInternal(valueAt(idx))
        removeAt(idx)
        logd{"client ${client.packageName} removed listener $key, listenerCount $size"}

        if (size == 0) {
            client.finishMonitorAppOp()
            gc = true
        }
    }
    if (gc) {
        // won't collect this instance, but may collect ones that were already empty
        client.gls.gcMapOfListeners()
    }
}
