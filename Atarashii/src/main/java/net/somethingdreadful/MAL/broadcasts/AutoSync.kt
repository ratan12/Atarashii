package net.somethingdreadful.MAL.broadcasts

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.app.NotificationCompat
import android.util.Log
import net.somethingdreadful.MAL.*
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.tasks.NetworkTask
import net.somethingdreadful.MAL.tasks.TaskJob
import java.util.*

class AutoSync : BroadcastReceiver(), NetworkTask.NetworkTaskListener {

    override fun onReceive(context: Context?, intent: Intent) {
        if (context == null) {
            AppLog.log(Log.ERROR, "Atarashii", "AutoSync.onReceive(): context is null")
            return
        }
        PrefManager.create(context)
        AccountService.create(context)
        if (APIHelper.isNetworkAvailable(context) && AccountService.getAccount() != null) {
            val notificationIntent = Intent(context, Home::class.java)
            val contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            if (!networkChange(intent) || !PrefManager.getAutosyncDone()) {
                val args = ArrayList<String>()
                args.add(ContentManager.listSortFromInt(0, true))
                args.add(1.toString())
                args.add(false.toString())
                NetworkTask(true, context, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *args.toTypedArray())
                NetworkTask(false, context, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *args.toTypedArray())

                val mBuilder = NotificationCompat.Builder(context)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.toast_info_SyncMessage))
                        .setContentIntent(contentIntent)

                nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm!!.notify(R.id.notification_sync, mBuilder.build())
            }
        } else if (!networkChange(intent)) {
            PrefManager.setAutosyncDone(false)
        }
    }

    override fun onNetworkTaskFinished(result: Any, job: TaskJob, isAnime: Boolean) {
        nm!!.cancel(R.id.notification_sync)
        PrefManager.setAutosyncDone(true)
    }

    override fun onNetworkTaskError(job: TaskJob) {
        nm!!.cancel(R.id.notification_sync)
        PrefManager.setAutosyncDone(false)
    }

    private fun networkChange(intent: Intent?): Boolean {
        return intent != null && intent.action != null && intent.action == android.net.ConnectivityManager.CONNECTIVITY_ACTION
    }

    companion object {
        private var nm: NotificationManager? = null
    }
}