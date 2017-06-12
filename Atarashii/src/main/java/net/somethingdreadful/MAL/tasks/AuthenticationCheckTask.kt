package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import net.somethingdreadful.MAL.*
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.account.AccountType
import net.somethingdreadful.MAL.api.ALApi
import net.somethingdreadful.MAL.api.BaseModels.Profile
import net.somethingdreadful.MAL.api.MALApi
import net.somethingdreadful.MAL.database.DatabaseHelper

class AuthenticationCheckTask
/**
 * Create an userAccount and verify it.
 *
 *
 * Only use this with the FirstTimeActivity

 * @param callback Auth listener
 * *
 * @param activity The FirstTimeActivity
 */
(private val callback: AuthenticationCheckTask.AuthenticationCheckListener?, private val activity: Activity) : AsyncTask<String, Void, Boolean>() {

    override fun doInBackground(vararg params: String): Boolean? {
        try {
            // Avoid overwrite issues
            if (DatabaseHelper.DBExists(activity))
                DatabaseHelper.deleteDatabase(activity)
            if (params.size >= 2) {
                val api = MALApi(params[0], params[1])
                val valid = api.isAuth
                if (valid) {
                    AccountService.addAccount(params[0], params[1], AccountType.MyAnimeList)
                    ContentManager(activity).getProfile(params[0])
                }
                return valid
            } else {
                val api = ALApi(activity)

                val auth = api.getAuthCode(params[0])
                var profile: Profile? = api.currentUser
                if (profile == null)
                // try again
                    profile = api.currentUser

                if (profile == null) {
                    Theme.Snackbar(activity, R.string.toast_error_keys)
                } else {
                    AccountService.addAccount(profile.username, "none", AccountType.AniList)
                    AccountService.setAccesToken(auth.access_token, java.lang.Long.parseLong(auth.expires_in))
                    AccountService.refreshToken = auth.refresh_token

                    PrefManager.setNavigationBackground(profile.imageUrlBanner)
                    return true
                }
            }
        } catch (e: Exception) {
            AppLog.logTaskCrash("AuthenticationCheckTask", "doInBackground()", e)
            e.printStackTrace()
        }

        return false
    }

    override fun onPostExecute(result: Boolean?) {
        callback?.onAuthenticationCheckFinished(result!!)
    }

    interface AuthenticationCheckListener {
        fun onAuthenticationCheckFinished(result: Boolean)
    }
}
