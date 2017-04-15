package net.somethingdreadful.MAL.dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;


public class StatusPickerDialogFragment extends DialogFragment implements OnCheckedChangeListener {
    private boolean isAnime;
    private String currentStatus;

    @Override
    public AlertDialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(makeRatiobutton());
        builder.setTitle(R.string.dialog_title_status);
        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DetailView) getActivity()).onStatusDialogDismissed(currentStatus);
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        });

        return builder.create();
    }

    private View makeRatiobutton() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_status_picker, null);
        RadioGroup radio = (RadioGroup) view.findViewById(R.id.statusRadioGroup);
        isAnime = ((DetailView) getActivity()).isAnime;

        currentStatus = isAnime ? ((DetailView) getActivity()).animeRecord.getWatchedStatus() : ((DetailView) getActivity()).mangaRecord.getReadStatus();

        if ((Anime.STATUS_WATCHING.equals(currentStatus)) || (Manga.STATUS_READING.equals(currentStatus))) {
            radio.check(R.id.statusRadio_InProgress);
        }
        if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
            radio.check(R.id.statusRadio_Completed);
        }
        if (GenericRecord.STATUS_ONHOLD.equals(currentStatus)) {
            radio.check(R.id.statusRadio_OnHold);
        }
        if (GenericRecord.STATUS_DROPPED.equals(currentStatus)) {
            radio.check(R.id.statusRadio_Dropped);
        }
        if ((Anime.STATUS_PLANTOWATCH.equals(currentStatus)) || (Manga.STATUS_PLANTOREAD.equals(currentStatus))) {
            radio.check(R.id.statusRadio_Planned);
        }

        radio.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.statusRadio_InProgress:
                currentStatus = isAnime ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
                break;

            case R.id.statusRadio_Completed:
                currentStatus = GenericRecord.STATUS_COMPLETED;
                break;

            case R.id.statusRadio_OnHold:
                currentStatus = GenericRecord.STATUS_ONHOLD;
                break;

            case R.id.statusRadio_Dropped:
                currentStatus = GenericRecord.STATUS_DROPPED;
                break;
            case R.id.statusRadio_Planned:
                currentStatus = isAnime ? Anime.STATUS_PLANTOWATCH : Manga.STATUS_PLANTOREAD;
                break;
        }
    }
}