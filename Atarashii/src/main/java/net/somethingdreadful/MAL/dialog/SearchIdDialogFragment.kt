package net.somethingdreadful.MAL.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.DialogFragment
import android.content.Intent
import android.os.Bundle
import net.somethingdreadful.MAL.DetailView
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.SearchActivity


class SearchIdDialogFragment : DialogFragment() {
    private var query: Int = 0

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        query = Integer.parseInt((activity as SearchActivity).query)
    }

    override fun onCreateDialog(state: Bundle): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.dialog_title_id_search)
        builder.setMessage(R.string.dialog_message_id_search)

        builder.setPositiveButton(R.string.dialog_label_anime) { dialog, which ->
            val startDetails = Intent(activity, DetailView::class.java)
            startDetails.putExtra("recordID", query)
            startDetails.putExtra("recordType", true)
            startActivity(startDetails)
            dismiss()
            activity.finish()
        }
        builder.setNeutralButton(R.string.dialog_label_cancel) { dialog, which ->
            dismiss()
            activity.finish()
        }
        builder.setNegativeButton(R.string.dialog_label_manga) { dialog, which ->
            val startDetails = Intent(activity, DetailView::class.java)
            startDetails.putExtra("recordID", query)
            startDetails.putExtra("recordType", false)
            startActivity(startDetails)
            dismiss()
            activity.finish()
        }

        return builder.create()
    }
}