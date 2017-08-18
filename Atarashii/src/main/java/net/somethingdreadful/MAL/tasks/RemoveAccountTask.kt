package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.account.AccountService
import java.util.*

class RemoveAccountTask(private val callback: removeAccountTaskListener, val activity: Activity, val allAccounts: Boolean) : AsyncTask<String, Void, ArrayList<AccountService.userAccount>?>() {

    override fun doInBackground(vararg params: String): ArrayList<AccountService.userAccount> {
        val cManager = ContentManager(activity)
        val accounts: ArrayList<AccountService.userAccount>?

        try {
            if (allAccounts) {
                // Remove all accounts, preferences and the entire DB
                AccountService.clearData()
            } else {
                // Remove account and the DB tables
                val accountID = params[0].toInt()
                AccountService.deleteAccount(accountID)
                cManager.removeAccountTable(accountID)
                cManager.removeAccount(accountID)

                accounts = cManager.accounts
                if (accounts != null)
                    return accounts
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    override fun onPostExecute(result: ArrayList<AccountService.userAccount>?) {
        callback.onAccountRemovedTaskFinished(result, allAccounts)
    }

    interface removeAccountTaskListener {
        fun onAccountRemovedTaskFinished(result: ArrayList<AccountService.userAccount>?, allAccounts: Boolean)
    }
}
