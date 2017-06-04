package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log

import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.Theme
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule

class ScheduleTask(activity: Activity, private val forceRefresh: Boolean, private val callback: ScheduleTask.ScheduleTaskListener) : AsyncTask<String, Void, Schedule>() {
    private var activity: Activity? = null


    init {
        this.activity = activity
    }

    override fun doInBackground(vararg params: String): Schedule? {
        val isNetworkAvailable = APIHelper.isNetworkAvailable(activity)

        var taskResult: Schedule? = Schedule()
        val cManager = ContentManager(activity)
        if (!AccountService.isMAL && isNetworkAvailable)
            cManager.verifyAuthentication()

        try {
            if (forceRefresh) {
                if (isNetworkAvailable) {
                    taskResult = cManager.schedule
                    if (taskResult != null && !taskResult.isNull)
                        cManager.saveSchedule(taskResult)
                } else {
                    Theme.Snackbar(activity, R.string.toast_error_noConnectivity)
                }
            } else {
                taskResult = cManager.scheduleFromDB
                if (taskResult!!.isNull) { // there are no records
                    if (isNetworkAvailable) {
                        taskResult = cManager.schedule
                        if (taskResult != null && !taskResult.isNull)
                            cManager.saveSchedule(taskResult)
                    } else {
                        Theme.Snackbar(activity, R.string.toast_error_noConnectivity)
                    }
                }
            }
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "ScheduleTask.doInBackground(): " + e.message)
        }

        return taskResult
    }

    override fun onPostExecute(result: Schedule) {
        callback.onScheduleTaskFinished(result)
    }

    interface ScheduleTaskListener {
        fun onScheduleTaskFinished(result: Schedule)
    }
}
