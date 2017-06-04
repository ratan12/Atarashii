package net.somethingdreadful.MAL.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RecordStatusUpdatedReceiver(private val callback: RecordStatusUpdatedReceiver.RecordStatusUpdatedListener?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == RECV_IDENT) {
            if (callback != null) {
                val isAnime = intent.getSerializableExtra("type") as Boolean
                callback.onRecordStatusUpdated(isAnime)
            }
        }
    }

    interface RecordStatusUpdatedListener {
        fun onRecordStatusUpdated(isAnime: Boolean)
    }

    companion object {
        val RECV_IDENT = "net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver"
    }
}