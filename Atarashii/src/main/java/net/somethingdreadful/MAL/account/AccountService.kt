package net.somethingdreadful.MAL.account

import android.accounts.*
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.PrefManager
import net.somethingdreadful.MAL.account.AccountType.AniList
import net.somethingdreadful.MAL.account.AccountType.MyAnimeList
import net.somethingdreadful.MAL.database.DatabaseHelper
import java.util.*

class AccountService : Service() {
    private var mAuthenticator: Authenticator? = null

    override fun onCreate() {
        mAuthenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAuthenticator!!.iBinder
    }

    inner class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {

        override fun editProperties(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String): Bundle {
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun addAccount(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String, s2: String, strings: Array<String>, bundle: Bundle): Bundle {
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun confirmCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, bundle: Bundle): Bundle? {
            return null
        }

        @Throws(NetworkErrorException::class)
        override fun getAuthToken(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, s: String, bundle: Bundle): Bundle {
            throw UnsupportedOperationException()
        }

        override fun getAuthTokenLabel(s: String): String {
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun updateCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, s: String, bundle: Bundle): Bundle {
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun hasFeatures(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, strings: Array<String>): Bundle {
            throw UnsupportedOperationException()
        }
    }

    companion object {
        var accountType: net.somethingdreadful.MAL.account.AccountType? = null
        var accountId: Int = 0
        private var userAccount: Account? = null
        private var context: Context? = null
        /**
         * The account version will be used to peform
         */
        private val accountVersion = 4

        fun create(context: Context) {
            AccountService.context = context
        }

        /**
         * This is used for Account upgrade purpose
         */
        private fun onUpgrade(oldVersion: Int) {
            AppLog.log(Log.INFO, "Atarashii", "AccountService.onUpgrade(): Upgrading from " + oldVersion + " to " + accountVersion.toString() + ".")
            setAccountVersion()
            when (oldVersion + 1) {
                1, 2, 3, 4 -> deleteAccount()
            }
        }

        /**
         * Get the username of an account.

         * @return String The username
         */
        val username: String?
            get() {
                if (getAccount() == null)
                    return null
                val username = getAccount()!!.name.substring(0, getAccount()!!.name.indexOf("@"))
                AppLog.setUserName(username)
                return username
            }

        /**
         * Get the password of an account.

         * @return String The password
         */
        val password: String?
            get() {
                val account = getAccount() ?: return null
                val accountManager = AccountManager.get(context)
                return accountManager.getPassword(account)
            }

        private fun getAccountType(): String {
            return if (context!!.packageName.contains("beta")) ".beta.account.SyncAdapter.account" else ".account.SyncAdapter.account"
        }

        /**
         * Check if an account exists.

         * @param context The context
         * *
         * @return boolean if there is an account
         */
        fun AccountExists(context: Context): Boolean {
            getAccount()
            val doesExist: Boolean = AccountManager.get(context).getAccountsByType(getAccountType()).isNotEmpty()
            return doesExist
        }

        /**
         * Get an Account on the device.

         * @return Account The account
         */
        fun getAccount(): Account? {
            if (userAccount == null) {
                val accountManager = AccountManager.get(context)
                val myaccount = accountManager.getAccountsByType(getAccountType())
                var version: String? = accountVersion.toString()
                if (myaccount.isNotEmpty()) {
                    accountType = getAccountType(accountManager.getUserData(myaccount[0], "accountType"))
                    version = accountManager.getUserData(myaccount[0], "accountVersion")
                    if (version!!.toInt() >= 4)
                        accountId = accountManager.getUserData(myaccount[0], "accountId").toInt()
                    AppLog.setCrashData("Site", AccountService.accountType!!.toString())
                    AppLog.setCrashData("accountVersion", version)
                }
                userAccount = if (myaccount.isNotEmpty()) myaccount[0] else null
                if (version == null)
                    onUpgrade(1)
                else if (Integer.parseInt(version) != accountVersion)
                    onUpgrade(Integer.parseInt(version))
            }
            return userAccount
        }

        val isMAL: Boolean
            get() {
                getAccount()
                if (userAccount == null || accountType == null) {
                    AccountService.deleteAccount()
                }

                when (accountType) {
                    MyAnimeList -> return true
                    AniList -> return false
                }
                return false
            }

        /**
         * Get the authtoken with the given string.

         * @param type The authToken string
         * *
         * @return AccountType The type of account
         */
        private fun getAccountType(type: String): net.somethingdreadful.MAL.account.AccountType {
            if (net.somethingdreadful.MAL.account.AccountType.AniList.toString() == type)
                return AniList
            else
                return MyAnimeList
        }

        /**
         * Removes an account from the accountmanager.
         */
        fun deleteAccount(accountId: Int) {
            val accountManager = AccountManager.get(context)
            val myaccount = accountManager.getAccountsByType(getAccountType())
            AppLog.log(Log.INFO, "Atarashii", "AccountService: remove account "+ accountId + " found: " + myaccount.size)
            if (myaccount != null)
                for (account: Account in myaccount) {
                    val tempAccountID = accountManager.getUserData(account, "accountId").toInt()
                    if (tempAccountID == accountId) {
                        accountManager.removeAccount(account, null, null)
                        break
                    }
                }
        }

        /**
         * Removes an account from the accountmanager.
         */
        fun deleteAccount() {
            val accountManager = AccountManager.get(context)
            val myaccount = accountManager.getAccountsByType(getAccountType())
            userAccount = null
            accountType = null

            for (account: Account in myaccount) {
                accountManager.removeAccount(getAccount(), null, null)
            }
        }

        /**
         * Add an account in the accountmanager.

         * @param username The username of the account that will be saved
         * *
         * @param password The password of the account that will be saved
         */
        fun addAccount(accountId: Int, username: String, password: String, accountType: net.somethingdreadful.MAL.account.AccountType) {
            val accountManager = AccountManager.get(context)
            userAccount = Account(username + "@" + accountType, getAccountType())
            accountManager.addAccountExplicitly(userAccount, password, null)
            accountManager.setUserData(userAccount, "accountType", accountType.toString())
            accountManager.setUserData(userAccount, "accountId", accountId.toString())
            accountManager.setUserData(userAccount, "accountVersion", accountVersion.toString())
            AccountService.accountType = accountType
            AccountService.accountId = accountId
        }

        /**
         * Add an accesToken to the Account data.

         * @param token The AccesToken which should be stored
         * *
         * @param time  The time till the token will expire
         * *
         * @return String The token
         */
        fun setAccesToken(token: String, time: Long?): String {
            val accountManager = AccountManager.get(context)
            accountManager.setUserData(getAccount(), "accesToken", token)
            accountManager.setUserData(getAccount(), "accesTokenTime", java.lang.Long.toString(System.currentTimeMillis() / 1000 + (time!! - 60)))
            return token
        }

        /**
         * Get the accesToken.
         *
         *
         * Note: this method will return null if the accesToken is expired!

         * @return String accesToken
         */
        val accesToken: String?
            get() {
                val accountManager = AccountManager.get(context)
                val token = accountManager.getUserData(getAccount(), "accesToken")
                try {
                    val expireTime = java.lang.Long.parseLong(accountManager.getUserData(getAccount(), "accesTokenTime"))
                    val time = System.currentTimeMillis() / 1000
                    val timeLeft = expireTime - time
                    AppLog.log(Log.INFO, "Atarashii", "AccountService: The accestoken will expire in " + java.lang.Long.toString(timeLeft / 60) + " minutes.")
                    return if (timeLeft >= 0) token else null
                } catch (e: Exception) {
                    AppLog.log(Log.ERROR, "Atarashii", "AccountService: The expire time could not be received.")
                    return null
                }

            }

        /**
         * Set an auth token in the accountmanager.
         */
        private fun setAccountVersion() {
            if (userAccount != null) {
                val accountManager = AccountManager.get(context)
                accountManager.setUserData(userAccount, "accountVersion", accountVersion.toString())
            }
        }

        var refreshToken: String
            /**
             * get an refresh token in the accountmanager.
             */
            get() {
                val accountManager = AccountManager.get(context)
                return accountManager.getUserData(getAccount(), "refreshToken")
            }
            /**
             * Set an auth token in the accountmanager.

             * @param refreshToken auth token of the account that will be saved
             */
            set(refreshToken) {
                val accountManager = AccountManager.get(context)
                accountManager.setUserData(getAccount(), "refreshToken", refreshToken)
            }

        /**
         * Removes the userdata
         */
        fun clearData() {
            DatabaseHelper.deleteDatabase(context)
            PrefManager.clear()
            AccountService.deleteAccount()
        }

        fun setAccount(item: AccountService.userAccount) {
            val accountManager = AccountManager.get(context)
            val myaccount = accountManager.getAccountsByType(getAccountType())
            AppLog.log(Log.INFO, "Atarashii", "AccountService: accounts found: " + myaccount.size)
            if (myaccount != null) {
                for (account: Account in myaccount) {
                    val tempAccountID = accountManager.getUserData(account, "accountId").toInt()
                    val tempAccountType = getAccountType(accountManager.getUserData(account, "accountType"))
                    if (tempAccountID == item.id) {
                        AccountService.accountType = tempAccountType
                        AccountService.accountId = tempAccountID
                        AccountService.userAccount = account
                        break
                    }
                }
                AppLog.setCrashData("Site", AccountService.accountType!!.toString())
            }
        }
    }

    class userAccount {
        var id: Int = 0
        var username: String = ""
        var imageUrl: String = ""
        var website: AccountType = MyAnimeList

        fun create(cursor: Cursor): userAccount {
            val columnNames = Arrays.asList(*cursor.columnNames)
            id = (cursor.getInt(columnNames.indexOf(DatabaseHelper.COLUMN_ID)))
            username = cursor.getString(columnNames.indexOf("username"))
            imageUrl = cursor.getString(columnNames.indexOf("imageUrl"))
            website = if (cursor.getInt(columnNames.indexOf("website")) == 0) MyAnimeList else AniList
            return this
        }
    }
}