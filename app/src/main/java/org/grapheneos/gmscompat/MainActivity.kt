package org.grapheneos.gmscompat

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.grapheneos.gmscompat.Constants.PLAY_SERVICES_PKG

val USAGE_GUIDE_URL = "https://grapheneos.org/usage#sandboxed-google-play"

class MainActivity : AppCompatActivity(R.layout.main_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isPkgInstalled(this, PLAY_SERVICES_PKG)) {
            val uri = Uri.parse(USAGE_GUIDE_URL)
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            finishAndRemoveTask()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (App.isGserviceEnabled(GserviceBroker.ID_GoogleLocationManagerService)
            && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED)
        {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.missing_location_permission, packageManager.backgroundPermissionOptionLabel))
                .setCancelable(false)
                .setPositiveButton(R.string.open_settings) { _, _ ->
                    startActivity(appSettingsIntent(packageName))
                }
                .show()
        }
    }
}

fun isPkgInstalled(ctx: Context, pkg: String): Boolean {
    try {
        ctx.packageManager.getPackageInfo(pkg, 0)
        return true
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }
}
