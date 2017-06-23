package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.EditText
import net.somethingdreadful.MAL.R

class InputDialogFragment : DialogFragment() {
    private var callback: onClickListener? = null
    private var input: EditText? = null

    fun setCallback(callback: onClickListener): InputDialogFragment {
        this.callback = callback
        return this
    }

    private fun createView(): View {
        val result = activity.layoutInflater.inflate(R.layout.dialog_update_nav_image, null)
        input = result.findViewById(R.id.editText) as EditText
        return result
    }

    override fun onCreateDialog(state: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, theme)
        builder.setTitle(arguments.getString("title"))
        builder.setView(createView())
        input!!.hint = arguments.getString("hint")
        input!!.setText(arguments.getString("message"))

        builder.setPositiveButton(R.string.dialog_label_update) { dialog, whichButton ->
            input!!.clearFocus()
            if (input!!.text.toString() != "") {
                callback!!.onPosInputButtonClicked(input!!.text.toString(), arguments.getInt("id"))
            }
            dismiss()
        }
        builder.setNeutralButton(R.string.dialog_label_remove) { dialog, whichButton ->
            input!!.clearFocus()
            callback!!.onNegInputButtonClicked(arguments.getInt("id"))
            dismiss()
        }
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }

        return builder.create()
    }

    /**
     * The interface for callback
     */
    interface onClickListener {
        fun onPosInputButtonClicked(text: String, id: Int)

        fun onNegInputButtonClicked(id: Int)
    }
}
