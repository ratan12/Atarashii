package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.Profile

class UserNetworkTask(private val forcesync: Boolean, private val callback: UserNetworkTask.UserNetworkTaskListener?, val activity: Activity) : AsyncTask<String, Void, Profile?>() {

    override fun doInBackground(vararg params: String): Profile? {
        val isNetworkAvailable = APIHelper.isNetworkAvailable(activity)
        var result: Profile? = null
        val cManager = ContentManager(activity)

        try {
            if (!AccountService.isMAL && isNetworkAvailable)
                cManager.verifyAuthentication()

            if (forcesync && isNetworkAvailable) {
                result = cManager.getProfile(params[0])
            } else if (params[0].equals(AccountService.username!!, ignoreCase = true)) {
                result = cManager.profileFromDB
                if (result == null && isNetworkAvailable)
                    result = cManager.getProfile(params[0])
            } else if (isNetworkAvailable) {
                result = cManager.getProfile(params[0])
            }

            if (result != null && isNetworkAvailable && params.size == 2) {
                val activities = cManager.getActivity(params[0], Integer.parseInt(params[1]))
                result.activity = activities
            }
        } catch (e: Exception) {
            AppLog.logTaskCrash("UserNetworkTask", "doInBackground(5): task unknown API error (?)", e)
        }

        return result
    }

    override fun onPostExecute(result: Profile?) {
        callback?.onUserNetworkTaskFinished(result)
    }

    interface UserNetworkTaskListener {
        fun onUserNetworkTaskFinished(result: Profile?)
    }
}
