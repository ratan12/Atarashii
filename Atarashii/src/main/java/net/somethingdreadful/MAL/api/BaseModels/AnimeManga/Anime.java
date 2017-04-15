package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import android.app.Activity;
import android.database.Cursor;
import android.text.TextUtils;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALModels.RecordStub;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Anime extends GenericRecord implements Serializable {

    /**
     * The amount of minutes how long an episode lasts.
     */
    @Getter
    @Setter
    private int duration;

    /**
     * Total number of episodes of the anime.
     * <p/>
     * This value is the number of episodes of the anime, or null if unknown.
     */
    @Setter
    @Getter
    private int episodes;

    /**
     * The video ID on youtube.
     * <p/>
     * Website: AniList
     */
    @Setter
    @Getter
    private String youtubeId;

    /**
     * The video URL on youtube.
     * <p/>
     * Website: AniList
     * TODO: Enable this feature in the interface
     */
    public String getYoutubeUrl() {
        return "https://www.youtube.com/watch?v=" + getYoutubeId();
    }

    /**
     * The Airing information about an anime.
     * <p/>
     * Website: AniList
     */
    @Getter
    @Setter
    private net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.Airing airing;

    /**
     * Opening themes of the record
     */
    @Setter
    @Getter
    private ArrayList<String> openingTheme;

    /**
     * Ending themes of the record
     */
    @Setter
    @Getter
    private ArrayList<String> endingTheme;

    /**
     * A list of producers for the anime
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<String> producers;

    /**
     * A list of characters.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> characterAnime;

    /**
     * A list of manga adaptations of this anime (or conversely, manga from which this anime is adapted).
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> mangaAdaptations;

    /**
     * A list of anime prequels of this anime.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> prequels;

    /**
     * A list of anime sequels of this anime.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> sequels;

    /**
     * A list of anime side stories of this anime.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> sideStories;

    /**
     * Parent story of this anime.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private RecordStub parentStory;

    /**
     * A list of spin-offs of this anime.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> spinOffs;

    /**
     * A list of summaries of this anime.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> summaries;

    /**
     * A list of other related animes.
     * <p/>
     * Website: MyanimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> other;

    /**
     * Personal watched status of the anime
     * <p/>
     * Defined string. Value will be one of watching, completed, on-hold, dropped, or plan to watch.
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private String watchedStatus;

    /**
     * Number of episodes watched by the user
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private int watchedEpisodes;

    /**
     * The date the user started watching the show
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private String watchingStart;

    /**
     * The date the user finished watching the show
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private String watchingEnd;

    /**
     * Storage type for the series
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private int storage;

    /**
     * The value for the storage chosen
     * <p/>
     * This number may either be the number of discs (for DVDs, VHS, etc) or size in GB for HD types
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private float storageValue;

    /**
     * Set if the user is rewatching the anime
     * <p/>
     * Website: MyanimeList
     */
    private boolean rewatching;

    /**
     * The number of times the user has re-watched the title. (Does not include the first time.)
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private int rewatchCount;

    /**
     * How much value the user thinks there is in rewatching the series.
     * <p/>
     * Website: MyanimeList
     */
    @Getter
    private int rewatchValue;

    /**
     * External links.
     */
    @Getter @Setter
    public externalLinks externalLinks;

    @Getter public static class externalLinks implements Serializable {
        @Setter String officialSite;
        @Setter String animeDB;
        @Setter String animeNewsNetwork;
        @Setter String wikipedia;
    }

    public void setWatchedStatus(String watchedStatus) {
        if (this.watchedStatus == null || !this.watchedStatus.equals(watchedStatus)) {
            this.watchedStatus = watchedStatus;
            if (!fromCursor) {
                addDirtyField("watchedStatus");
                checkProgress();
            }
        }
    }

    public void setWatchedEpisodes(int watchedEpisodes) {
        if (this.watchedEpisodes != watchedEpisodes) {
            this.watchedEpisodes = watchedEpisodes;
            if (!fromCursor) {
                addDirtyField("watchedEpisodes");
                checkProgress();
            }
        }
    }

    public void setWatchingStart(String watchingStart) {
        if (!fromCursor)
            addDirtyField("watchingStart");
        this.watchingStart = watchingStart;
    }

    public void setWatchingEnd(String watchingEnd) {
        if (!fromCursor)
            addDirtyField("watchingEnd");
        this.watchingEnd = watchingEnd;
    }

    public void setStorage(int storage) {
        if (!fromCursor)
            addDirtyField("storage");
        this.storage = storage;
    }

    public void setStorageValue(float storageValue) {
        if (!fromCursor)
            addDirtyField("storageValue");
        this.storageValue = storageValue;
    }

    public void setRewatching(boolean rewatching) {
        if (!fromCursor)
            addDirtyField("rewatching");
        this.rewatching = rewatching;
    }

    public void setRewatchCount(int rewatchCount) {
        if (!fromCursor)
            addDirtyField("rewatchCount");
        this.rewatchCount = rewatchCount;
    }

    public void setRewatchValue(int rewatchValue) {
        if (!fromCursor)
            addDirtyField("rewatchValue");
        this.rewatchValue = rewatchValue;
    }

    private void checkProgress() {
        boolean completed = false;
        boolean started = false;

        // Automatically set the status on completed
        if (getEpisodes() > 0 && getWatchedEpisodes() == getEpisodes() && !getDirty().contains("watchedStatus")) {
            setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            completed = true;
        }

        // Automatically set the max episode on completed
        if (getWatchedStatus() != null && getEpisodes() > 0 && getWatchedStatus().equals(GenericRecord.STATUS_COMPLETED) && !getDirty().contains("watchedEpisodes")) {
            setWatchedEpisodes(getEpisodes());
            completed = true;
        }

        if (completed) {
            // Automatically set the progress when the record has been finished
            if (getRewatching() || (getRewatchCount() > 0)) {
                setRewatchCount(getRewatchCount() + 1);
                setRewatching(false);
            }

            // Automatically set the end date on completed if it is empty
            if ((getWatchingEnd() == null || getWatchingEnd().equals("") || getWatchingEnd().equals("0-00-00")) && PrefManager.getAutoDateSetter()) {
                setWatchingEnd(DateTools.getCurrentDate());
            }
        }

        if (getWatchedStatus() != null && getWatchedStatus().equals(GenericRecord.STATUS_WATCHING) && getWatchedEpisodes() == 0 && !getDirty().contains("watchedEpisodes")) {
            started = true;
        }

        // Automatically set the progress when the episode 1 has been watched
        if (getWatchedStatus() != null && getWatchedStatus().equals(GenericRecord.STATUS_PLANTOWATCH) && getWatchedEpisodes() == 1 && !getDirty().contains("watchedStatus")) {
            setWatchedStatus(GenericRecord.STATUS_WATCHING);
            started = true;
        }

        // Automatically set the start date on start if it is empty
        if ((getWatchingStart() == null || getWatchingStart().equals("") || getWatchingStart().equals("0-00-00")) && PrefManager.getAutoDateSetter() && started) {
            setWatchingStart(DateTools.getCurrentDate());
        }
    }

    /**
     * Get the anime or manga classification translations
     */
    public String getClassificationString(Activity activity) {
        return getStringFromResourceArray(activity, R.array.classificationArray, getClassificationInt());
    }

    public String getUserStatusString(Activity activity) {
        return getStringFromResourceArray(activity, R.array.mediaStatus_User, getUserStatusInt(getWatchedStatus()));
    }

    /**
     * Get the anime or manga status translations
     */
    public String getStatusString(Activity activity) {
        int array;
        String[] fixedArray;
        if (AccountService.isMAL()) {
            array = R.array.animeStatus_MAL;
            fixedArray = activity.getResources().getStringArray(R.array.animeFixedStatus_MAL);
        } else {
            array = R.array.animeStatus_AL;
            fixedArray = activity.getResources().getStringArray(R.array.animeFixedStatus_AL);
        }
        return getStringFromResourceArray(activity, array, getStatusInt(fixedArray));
    }

    public Integer getClassificationInt() {
        String[] classification = {
                "G - All Ages",
                "PG - Children",
                "PG-13 - Teens 13 or older",
                "R - 17+ (violence \u0026 profanity)",
                "R+ - Mild Nudity",
                "Rx - Hentai"
        };
        return Arrays.asList(classification).indexOf(getClassification());
    }

    public String getProducersString() {
        return getProducers() != null ? TextUtils.join(", ", getProducers()) : "";
    }

    public void setWatchedStatus(int id) {
        setWatchedStatus(ContentManager.listSortFromInt(id, true));
    }

    public int getStatusInt(String[] fixedStatus) {
        return Arrays.asList(fixedStatus).indexOf(getStatus());
    }

    public ArrayList<RecordStub> getParentStoryArray() {
        ArrayList<RecordStub> recordStubs = new ArrayList<>();
        if (getParentStory() != null)
            recordStubs.add(getParentStory());
        return recordStubs;
    }

    public void setParentStoryArray(ArrayList<RecordStub> recordStubs) {
        if (recordStubs != null && recordStubs.size() > 0)
            setParentStory(recordStubs.get(0));
    }

    public boolean getRewatching() {
        return rewatching;
    }

    private void setRewatching(int cv) {
        rewatching = cv == 1;
    }

    public static Anime fromCursor(Cursor cursor) {
        List<String> columnNames = Arrays.asList(cursor.getColumnNames());
        Anime result = (Anime) GenericRecord.fromCursor(new Anime(), cursor, columnNames);
        result.airing = new net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.Airing();
        result.externalLinks = new externalLinks();

        result.setDuration(cursor.getInt(columnNames.indexOf("duration")));
        result.setEpisodes(cursor.getInt(columnNames.indexOf("episodes")));
        result.setYoutubeId(cursor.getString(columnNames.indexOf("youtubeId")));
        result.getAiring().setTime(cursor.getString(columnNames.indexOf("airingTime")));
        result.getAiring().setNextEpisode(cursor.getInt(columnNames.indexOf("nextEpisode")));
        result.setWatchedStatus(cursor.getString(columnNames.indexOf("watchedStatus")));
        result.setWatchedEpisodes(cursor.getInt(columnNames.indexOf("watchedEpisodes")));
        result.setWatchingStart(cursor.getString(columnNames.indexOf("watchingStart")));
        result.setWatchingEnd(cursor.getString(columnNames.indexOf("watchingEnd")));
        result.setStorage(cursor.getInt(columnNames.indexOf("storage")));
        result.setStorageValue(cursor.getFloat(columnNames.indexOf("storageValue")));
        result.setRewatching(cursor.getInt(columnNames.indexOf("rewatching")));
        result.setRewatchCount(cursor.getInt(columnNames.indexOf("rewatchCount")));
        result.setRewatchValue(cursor.getInt(columnNames.indexOf("rewatchValue")));
        result.getExternalLinks().setOfficialSite(cursor.getString(columnNames.indexOf("officialSite")));
        result.getExternalLinks().setAnimeDB(cursor.getString(columnNames.indexOf("animeDB")));
        result.getExternalLinks().setWikipedia(cursor.getString(columnNames.indexOf("wikipedia")));
        result.getExternalLinks().setAnimeNewsNetwork(cursor.getString(columnNames.indexOf("animeNewsNetwork")));
        return result;
    }
}
