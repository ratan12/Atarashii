package net.somethingdreadful.MAL.cover;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuickActionsDialog extends DialogFragment {
    private QuickActionsListener quickActionsListener;
    private Activity activity;
    private IGFModel.IGFItem igfItem;
    private boolean isRecordUpdated = false;
    private boolean isAnime;
    @BindView(R.id.coverdialog_title) TextView title;
    @BindView(R.id.coverdialog_progress) TextView progress;

    public static QuickActionsDialog newInstance(Activity activity, boolean isAnime, QuickActionsListener quickActionsListener, IGFModel.IGFItem igfItem) {
        QuickActionsDialog quickActionsDialog = new QuickActionsDialog();
        quickActionsDialog.quickActionsListener = quickActionsListener;
        quickActionsDialog.show(activity.getFragmentManager(), "quickActionsDialog");
        quickActionsDialog.activity = activity;
        quickActionsDialog.igfItem = igfItem;
        quickActionsDialog.isAnime = isAnime;
        return quickActionsDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cover_quickactiondialog, container, false);
        ButterKnife.bind(this, view);
        title.setText(igfItem.getTitle());
        progress.setText(igfItem.getShortDetails());
        return view;
    }

    @Override
    public void onPause() {
        if (isRecordUpdated)
            new CoverAction(activity, isAnime).update(igfItem);
        super.onPause();
    }

    @OnClick({R.id.mainLayout, R.id.fab_dismiss})
    void dismissClick() {
        quickActionsListener.onDismiss();
        dismiss();
    }

    private void updateProgress(int count) {
        if (igfItem.getProgressRawMax() != 0)
            igfItem.setProgressRaw(igfItem.getProgressRaw() + count);
        if (igfItem.getProgressRawMax() < igfItem.getProgressRaw() + count)
            igfItem.setProgressRaw(igfItem.getProgressRawMax());
        if (igfItem.getProgressRaw() + count < 0)
            igfItem.setProgressRaw(0);
        igfItem.refreshDetails();
        progress.setText(igfItem.getShortDetails());
        isRecordUpdated = true;
    }

    @OnClick(R.id.fab_plusone)
    void plusOneProgress() {
        updateProgress(1);
    }

    @OnClick(R.id.fab_plusfive)
    void plusTwoProgress() {
        updateProgress(5);
    }

    @OnClick(R.id.fab_negone)
    void negOneProgress() {
        updateProgress(-1);
    }

    @OnClick(R.id.fab_negfive)
    void negTwoProgress() {
        updateProgress(-5);
    }


    @OnClick(R.id.fab_copytitle)
    void copyTitle() {
        android.content.ClipboardManager clipBoard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clipData = android.content.ClipData.newPlainText("Atarashii", igfItem.getTitle());
        clipBoard.setPrimaryClip(clipData);
        dismiss();
        Theme.Snackbar(activity, R.string.toast_info_Copied);
    }

    @OnClick(R.id.fab_markdone)
    void markAsDone() {
        new CoverAction(activity, isAnime).comProgress(igfItem);
        dismiss();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onStart();
    }

    /**
     * The interface for callback
     */
    interface QuickActionsListener {
        void onDismiss();
    }
}