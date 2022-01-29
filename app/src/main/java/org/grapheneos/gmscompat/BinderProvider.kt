package org.grapheneos.gmscompat

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.ArraySet

private const val KEY_BINDER = "binder"

class BinderProvider : AbsContentProvider() {
    override fun call(pkg: String, arg: String?, extras: Bundle?): Bundle? {
        logd{"callingPkg " + pkg + " callingPid " + Binder.getCallingPid()}

        val binder = extras!!.getBinder(KEY_BINDER)!!
        try {
            // important to add before linkToDeath() to avoid race with binderDied() callback
            addBoundProcess(binder)
            binder.linkToDeath(DeathRecipient(binder), 0)
        } catch (e: RemoteException) {
            removeBoundProcess(binder)
            logd{"binder already died: " + e}
            return null
        }
        PersistentFgService.start(context, pkg)

        val res = Bundle()
        res.putBinder(KEY_BINDER, dummyBinder)
        return res
    }

    companion object {
        private val dummyBinder = Binder()
        private val boundProcesses = ArraySet<IBinder>(10)

        fun addBoundProcess(binder: IBinder) {
            synchronized(boundProcesses) {
                boundProcesses.add(binder)
            }
        }

        fun removeBoundProcess(binder: IBinder) {
            synchronized(boundProcesses) {
                boundProcesses.remove(binder)
                if (boundProcesses.size == 0) {
                    val ctx = App.ctx()
                    val i = Intent(ctx, PersistentFgService::class.java)
                    if (ctx.stopService(i)) {
                        logd{"no bound processes, stopping PersistentFgService"}
                    }
                }
            }
        }
    }

    class DeathRecipient(val binder: IBinder) : IBinder.DeathRecipient {
        override fun binderDied() {
            removeBoundProcess(binder)
        }
    }
}
