package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.DialogFragment
import android.os.Bundle
import net.somethingdreadful.MAL.R

class ChooseDialogFragment : DialogFragment() {
    private var callback: onClickListener? = null

    override fun onCreateDialog(state: Bundle?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(arguments.getString("title"))
        builder.setMessage(arguments.getString("message"))
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }
        builder.setPositiveButton(arguments.getString("positive")) { dialog, whichButton ->
            callback!!.onPositiveButtonClicked()
            dismiss()
        }
        return builder.create()
    }

    fun setCallback(callback: onClickListener) {
        this.callback = callback
    }

    /**
     * The interface for callback
     */
    interface onClickListener {
        fun onPositiveButtonClicked()
    }
}