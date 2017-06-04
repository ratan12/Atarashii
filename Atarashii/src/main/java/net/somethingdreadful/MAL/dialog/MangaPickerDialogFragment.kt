package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import net.somethingdreadful.MAL.DetailView
import net.somethingdreadful.MAL.R

class MangaPickerDialogFragment : DialogFragment() {
    private var chapterPicker: NumberPicker? = null
    private var volumePicker: NumberPicker? = null

    private fun makeNumberPicker(): View {
        val view = activity.layoutInflater.inflate(R.layout.dialog_manga_picker, null)

        val volumesTotal = (activity as DetailView).mangaRecord.volumes
        val volumesRead = (activity as DetailView).mangaRecord.volumesRead
        val chaptersTotal = (activity as DetailView).mangaRecord.chapters
        val chaptersRead = (activity as DetailView).mangaRecord.chaptersRead

        chapterPicker = view.findViewById(R.id.chapterPicker) as NumberPicker
        volumePicker = view.findViewById(R.id.volumePicker) as NumberPicker
        chapterPicker!!.minValue = 0
        volumePicker!!.minValue = 0

        if (chaptersTotal != 0) {
            chapterPicker!!.maxValue = chaptersTotal
        } else {
            chapterPicker!!.maxValue = 9999
        }

        if (volumesTotal != 0) {
            volumePicker!!.maxValue = volumesTotal
        } else {
            volumePicker!!.maxValue = 9999
        }

        chapterPicker!!.value = chaptersRead
        volumePicker!!.value = volumesRead
        return view
    }

    override fun onCreateDialog(state: Bundle): Dialog {
        val builder = AlertDialog.Builder(activity, theme)
        builder.setView(makeNumberPicker())
        builder.setTitle(R.string.dialog_title_read_update)
        builder.setPositiveButton(R.string.dialog_label_update) { dialog, whichButton ->
            chapterPicker!!.clearFocus()
            volumePicker!!.clearFocus()
            (activity as DetailView).onMangaDialogDismissed(chapterPicker!!.value, volumePicker!!.value)
            dismiss()
        }
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }

        return builder.create()
    }
}