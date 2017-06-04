package net.somethingdreadful.MAL.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.R
import java.text.SimpleDateFormat
import java.util.*

class DatePickerDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var startDate: Boolean? = null
    private var mDateDialog: DatePickerDialog? = null
    private var callback: onDateSetListener? = null

    fun setCallback(callback: onDateSetListener): DatePickerDialogFragment {
        this.callback = callback
        return this
    }

    override fun onCreateDialog(state: Bundle): Dialog {
        startDate = arguments.getBoolean("startDate")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var current = Date()
        try {
            if (arguments.getString("current") != null && arguments.getString("current") != "0-00-00")
                current = sdf.parse(arguments.getString("current"))
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatePickerDialogFragment.onCreateDialog(): " + e.message)
            AppLog.logException(e)
        }

        val c = Calendar.getInstance()
        c.time = current
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        mDateDialog = DatePickerDialog(activity, this, year, month, day)

        mDateDialog!!.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok)) { dialog, which ->
            callback!!.onDateSet(startDate, mDateDialog!!.datePicker.year, mDateDialog!!.datePicker.month + 1, mDateDialog!!.datePicker.dayOfMonth)
            dismiss()
        }

        mDateDialog!!.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel)) { dialog, which -> dismiss() }

        mDateDialog!!.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.dialog_label_remove)) { dialog, which ->
            callback!!.onDateSet(startDate, 0, 0, 0)
            dismiss()
        }
        return mDateDialog as DatePickerDialog
    }

    override fun onDateSet(datePicker: DatePicker, i: Int, i1: Int, i2: Int) {}

    /**
     * The interface for callback
     */
    interface onDateSetListener {
        fun onDateSet(startDate: Boolean?, year: Int, month: Int, day: Int)
    }
}
