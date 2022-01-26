package org.grapheneos.gmscompat

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import org.grapheneos.gmscompat.Constants.PLAY_SERVICES_PKG
import org.grapheneos.gmscompat.Constants.PLAY_STORE_PKG

class MainFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val ctx = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(ctx)

        screen.addPref().apply {
            title = getString(R.string.usage_guide)
            val uri = Uri.parse(USAGE_GUIDE_URL)
            linkToActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        screen.addPref().apply {
            title = getString(R.string.component_system_settings, getString(R.string.play_services))
            linkToAppSettings(PLAY_SERVICES_PKG)
        }
        SwitchPreferenceCompat(ctx).apply {
            title = getString(R.string.reroute_location_requests_to_os_apis)
            isSingleLineTitle = false
            isChecked = App.isGserviceEnabled(GserviceBroker.ID_GoogleLocationManagerService)
            setOnPreferenceChangeListener { _, value ->
                val redirectionEnabled = value as Boolean
                App.setGserviceState(GserviceBroker.ID_GoogleLocationManagerService, redirectionEnabled)

                var msg: String? = null
                if (redirectionEnabled) {
                    // other location modes imply this one
                    if (playServicesHasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        msg = getString(R.string.revoke_location_perm)
                    }
                } else {
                    if (!playServicesHasPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        msg = getString(R.string.grant_location_perm, ctx.packageManager.backgroundPermissionOptionLabel)
                    }
                }
                if (msg != null) {
                    AlertDialog.Builder(ctx)
                        .setMessage(msg)
                        .setPositiveButton(R.string.open_settings) { _, _ ->
                            startActivity(appSettingsIntent(PLAY_SERVICES_PKG))
                        }
                        .show()
                }
                true
            }
            screen.addPreference(this)
        }

        if (isPkgInstalled(ctx, PLAY_STORE_PKG)) {
            val playStore = getString(R.string.play_store)
            // no public activity to show its preferences
    //        screen.addPref().apply {
    //            title = getString(R.string.component_settings, playStore)
    //            linkToActivity(Intent().setClassName(PLAY_STORE_PKG, "?"))
    //        }
            screen.addPref().apply {
                title = getString(R.string.component_system_settings, playStore)
                linkToAppSettings(PLAY_STORE_PKG)
            }
        }
        screen.addPref().apply {
            title = getString(R.string.google_settings)
            linkToActivity(Intent().setClassName(PLAY_SERVICES_PKG, PLAY_SERVICES_PKG +
                ".app.settings.GoogleSettingsLink"))
//                ".app.settings.GoogleSettingsIALink")
//                ".app.settings.GoogleSettingsActivity")
        }

        preferenceScreen = screen
    }

    fun PreferenceScreen.addPref(): Preference {
        val pref = Preference(context)
        pref.isSingleLineTitle = false
        addPreference(pref)
        return pref
    }

    fun Preference.linkToActivity(intent: Intent) {
        setOnPreferenceClickListener {
            startActivity(intent)
            true
        }
    }

    fun Preference.linkToAppSettings(pkg: String) {
        linkToActivity(appSettingsIntent(pkg))
    }

    fun playServicesHasPermission(perm: String): Boolean {
        return requireContext().packageManager.checkPermission(perm, PLAY_SERVICES_PKG) == PackageManager.PERMISSION_GRANTED
    }
}

fun appSettingsIntent(pkg: String): Intent {
    val uri = Uri.fromParts("package", pkg, null)
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
}
