package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import net.somethingdreadful.MAL.R
import java.util.*

class CustomListDialogFragment : DialogFragment(), DialogInterface.OnMultiChoiceClickListener {
    private var callback: onUpdateClickListener? = null
    private var array = ArrayList<String>()
    private var resArray: Array<String>? = null

    override fun onCreateDialog(state: Bundle): Dialog {
        val builder = AlertDialog.Builder(activity)
        resArray = arguments.getStringArray("all")
        val checkedItems = BooleanArray(resArray!!.size)
        if (arguments.containsKey("current")) {
            array = arguments.getStringArrayList("current")
            for (n in resArray!!.indices) {
                checkedItems[n] = array.contains(resArray!![n])
            }
        }
        builder.setTitle(resources.getString(R.string.card_content_genres))
        builder.setMultiChoiceItems(resArray, checkedItems, this)
        builder.setPositiveButton(R.string.dialog_label_update) { dialog, whichButton ->
            var result = ""
            for (aResArray in resArray!!) {
                result += if (array.contains(aResArray)) 1 else 0
            }
            for (i in result.length..14) {
                result += "0"
            }
            callback!!.onUpdated(result, arguments.getInt("id"))
            dismiss()
        }
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }
        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int, b: Boolean) {
        if (b)
            array.add(resArray!![i])
        else
            array.remove(resArray!![i])
    }

    /**
     * The interface for callback
     */
    interface onUpdateClickListener {
        fun onUpdated(result: String, id: Int)
    }

    /**
     * Set the Callback for update purpose.

     * @param callback The activity/fragment where the callback is located
     * *
     * @return ListDialogFragment This will return the dialog itself to make init simple
     */
    fun setOnSendClickListener(callback: onUpdateClickListener): CustomListDialogFragment {
        this.callback = callback
        return this
    }
}