package org.grapheneos.gmscompat.location

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.util.SparseArray
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.ILocationCallback
import com.google.android.gms.location.ILocationListener
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.internal.FusedLocationProviderResult
import com.google.android.gms.location.internal.IFusedLocationProviderCallback
import com.google.android.gms.location.internal.IGoogleLocationManagerService
import com.google.android.gms.location.internal.ISettingsCallbacks
import com.google.android.gms.location.internal.LocationRequestUpdateData
import org.grapheneos.gmscompat.App
import org.grapheneos.gmscompat.logd
import org.grapheneos.gmscompat.opModeToString
import java.lang.ref.WeakReference
import java.util.Arrays
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class GLocationService(val ctx: Context) : IGoogleLocationManagerService.Stub() {
    val appOpsManager = ctx.getSystemService(AppOpsManager::class.java)!!
    val packageManager = ctx.packageManager!!
    val listenerCallbacksExecutor = Executors.newCachedThreadPool()
    val nonClientLocationManager = ctx.getSystemService(LocationManager::class.java)!!

    // maps app uid to its registered listeners
    // gc would be hard to do correctly if listeners were strongly referenced
    val mapOfListeners = SparseArray<WeakReference<Listeners>>(50)

    override fun updateLocationRequest(data: LocationRequestUpdateData) {
//        logd{data}
        val pendingIntent = data.pendingIntent
        val client = Client(this, pendingIntent?.creatorPackage, data.request?.contextAttributionTag, data.appOpsReasonMessage)

        val listeners: Listeners =
        synchronized(mapOfListeners) {
            val cur = mapOfListeners.get(client.uid)?.get()
            if (cur != null) {
                cur
            } else {
                val l = Listeners(7)
                mapOfListeners.put(client.uid, WeakReference(l))
                l
            }
        }
        if (data.opCode == LocationRequestUpdateData.OP_REQUEST_UPDATES) {
            val key: Any
            val glf: GLocationForwarder
            if (pendingIntent != null) {
                if (pendingIntent.creatorUid != client.uid) {
                    throw SecurityException()
                }
                key = pendingIntent
                glf = GlfPendingIntent(pendingIntent)
            } else {
                val lcallback: ILocationCallback? = data.callback
                val llistener: ILocationListener? = data.listener
                if (lcallback != null) {
                    key = lcallback.asBinder()
                    glf = GlfLocationCallback(lcallback)
                } else {
                    key = llistener!!.asBinder()
                    glf = GlfLocationListener(llistener)
                }
            }
            glf.listeners = listeners

            val req: LocationRequest = data.request.request
            val oll = OsLocationListener(client, client.getProvider(req), req.toOsLocationRequest(), glf)
            listeners.update(client, key, oll)
        } else {
            require(data.opCode == LocationRequestUpdateData.OP_REMOVE_UPDATES)
            val key: Any
            if (pendingIntent != null) {
                key = pendingIntent
            } else {
                val lcallback: ILocationCallback? = data.callback
                val llistener: ILocationListener? = data.listener
                if (lcallback != null) {
                    key = lcallback.asBinder()
                } else {
                    key = llistener!!.asBinder()
                }
            }
            listeners.remove(client, key)
        }
        data.fusedLocationProviderCallback.onFusedLocationProviderResult(FusedLocationProviderResult.SUCCESS)
    }

    fun gcMapOfListeners() {
        val map: SparseArray<WeakReference<Listeners>> = mapOfListeners
        synchronized(map) {
            var i = 0
            while (i < map.size()) {
                if (map.valueAt(i).get() == null) {
                    map.delete(i)
                } else {
                    ++i
                }
            }
        }
    }

    // https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient#flushLocations()
    override fun flushLocations(callback: IFusedLocationProviderCallback) {
        logd{callback}
        val client = Client(this)

        val listeners =
        synchronized(mapOfListeners) {
            val cur = mapOfListeners.get(client.uid)?.get()
            if (cur != null) {
                cur
            } else {
                return
            }
        }
        listeners.all().forEach {
            it.flush()
        }
        callback.onFusedLocationProviderResult(FusedLocationProviderResult.SUCCESS)
    }

    // https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient#getLastLocation()
    override fun getLastLocation3(contextAttributionTag: String?): Location? {
        logd{contextAttributionTag}
        val client = Client(this, attributionTag = contextAttributionTag)

        val opMode = client.noteProxyAppOp()
        if (opMode != MODE_ALLOWED) {
            throw SecurityException("noteProxyAppOp returned ${opModeToString(opMode)}")
        }
        val criteria = Criteria()
        criteria.accuracy =
        if (client.permission == Permission.FINE) {
            Criteria.ACCURACY_FINE
        } else {
            Criteria.ACCURACY_COARSE
        }
        val lm = client.locationManager
        val provider = lm.getBestProvider(criteria, true)!!
        val loc = lm.getLastKnownLocation(provider)
        if (loc != null) {
            if (client.permission == Permission.COARSE) {
                val pp = lm.getProviderProperties(provider)
                if (pp!!.accuracy == ProviderProperties.ACCURACY_FINE) {
                    return LocationFudger().createCoarse(loc)
                }
            }
        }
        return loc
    }

    override fun getLastLocation(): Location? {
        logd()
        return getLastLocation3(null)
    }

    override fun getLastLocation2(packageName: String?): Location? {
        logd()
        // GMS ignores packageName too
        return getLastLocation3(null)
    }

    // https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient#getLocationAvailability()
    override fun getLocationAvailability(packageName: String?): LocationAvailability {
        logd{packageName}
        val client = Client(this)
        return LocationAvailability.get(client.locationManager.isLocationEnabled())
    }

    // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient#checkLocationSettings(com.google.android.gms.location.LocationSettingsRequest)
    override fun requestLocationSettingsDialog(
        settingsRequest: LocationSettingsRequest,
        callback: ISettingsCallbacks,
        packageName: String?
    ) {
        logd{settingsRequest}
        // GMS doesn't check whether caller has a location permission in this case

        val lss = LocationSettingsStates()
        lss.gpsPresent = true
        lss.gpsUsable = nonClientLocationManager.isLocationEnabled()
//        lss.networkLocationPresent = true;
//        lss.networkLocationUsable = lss.gpsUsable;

        callback.onLocationSettingsResult(LocationSettingsResult(lss, Status.SUCCESS))
    }

    companion object {
        @JvmField
        val INSTANCE = GLocationService(App.ctx)

        @JvmField
        val CODES = intArrayOf(
            FIRST_CALL_TRANSACTION + 58, // updateLocationRequest
            FIRST_CALL_TRANSACTION + 66, // flushLocations
            FIRST_CALL_TRANSACTION + 6,  // getLastLocation
            FIRST_CALL_TRANSACTION + 20, // getLastLocation2
            FIRST_CALL_TRANSACTION + 79, // getLastLocation3
            FIRST_CALL_TRANSACTION + 33, // getLocationAvailability
            FIRST_CALL_TRANSACTION + 62, // requestLocationSettingsDialog
        )
        init {
            Arrays.sort(CODES)
        }
    }
}
