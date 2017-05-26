package net.somethingdreadful.MAL.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.SearchActivity;


public class SearchIdDialogFragment extends DialogFragment {
    private int query;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        query = Integer.parseInt(((SearchActivity) activity).getQuery());
    }

    @Override
    public AlertDialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_id_search);
        builder.setMessage(R.string.dialog_message_id_search);

        builder.setPositiveButton(R.string.dialog_label_anime, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startDetails = new Intent(getActivity(), DetailView.class);
                startDetails.putExtra("recordID", query);
                startDetails.putExtra("recordType", true);
                startActivity(startDetails);
                dismiss();
                getActivity().finish();
            }
        });
        builder.setNeutralButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                getActivity().finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_label_manga, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startDetails = new Intent(getActivity(), DetailView.class);
                startDetails.putExtra("recordID", query);
                startDetails.putExtra("recordType", false);
                startActivity(startDetails);
                dismiss();
                getActivity().finish();
            }
        });

        return builder.create();
    }
}