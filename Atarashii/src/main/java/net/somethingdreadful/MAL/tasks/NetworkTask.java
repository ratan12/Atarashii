package net.somethingdreadful.MAL.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NetworkTask extends AsyncTask<String, Void, Object> {
    private TaskJob job;
    private boolean isAnime;
    private Activity activity = null;
    private Context context;
    private Bundle data;
    private NetworkTaskListener callback;
    private Object taskResult;
    private HashMap<String, String> Qdata;
    private final TaskJob[] arrayTasks = {TaskJob.GETLIST, TaskJob.FORCESYNC, TaskJob.GETMOSTPOPULAR, TaskJob.GETTOPRATED,
            TaskJob.GETJUSTADDED, TaskJob.GETUPCOMING, TaskJob.SEARCH, TaskJob.REVIEWS};


    public NetworkTask(TaskJob job, boolean isAnime, Activity activity, Bundle data, NetworkTaskListener callback) {
        if (job == null || activity == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = job;
        this.isAnime = isAnime;
        this.activity = activity;
        this.data = data;
        this.callback = callback;
    }

    public NetworkTask(Activity activity, boolean isAnime, HashMap<String, String> Qdata, NetworkTaskListener callback) {
        this.job = TaskJob.BROWSE;
        this.isAnime = isAnime;
        this.activity = activity;
        this.Qdata = Qdata;
        this.callback = callback;
    }

    public NetworkTask(boolean isAnime, Context context, NetworkTaskListener callback) {
        if (context == null)
            throw new IllegalArgumentException("job, type and context must not be null");
        this.job = TaskJob.FORCESYNC;
        this.isAnime = isAnime;
        this.context = context;
        this.data = new Bundle();
        this.callback = callback;
    }

    private Context getContext() {
        return context != null ? context : activity;
    }

    private boolean isAnimeTask() {
        return isAnime;
    }

    private boolean isArrayList() {
        return Arrays.asList(arrayTasks).contains(job);
    }

    @Override
    protected Object doInBackground(String... params) {
        boolean isNetworkAvailable = APIHelper.isNetworkAvailable(getContext());
        if (job == null) {
            AppLog.log(Log.ERROR, "Atarashii", "NetworkTask.doInBackground(): No job identifier, don't know what to do");
            return null;
        }

        if (!isNetworkAvailable && !job.equals(TaskJob.GETLIST) && !job.equals(TaskJob.GETDETAILS)) {
            if (activity != null)
                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
            return null;
        }

        int page = 1;
        if (data != null && data.containsKey("page"))
            page = data.getInt("page", 1);

        taskResult = null;
        ContentManager cManager = new ContentManager(activity != null ? activity : context);

        if (!AccountService.Companion.isMAL() && isNetworkAvailable)
            cManager.verifyAuthentication();

        try {
            switch (job) {
                case GETLIST:
                    if (params != null)
                        taskResult = isAnimeTask() ? cManager.getAnimeListFromDB(params[0], Integer.parseInt(params[1]), params[2]) : cManager.getMangaListFromDB(params[0], Integer.parseInt(params[1]), params[2]);
                    break;
                case BROWSE:
                    taskResult = isAnimeTask() ? cManager.getBrowseAnime(Qdata) : cManager.getBrowseManga(Qdata);
                    break;
                case GETFRIENDLIST:
                    if (params != null)
                        taskResult = isAnimeTask() ? cManager.getProfileAnimeList(params[0]) : cManager.getProfileMangaList(params[0]);
                    break;
                case FORCESYNC:
                    if (params != null) {
                        /* FORCESYNC may not require authentication if there are no dirty records to update, so a forced sync would even
                         * work if the password has changed, which would be strange for the user. So do an Auth-Check before syncing
                         *
                         * this will throw an RetrofitError-Exception if the credentials are wrong
                         */
                        if (AccountService.Companion.isMAL())
                            cManager.verifyAuthentication();

                        if (isAnimeTask()) {
                            cManager.cleanDirtyAnimeRecords();
                            cManager.downloadAnimeList(AccountService.Companion.getUsername());
                            taskResult = cManager.getAnimeListFromDB(params[0], Integer.parseInt(params[1]), params[2]);
                        } else {
                            cManager.cleanDirtyMangaRecords();
                            cManager.downloadMangaList(AccountService.Companion.getUsername());
                            taskResult = cManager.getMangaListFromDB(params[0], Integer.parseInt(params[1]), params[2]);
                        }
                    }
                    break;
                case GETMOSTPOPULAR:
                    taskResult = isAnimeTask() ? cManager.getMostPopularAnime(page) : cManager.getMostPopularManga(page);
                    break;
                case GETMOSTPOPULARS:
                    taskResult = isAnimeTask() ? cManager.getPopularSeasonAnime(page) : cManager.getPopularSeasonManga(page);
                    break;
                case GETMOSTPOPULARY:
                    taskResult = isAnimeTask() ? cManager.getPopularYearAnime(page) : cManager.getPopularYearManga(page);
                    break;
                case GETTOPRATED:
                    taskResult = isAnimeTask() ? cManager.getTopRatedAnime(page) : cManager.getTopRatedManga(page);
                    break;
                case GETTOPRATEDS:
                    taskResult = isAnimeTask() ? cManager.getTopSeasonAnime(page) : cManager.getTopSeasonManga(page);
                    break;
                case GETTOPRATEDY:
                    taskResult = isAnimeTask() ? cManager.getTopYearAnime(page) : cManager.getTopYearManga(page);
                    break;
                case GETJUSTADDED:
                    taskResult = isAnimeTask() ? cManager.getJustAddedAnime(page) : cManager.getJustAddedManga(page);
                    break;
                case GETUPCOMING:
                    taskResult = isAnimeTask() ? cManager.getUpcomingAnime(page) : cManager.getUpcomingManga(page);
                    break;
                case GETDETAILS:
                    if (data != null && data.containsKey("recordID"))
                        if (isAnimeTask()) {
                            // Get Anime from database
                            Anime record = cManager.getAnime(data.getInt("recordID", -1));

                            if (isNetworkAvailable) {
                                // Get records from the website
                                // Check for synopsis for relation.
                                if (record == null || record.getImageUrl() == null)
                                    record = cManager.getAnimeRecord(data.getInt("recordID", -1));

                                // Check if the record is on the animelist.
                                // after that load details if synopsis == null or else return the DB record
                                if (record != null && (record.getSynopsis() == null || params[0].equals("true")) && record.getWatchedStatus() != null) {
                                    AppLog.log(Log.INFO, "Atarashii", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, isAnime, record.getId()));
                                    taskResult = cManager.updateWithDetails(record.getId(), record);
                                } else {
                                    taskResult = record;
                                }
                            } else if (record != null) {
                                taskResult = record;
                            } else {
                                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                            }
                        } else {
                            // Get Manga from database
                            Manga record = cManager.getManga(data.getInt("recordID", -1));

                            if (isNetworkAvailable) {
                                // Get records from the website
                                if (record == null || record.getImageUrl() == null)
                                    record = cManager.getMangaRecord(data.getInt("recordID", -1));

                                // Check if the record is on the mangalist
                                // load details if synopsis == null or else return the DB record
                                if (record != null && (record.getSynopsis() == null || params[0].equals("true")) && record.getReadStatus() != null) {
                                    AppLog.log(Log.INFO, "Atarashii", String.format("NetworkTask.doInBackground(): TaskJob = %s & %sID = %s", job, isAnime, record.getId()));
                                    taskResult = cManager.updateWithDetails(record.getId(), record);
                                } else {
                                    taskResult = record;
                                }
                            } else if (record != null) {
                                taskResult = record;
                            } else {
                                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                            }
                        }
                    break;
                case SEARCH:
                    if (params != null)
                        taskResult = isAnimeTask() ? cManager.searchAnime(params[0], page) : cManager.searchManga(params[0], page);
                    break;
                case REVIEWS:
                    if (params != null)
                        taskResult = isAnimeTask() ? cManager.getAnimeReviews(Integer.parseInt(params[0]), page) : cManager.getMangaReviews(Integer.parseInt(params[0]), page);
                    break;
                case RECOMMENDATION:
                    if (params != null)
                        taskResult = isAnimeTask() ? cManager.getAnimeRecs(Integer.parseInt(params[0])) : cManager.getMangaRecs(Integer.parseInt(params[0]));
                    break;
                default:
                    AppLog.log(Log.ERROR, "Atarashii", "NetworkTask.doInBackground(): " + String.format("%s-task invalid job identifier %s", isAnime, job.name()));
            }
            /* if result is still null at this point there was no error but the API returned an empty result
             * (e. g. an empty anime-/mangalist), so create an empty list to let the callback know that
             * there was no error
             */
            if (taskResult == null)
                return isArrayList() ? new ArrayList<>() : null;
        } catch (Exception e) {
            AppLog.logTaskCrash("NetworkTask", "doInBackground(): " + String.format("%s-task error on job %s", isAnime, job.name()), e);
            return isArrayList() && !job.equals(TaskJob.FORCESYNC) && !job.equals(TaskJob.GETLIST) ? new ArrayList<>() : null;
        }
        return taskResult;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (callback != null) {
            if (result != null)
                callback.onNetworkTaskFinished(taskResult, job, isAnime);
            else
                callback.onNetworkTaskError(job);
        }
    }

    public interface NetworkTaskListener {
        void onNetworkTaskFinished(Object result, TaskJob job, boolean isAnime);

        void onNetworkTaskError(TaskJob job);
    }
}
