package org.grapheneos.gmscompat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ResetGservices : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logd{intent}
        // Gservice clients are strongly bound, will exit too
        System.exit(0)
    }
}
