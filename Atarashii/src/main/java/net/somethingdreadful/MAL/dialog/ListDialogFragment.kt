package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle

import net.somethingdreadful.MAL.R

class ListDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    private var array: IntArray? = null
    private var selectedItem = -1
    private var callback: onUpdateClickListener? = null

    override fun onCreateDialog(state: Bundle): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(arguments.getString("title"))
        array = resources.getIntArray(arguments.getInt("intArray"))
        builder.setSingleChoiceItems(arguments.getInt("stringArray"), arguments.getInt("current"), this)
        builder.setPositiveButton(R.string.dialog_label_update) { dialog, whichButton ->
            if (selectedItem != -1)
                callback!!.onUpdated(selectedItem, arguments.getInt("id"))
            dismiss()
        }
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }
        return builder.create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        selectedItem = array!![which]
    }

    /**
     * The interface for callback
     */
    interface onUpdateClickListener {
        fun onUpdated(number: Int, id: Int)
    }

    /**
     * Set the Callback for update purpose.

     * @param callback The activity/fragment where the callback is located
     * *
     * @return ListDialogFragment This will return the dialog itself to make init simple
     */
    fun setOnSendClickListener(callback: onUpdateClickListener): ListDialogFragment {
        this.callback = callback
        return this
    }
}