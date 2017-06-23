package net.somethingdreadful.MAL

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.tasks.AuthenticationCheckTask
import net.somethingdreadful.MAL.tasks.NetworkTask
import net.somethingdreadful.MAL.tasks.TaskJob
import java.util.*

class AddAccount : AppIntro(), AuthenticationCheckTask.AuthenticationCheckListener, NetworkTask.NetworkTaskListener {
    var isMAL = true
    var username: String = ""
    var password: String = ""
    private var dialog: ProgressDialog? = null
    private var loadedRecords = 0
    var AddAccountLogin: FirstTimeInitLogin = FirstTimeInitLogin.newInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(FirstTimeInitChoose.newInstance(this))
        addSlide(AddAccountLogin)

        setBarColor(resources.getColor(R.color.accent))
        setSeparatorColor(resources.getColor(android.R.color.black))
        showSkipButton(false)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        if (APIHelper.isNetworkAvailable(this)) {
            // Get username and password from the inputviews
            if (AddAccountLogin.input1 != null && AddAccountLogin.input2 != null) {
                username = AddAccountLogin.input1.text.toString()
                password = AddAccountLogin.input2.text.toString()

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
                    AuthenticationCheckTask(this, this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, password)
                else
                    AuthenticationCheckTask(this, this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username)
            } else {
                dialog!!.dismiss()
                Theme.Snackbar(this, R.string.toast_error_layout)
            }
        } else {
            dialog!!.dismiss()
            Theme.Snackbar(this, R.string.toast_error_noConnectivity)
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
