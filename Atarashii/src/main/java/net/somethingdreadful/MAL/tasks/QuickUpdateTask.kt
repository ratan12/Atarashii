package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.PrefManager
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver

class QuickUpdateTask(private val isAnime: Boolean, private val activity: Activity, private val record: IGFModel.IGFItem) : AsyncTask<GenericRecord, Void, Boolean>() {

    override fun doInBackground(vararg gr: GenericRecord): Boolean? {
        var anime = Anime()
        var manga = Manga()
        var error = false
        val isNetworkAvailable = APIHelper.isNetworkAvailable(activity)
        val manager = ContentManager(activity)

        if (!AccountService.isMAL && isNetworkAvailable)
            manager.verifyAuthentication()

        try {
            if (isAnime) {
                anime = manager.getAnime(record.id)
                if (anime.watchedEpisodes != record.progressRaw) {
                    anime.setWatchedEpisodes(record.progressRaw)
                } else if (anime.watchedStatus != record.userStatusRaw) {
                    anime.setWatchedStatus(record.userStatusRaw)
                }
                AppLog.log(Log.INFO, "Atarashii", "QuickUpdateTask(Anime): got: id=" + record.id + ", count=" + record.progressRaw + ", status=" + record.userStatusRaw)
            } else {
                manga = manager.getManga(record.id)
                if (PrefManager.getUseSecondaryAmountsEnabled() && manga.chaptersRead != record.progressRaw) {
                    manga.setChaptersRead(record.progressRaw)
                } else if (!PrefManager.getUseSecondaryAmountsEnabled() && manga.volumesRead != record.progressRaw) {
                    manga.setVolumesRead(record.progressRaw)
                } else if (manga.readStatus != record.userStatusRaw) {
                    manga.setReadStatus(record.userStatusRaw)
                }
                AppLog.log(Log.INFO, "Atarashii", "QuickUpdateTask(Manga): got: id=" + record.id + ", count=" + record.progressRaw + ", status=" + record.userStatusRaw)
            }

            // Sync details if there is network connection
            if (isNetworkAvailable) {
                if (isAnime) {
                    error = !manager.writeAnimeDetails(anime)
                } else {
                    error = !manager.writeMangaDetails(manga)
                }
            }
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "WriteDetailTask.QuickUpdateTask(): unknown error: " + e.message)
            AppLog.logException(e)
            error = true
        }

        // Save the records and mark as synced
        if (isAnime) {
            if (isNetworkAvailable && !error)
                anime.clearDirty()
            manager.saveAnimeToDatabase(anime)
        } else {
            if (isNetworkAvailable && !error)
                manga.clearDirty()
            manager.saveMangaToDatabase(manga)
        }
        return null
    }

    override fun onPostExecute(aBoolean: Boolean?) {
        // send broadcast for list updates
        val i = Intent()
        i.action = RecordStatusUpdatedReceiver.RECV_IDENT
        i.putExtra("type", isAnime)
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i)
    }
}