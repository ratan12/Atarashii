package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga

import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver

class WriteDetailTask(isAnime: Boolean, private val activity: Activity) : AsyncTask<GenericRecord, Void, Boolean>() {
    private var isAnime = true

    init {
        this.isAnime = isAnime
    }

    override fun doInBackground(vararg gr: GenericRecord): Boolean? {
        var error = false
        val isNetworkAvailable = APIHelper.isNetworkAvailable(activity)
        val manager = ContentManager(activity)

        if (!AccountService.isMAL && isNetworkAvailable)
            manager.verifyAuthentication()

        try {
            // Sync details if there is network connection
            if (isNetworkAvailable) {
                if (isAnime) {
                    error = !manager.writeAnimeDetails(gr[0] as Anime)
                } else {
                    error = !manager.writeMangaDetails(gr[0] as Manga)
                }
            }
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "WriteDetailTask.doInBackground(5, " + isAnime + "): unknown API error (?): " + e.message)
            AppLog.logException(e)
            error = true
        }

        // Records updated successfully and will be marked as done if it hasn't been removed
        if (isNetworkAvailable && !error && !gr[0].deleteFlag) {
            gr[0].clearDirty()
        }

        if (gr[0].deleteFlag) {
            // Delete record
            if (isAnime) {
                manager.deleteAnime(gr[0] as Anime)
            } else {
                manager.deleteManga(gr[0] as Manga)
            }
        } else {
            // Save the records
            if (isAnime) {
                manager.saveAnimeToDatabase(gr[0] as Anime)
            } else {
                manager.saveMangaToDatabase(gr[0] as Manga)
            }
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