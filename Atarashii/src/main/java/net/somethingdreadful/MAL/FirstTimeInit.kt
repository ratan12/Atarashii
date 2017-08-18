package net.somethingdreadful.MAL

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.tasks.AuthenticationCheckTask
import net.somethingdreadful.MAL.tasks.NetworkTask
import net.somethingdreadful.MAL.tasks.TaskJob
import java.util.*

class FirstTimeInit : AppIntro(), AuthenticationCheckTask.AuthenticationCheckListener, NetworkTask.NetworkTaskListener {
    var isMAL = true
    var username: String = ""
    var password: String = ""
    private var dialog: ProgressDialog? = null
    private var loadedRecords = 0
    var firstTimeInitLogin: FirstTimeInitLogin = FirstTimeInitLogin.newInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name), getString(R.string.app_welcome), R.drawable.icon, resources.getColor(R.color.primary)))
        addSlide(FirstTimeInitChoose.newInstance(this))
        addSlide(firstTimeInitLogin)

        setBarColor(resources.getColor(R.color.accent))
        setSeparatorColor(resources.getColor(android.R.color.black))
        showSkipButton(false)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
    }

    fun pressedDone() {
        if (APIHelper.isNetworkAvailable(this)) {
            // Get username and password from the inputviews
            username = firstTimeInitLogin.input1.text.toString()
            password = firstTimeInitLogin.input2.text.toString()

            if (username != "" && password != "" && isMAL || username != "" && !isMAL) {
                // Create loading dialog
                dialog = ProgressDialog(this)
                dialog!!.isIndeterminate = true
                dialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                dialog!!.setTitle(getString(R.string.dialog_title_Verifying))
                dialog!!.setMessage(getString(R.string.dialog_message_Verifying))
                dialog!!.setCanceledOnTouchOutside(false)
                dialog!!.show()

                // Make auth check request
                if (isMAL)
                    AuthenticationCheckTask(this, this, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, password)
                else
                    AuthenticationCheckTask(this, this, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username)
            } else {
                Theme.Snackbar(this, R.string.toast_error_layout)
            }
        } else {
            Theme.Snackbar(this, R.string.toast_error_noConnectivity)
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        if (isMAL) {
            pressedDone()
        } else {
            Theme.Snackbar(this, R.string.toast_error_login)
        }
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }

    override fun onAuthenticationCheckFinished(isValid: Boolean) {
        if (isValid) {
            // Load anime and Manga records
            IGFModel.coverText = resources.getStringArray(R.array.igf_strings)
            loadedRecords = 0
            LoadRecords(true)
            LoadRecords(false)

            // Change dialog text
            dialog!!.setTitle(getString(R.string.dialog_title_records) + " (" + loadedRecords + "/2)")
            dialog!!.setMessage(getString(R.string.dialog_message_records))
        } else {
            dialog!!.dismiss()
            Theme.Snackbar(this, R.string.toast_error_VerifyProblem)
        }
    }

    override fun onNetworkTaskFinished(result: Any, job: TaskJob, isAnime: Boolean) {
        checkRecords()
    }

    override fun onNetworkTaskError(job: TaskJob) {
        checkRecords()
    }

    /**
     * Check if both records are loadedRecords and launch the main activity
     */
    private fun checkRecords() {
        loadedRecords++
        dialog!!.setTitle(getString(R.string.dialog_title_records) + " (" + loadedRecords + "/2)")
        if (loadedRecords == 2) {
            dialog!!.dismiss()
            val goHome = Intent(this, Home::class.java)
            startActivity(goHome)
            finish()
        }
    }

    /**
     * Create List loading request

     * @param isAnime Boolean if the animelist or mangalist should be loadedRecords
     */
    private fun LoadRecords(isAnime: Boolean) {
        val args = ArrayList<String>()
        args.add(ContentManager.listSortFromInt(0, isAnime))
        args.add(1.toString())
        args.add(false.toString())

        val networkTask = NetworkTask(TaskJob.FORCESYNC, isAnime, this, Bundle(), this)
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *args.toTypedArray())
    }
}

