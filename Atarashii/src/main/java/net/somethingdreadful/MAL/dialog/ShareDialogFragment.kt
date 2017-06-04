package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.account.AccountService

class ShareDialogFragment : DialogFragment() {
    private var title: String? = null
    private var share: Boolean = false

    override fun onCreateDialog(state: Bundle): Dialog {
        val builder = AlertDialog.Builder(activity, theme)
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)

        title = arguments.getString("title")
        share = arguments.getBoolean("share")

        if (share) {
            builder.setTitle(R.string.dialog_title_share)
            builder.setMessage(R.string.dialog_message_share)
            sharingIntent.type = "text/plain"
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        } else {
            builder.setTitle(R.string.dialog_title_view)
            builder.setMessage(R.string.dialog_message_view)
        }

        builder.setPositiveButton(R.string.dialog_label_animelist) { dialog, which ->
            if (share) {
                sharingIntent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_animelist)
                        .replace("\$name;", title!!)
                        .replace("\$username;", AccountService.username!!))
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)))
            } else {
                val mallisturlanime = Uri.parse(websiteURL + "animelist/" + title)
                startActivity(Intent(Intent.ACTION_VIEW, mallisturlanime))
            }
        }
        builder.setNeutralButton(R.string.dialog_label_cancel) { dialog, which -> }
        builder.setNegativeButton(R.string.dialog_label_mangalist) { dialog, which ->
            if (share) {
                sharingIntent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_mangalist)
                        .replace("\$name;", title!!)
                        .replace("\$username;", AccountService.username!!))
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)))
            } else {
                val mallisturlmanga = Uri.parse(websiteURL + "mangalist/" + title)
                startActivity(Intent(Intent.ACTION_VIEW, mallisturlmanga))
            }
        }

        return builder.create()
    }

    private val websiteURL: String
        get() = if (AccountService.isMAL) "https://myanimelist.net/" else "http://anilist.co/"
}