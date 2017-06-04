package net.somethingdreadful.MAL.account

import android.accounts.*
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.PrefManager
import net.somethingdreadful.MAL.account.AccountType.AniList
import net.somethingdreadful.MAL.account.AccountType.MyAnimeList
import net.somethingdreadful.MAL.database.DatabaseHelper

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
        private var userAccount: Account? = null
        private var context: Context? = null
        /**
         * The account version will be used to peform
         */
        private val accountVersion = 3

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
                1, 2 // We added new base models to make loading easier, the user needs to log out (2.2 beta 1).
                -> deleteAccount()
                3 // The profile image is now saved in the settings
                -> {
                    val cManager = ContentManager(context)
                    if (!PrefManager.isCreated())
                        PrefManager.create(context)
                    val profile = cManager.profileFromDB
                    if (profile != null && profile.imageUrl != null) {
                        PrefManager.setProfileImage(profile.imageUrl)
                        PrefManager.commitChanges()
                    }
                }
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
                val username = getAccount()!!.name
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
            return AccountManager.get(context).getAccountsByType(getAccountType()).isNotEmpty()
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
                    System.exit(0)
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
        fun deleteAccount() {
            val accountManager = AccountManager.get(context)
            userAccount = null
            if (getAccount() != null)
                accountManager.removeAccount(getAccount(), null, null)
            accountType = null
        }

        /**
         * Add an account in the accountmanager.

         * @param username The username of the account that will be saved
         * *
         * @param password The password of the account that will be saved
         */
        fun addAccount(username: String, password: String, accountType: net.somethingdreadful.MAL.account.AccountType) {
            val accountManager = AccountManager.get(context)
            val account = Account(username, getAccountType())
            accountManager.addAccountExplicitly(account, password, null)
            accountManager.setUserData(account, "accountType", accountType.toString())
            accountManager.setUserData(account, "accountVersion", accountVersion.toString())
            AccountService.accountType = accountType
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
                    AppLog.log(Log.INFO, "Atarashii", "AccountService: The expire time could not be received.")
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
    }
}