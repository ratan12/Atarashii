package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.account.AccountService
import java.util.*

class AccountTask(private val callback: accountTaskListener, val activity: Activity) : AsyncTask<String, Void, ArrayList<AccountService.userAccount>?>() {

    override fun doInBackground(vararg params: String): ArrayList<AccountService.userAccount> {
        val cManager = ContentManager(activity)
        val accounts: ArrayList<AccountService.userAccount>?

        try {
            accounts = cManager.accounts
            if (accounts != null)
                return accounts
        } catch (e: Exception) {
            AppLog.logTaskCrash("AccountTask", "doInBackground(5): task unknown API error (?)", e)
        }
        return ArrayList()
    }

    override fun onPostExecute(result: ArrayList<AccountService.userAccount>?) {
        callback.onAccountTaskFinished(result)
    }

    interface accountTaskListener {
        fun onAccountTaskFinished(result: ArrayList<AccountService.userAccount>?)
    }
}
