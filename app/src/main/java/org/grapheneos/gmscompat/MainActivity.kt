package org.grapheneos.gmscompat

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.Switch

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        val pad = (16f * resources.displayMetrics.density).toInt()
        ll.setPadding(pad, pad, pad, pad)

        val sw = Switch(this)
        sw.textSize = 18f
        sw.isChecked = App.isGserviceEnabled(GserviceBroker.ID_GoogleLocationManagerService)
        sw.setText(R.string.reroute_location_requests_to_os_apis)
        sw.setOnCheckedChangeListener { _, isChecked ->
            App.setGserviceState(GserviceBroker.ID_GoogleLocationManagerService, isChecked)
        }
        ll.addView(sw)

        setContentView(ll)
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.missing_location_permission, packageManager.backgroundPermissionOptionLabel))
                .setCancelable(false)
                .setPositiveButton(R.string.open_settings, { _, _ ->
                    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    i.setData(Uri.fromParts("package", packageName, null))
                    startActivity(i)
                })
                .show()
        }
    }
}
