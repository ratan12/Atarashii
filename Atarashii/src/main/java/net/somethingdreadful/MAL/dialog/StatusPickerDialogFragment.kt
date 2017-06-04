package net.somethingdreadful.MAL.dialog

import android.app.AlertDialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import net.somethingdreadful.MAL.DetailView
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga


class StatusPickerDialogFragment : DialogFragment(), OnCheckedChangeListener {
    private var isAnime: Boolean = false
    private var currentStatus: String? = null

    override fun onCreateDialog(state: Bundle): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setView(makeRatiobutton())
        builder.setTitle(R.string.dialog_title_status)
        builder.setPositiveButton(R.string.dialog_label_update) { dialog, whichButton ->
            (activity as DetailView).onStatusDialogDismissed(currentStatus)
            dismiss()
        }
        builder.setNegativeButton(R.string.dialog_label_cancel) { dialog, whichButton -> dismiss() }

        return builder.create()
    }

    private fun makeRatiobutton(): View {
        val view = activity.layoutInflater.inflate(R.layout.dialog_status_picker, null)
        val radio = view.findViewById(R.id.statusRadioGroup) as RadioGroup
        isAnime = (activity as DetailView).isAnime

        currentStatus = if (isAnime) (activity as DetailView).animeRecord.watchedStatus else (activity as DetailView).mangaRecord.readStatus

        if (Anime.STATUS_WATCHING == currentStatus || Manga.STATUS_READING == currentStatus) {
            radio.check(R.id.statusRadio_InProgress)
        }
        if (GenericRecord.STATUS_COMPLETED == currentStatus) {
            radio.check(R.id.statusRadio_Completed)
        }
        if (GenericRecord.STATUS_ONHOLD == currentStatus) {
            radio.check(R.id.statusRadio_OnHold)
        }
        if (GenericRecord.STATUS_DROPPED == currentStatus) {
            radio.check(R.id.statusRadio_Dropped)
        }
        if (Anime.STATUS_PLANTOWATCH == currentStatus || Manga.STATUS_PLANTOREAD == currentStatus) {
            radio.check(R.id.statusRadio_Planned)
        }

        radio.setOnCheckedChangeListener(this)

        return view
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.statusRadio_InProgress -> currentStatus = if (isAnime) Anime.STATUS_WATCHING else Manga.STATUS_READING

            R.id.statusRadio_Completed -> currentStatus = GenericRecord.STATUS_COMPLETED

            R.id.statusRadio_OnHold -> currentStatus = GenericRecord.STATUS_ONHOLD

            R.id.statusRadio_Dropped -> currentStatus = GenericRecord.STATUS_DROPPED
            R.id.statusRadio_Planned -> currentStatus = if (isAnime) Anime.STATUS_PLANTOWATCH else Manga.STATUS_PLANTOREAD
        }
    }
}