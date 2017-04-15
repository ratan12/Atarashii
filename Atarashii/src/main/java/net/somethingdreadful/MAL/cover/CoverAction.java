package net.somethingdreadful.MAL.cover;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.tasks.QuickUpdateTask;

import java.util.ArrayList;

public class CoverAction {
    private Activity activity;
    private boolean isAnime = true;

    public CoverAction(Activity activity, boolean isAnime) {
        this.activity = activity;
        this.isAnime = isAnime;
    }

    static ArrayList<Integer> getpersonalIcons(){
        ArrayList<Integer> actionIcons = new ArrayList<>();
        actionIcons.add(R.drawable.ic_done_black_48px);             // completed
        actionIcons.add(R.drawable.ic_exposure_neg_1_black_48px);   // -1 progress
        actionIcons.add(R.drawable.ic_exposure_plus_1_black_48px);  // +1 progress
        actionIcons.add(R.drawable.ic_done_white_48px);
        actionIcons.add(R.drawable.ic_exposure_neg_1_black_48px);
        actionIcons.add(R.drawable.ic_exposure_plus_1_white_48px);
        return actionIcons;
    }

    public void addProgress(IGFModel.IGFItem item){
        modifyProgress(item, +1);
    }
    public void subProgress(IGFModel.IGFItem item){
        modifyProgress(item, -1);
    }

    private void modifyProgress(IGFModel.IGFItem item, int changeProgress) {
        item.setProgressRaw(item.getProgressRaw() + changeProgress);
        new QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void comProgress(IGFModel.IGFItem item) {
        item.setStatus(GenericRecord.STATUS_COMPLETED);
        new QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void addCoverItem(IGFModel.IGFItem item) {
        item.setStatus(ContentManager.listSortFromInt(PrefManager.getAddList(), isAnime));
        new QuickUpdateTask(isAnime, activity, item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void openDetails(IGFModel.IGFItem item) {
        if (APIHelper.isNetworkAvailable(activity)) {
            Intent startDetails = new Intent(activity, DetailView.class);
            startDetails.putExtra("recordID", item.getId());
            startDetails.putExtra("recordType", isAnime);
            activity.startActivity(startDetails);
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
        }
    }
}
