package net.somethingdreadful.MAL.cover

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import net.somethingdreadful.MAL.*
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.tasks.QuickUpdateTask
import java.util.*

class CoverAction(private val activity: Activity, isAnime: Boolean) {
    private var isAnime = true

    init {
        this.isAnime = isAnime
    }

    fun addProgress(item: IGFModel.IGFItem) {
        modifyProgress(item, +1)
    }

    fun subProgress(item: IGFModel.IGFItem) {
        modifyProgress(item, -1)
    }

    private fun modifyProgress(item: IGFModel.IGFItem, changeProgress: Int) {
        item.progressRaw = item.progressRaw + changeProgress
        QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun update(item: IGFModel.IGFItem) {
        QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun comProgress(item: IGFModel.IGFItem) {
        item.status = GenericRecord.STATUS_COMPLETED
        QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun addCoverItem(item: IGFModel.IGFItem) {
        item.status = ContentManager.listSortFromInt(PrefManager.getAddList(), isAnime)
        QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun openDetails(item: IGFModel.IGFItem) {
        if (APIHelper.isNetworkAvailable(activity)) {
            val startDetails = Intent(activity, DetailView::class.java)
            startDetails.putExtra("recordID", item.id)
            startDetails.putExtra("recordType", isAnime)
            activity.startActivity(startDetails)
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity)
        }
    }

    fun openPersonalDetails(item: IGFModel.IGFItem) {
        if (APIHelper.isNetworkAvailable(activity)) {
            val startDetails = Intent(activity, DetailView::class.java)
            startDetails.putExtra("recordID", item.id)
            startDetails.putExtra("recordType", isAnime)
            startDetails.putExtra("personal", true)
            activity.startActivity(startDetails)
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity)
        }
    }

    companion object {

        fun getpersonalIcons(): ArrayList<Int> {
            val actionIcons = ArrayList<Int>()
            actionIcons.add(R.drawable.ic_done_black_48px)             // completed
            actionIcons.add(R.drawable.ic_exposure_neg_1_black_48px)   // -1 progress
            actionIcons.add(R.drawable.ic_exposure_plus_1_black_48px)  // +1 progress
            actionIcons.add(R.drawable.ic_done_white_48px)
            actionIcons.add(R.drawable.ic_exposure_neg_1_black_48px)
            actionIcons.add(R.drawable.ic_exposure_plus_1_white_48px)
            return actionIcons
        }
    }
}
