package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.PrefManager
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.Theme
import net.somethingdreadful.MAL.account.AccountService

class NumberPickerDialogFragment : DialogFragment() {
    private var numberPicker: NumberPicker? = null
    private var numberInput: EditText? = null
    private var callback: onUpdateClickListener? = null
    private var inputScore = false

    private fun makeNumberPicker(): View {
        val view = activity.layoutInflater.inflate(R.layout.dialog_episode_picker, null)
        val max = getValue("max")
        val min = getValue("min")
        val current = getValue("current")

        numberInput = view.findViewById(R.id.numberInput) as EditText
        numberPicker = view.findViewById(R.id.numberPicker) as NumberPicker

        if (!inputScore) {
            numberPicker!!.maxValue = if (max != 0) max else 999
            numberPicker!!.minValue = if (min != 0) min else 0
            if (!AccountService.isMAL && isRating) {
                val score = Theme.getDisplayScore(current.toFloat())
                numberPicker!!.value = if (score == "?") 0 else Integer.parseInt(score)
            } else {
                numberPicker!!.value = current
            }
            numberInput!!.visibility = View.GONE
        } else {
            numberInput!!.setText(Theme.getDisplayScore(current.toFloat()))
            if (PrefManager.getScoreType() == 4)
                numberInput!!.inputType = InputType.TYPE_CLASS_TEXT
            numberPicker!!.visibility = View.GONE
        }
        return view
    }

    override fun onCreateDialog(state: Bundle): Dialog {
        val builder = AlertDialog.Builder(activity, theme)
        builder.setView(makeNumberPicker())
        builder.setTitle(arguments.getString("title"))
        builder.setPositiveButton(R.string.dialog_label_update) { dialog, whichButton ->
            numberPicker!!.clearFocus()
            numberInput!!.clearFocus()
            if (!AccountService.isMAL && isRating) {
                val value = Theme.getRawScore(if (!inputScore) numberPicker!!.value.toString() else numberInput!!.text.toString())
                callback!!.onUpdated(value, arguments.getInt("id"))
            } else
                callback!!.onUpdated(numberPicker!!.value, arguments.getInt("id"))
            dismiss()
        }
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }

        return builder.create()
    }

    private val isRating: Boolean
        get() = arguments.getInt("id") == R.id.scorePanel

    /**
     * Get the integer from an argument.

     * @param key The argument name
     * *
     * @return int The number of the argument
     */
    private fun getValue(key: String): Int {
        try {
            if (arguments.getInt("id") == R.id.scorePanel && PrefManager.getScoreType() == 4 && PrefManager.getScoreType() == 3)
                inputScore = true
            return arguments.getInt(key)
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "EpisodesPickerDialogFragment.makeNumberPicker(" + key + "): " + e.message)
            return 0
        }

    }

    /**
     * Set the Callback for update purpose.

     * @param callback The activity/fragment where the callback is located
     * *
     * @return NumberPickerDialogFragment This will return the dialog itself to make init simple
     */
    fun setOnSendClickListener(callback: onUpdateClickListener): NumberPickerDialogFragment {
        this.callback = callback
        return this
    }

    /**
     * The interface for callback
     */
    interface onUpdateClickListener {
        fun onUpdated(number: Int, id: Int)
    }
}