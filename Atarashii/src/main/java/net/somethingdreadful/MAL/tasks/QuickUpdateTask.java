package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver;

public class QuickUpdateTask extends AsyncTask<GenericRecord, Void, Boolean> {
    private boolean isAnime;
    private final Activity activity;
    private final IGFModel.IGFItem record;

    public QuickUpdateTask(boolean isAnime, Activity activity, IGFModel.IGFItem record) {
        this.isAnime = isAnime;
        this.activity = activity;
        this.record = record;
    }

    @Override
    protected Boolean doInBackground(GenericRecord... gr) {
        Anime anime = new Anime();
        Manga manga = new Manga();
        boolean error = false;
        boolean isNetworkAvailable = APIHelper.isNetworkAvailable(activity);
        ContentManager manager = new ContentManager(activity);

        if (!AccountService.isMAL() && isNetworkAvailable)
            manager.verifyAuthentication();

        try {
            if (isAnime) {
                anime = manager.getAnime(record.getId());
                if (anime.getWatchedEpisodes() != record.getProgressRaw()) {
                    anime.setWatchedEpisodes(record.getProgressRaw());
                } else if (!anime.getWatchedStatus().equals(record.getUserStatusRaw())) {
                    anime.setWatchedStatus(record.getUserStatusRaw());
                }
                AppLog.log(Log.INFO, "Atarashii", "QuickUpdateTask(Anime): got: id=" + record.getId() + ", count=" + record.getProgressRaw() + ", status=" + record.getUserStatusRaw());
            } else {
                manga = manager.getManga(record.getId());
                if (PrefManager.getUseSecondaryAmountsEnabled() && manga.getChaptersRead() != record.getProgressRaw()) {
                    manga.setChaptersRead(record.getProgressRaw());
                } else if (!PrefManager.getUseSecondaryAmountsEnabled() && manga.getVolumesRead() != record.getProgressRaw()) {
                    manga.setVolumesRead(record.getProgressRaw());
                } else if (!manga.getReadStatus().equals(record.getUserStatusRaw())) {
                    manga.setReadStatus(record.getUserStatusRaw());
                }
                AppLog.log(Log.INFO, "Atarashii", "QuickUpdateTask(Manga): got: id=" + record.getId() + ", count=" + record.getProgressRaw() + ", status=" + record.getUserStatusRaw());
            }

            // Sync details if there is network connection
            if (isNetworkAvailable) {
                if (isAnime) {
                    error = !manager.writeAnimeDetails(anime);
                } else {
                    error = !manager.writeMangaDetails(manga);
                }
            }
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "WriteDetailTask.QuickUpdateTask(): unknown error: " + e.getMessage());
            AppLog.logException(e);
            error = true;
        }

        // Save the records and mark as synced
        if (isAnime) {
            if (isNetworkAvailable && !error)
                anime.clearDirty();
            manager.saveAnimeToDatabase(anime);
        } else {
            if (isNetworkAvailable && !error)
                manga.clearDirty();
            manager.saveMangaToDatabase(manga);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // send broadcast for list updates
        Intent i = new Intent();
        i.setAction(RecordStatusUpdatedReceiver.RECV_IDENT);
        i.putExtra("type", isAnime ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }
}