package net.somethingdreadful.MAL

import android.app.Activity
import android.nfc.NfcAdapter

object NfcHelper {

    // disables "push to beam" for an activity
    fun disableBeam(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        adapter?.setNdefPushMessage(null, activity)
    }
}
