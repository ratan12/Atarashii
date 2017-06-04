package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.Profile
import java.util.*

class FriendsNetworkTask(private val forcesync: Boolean, private val callback: FriendsNetworkTask.FriendsNetworkTaskListener?, private val activity: Activity, private val id: Int) : AsyncTask<String, Void, ArrayList<Profile>>() {

    override fun doInBackground(vararg params: String): ArrayList<Profile>? {
        val isNetworkAvailable = APIHelper.isNetworkAvailable(activity)
        var result: ArrayList<Profile>? = null
        val cManager = ContentManager(activity)
        try {
            if (forcesync && isNetworkAvailable) {
                result = request(cManager, params[0])
            } else if (params[0].equals(AccountService.username!!, ignoreCase = true) && id != 1) {
                result = cManager.friendListFromDB
                if ((result == null || result.isEmpty()) && isNetworkAvailable)
                    result = request(cManager, params[0])
            } else if (id != 1 || isNetworkAvailable) {
                result = request(cManager, params[0])
            }

            /*
             * returning null means there was an error, so return an empty ArrayList if there was no error
             * but an empty result
             */
            if (result == null)
                result = ArrayList<Profile>()
        } catch (e: Exception) {
            AppLog.logTaskCrash("FriendsNetworkTask", "doInBackground(5): task unknown API error (?)", e)
        }

        return result
    }

    private fun request(cManager: ContentManager, param: String): ArrayList<Profile> {
        when (id) {
            0 -> return cManager.downloadAndStoreFriendList(param)
            1 -> return cManager.getFollowers(param)
            else -> return cManager.downloadAndStoreFriendList(param)
        }
    }

    override fun onPostExecute(result: ArrayList<Profile>) {
        callback?.onFriendsNetworkTaskFinished(result)
    }

    interface FriendsNetworkTaskListener {
        fun onFriendsNetworkTaskFinished(result: ArrayList<Profile>)
    }
}
