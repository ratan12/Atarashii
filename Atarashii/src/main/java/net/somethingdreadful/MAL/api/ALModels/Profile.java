package net.somethingdreadful.MAL.api.ALModels;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.PrefManager;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Profile implements Serializable {
    @Getter
    @Setter
    @SerializedName("display_name")
    public String displayName;

    /**
     * Watched animeTime in minutes
     */
    @Getter
    @Setter
    @SerializedName("anime_time")
    public int animeTime;

    /**
     * Read mangaChapters
     */
    @Getter
    @Setter
    @SerializedName("manga_chap")
    public int mangaChapters;

    /**
     * Info about the user available in the profile
     */
    @Getter
    @Setter
    public String about;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("list_order")
    public int listOrder;
    @Getter
    @Setter
    @SerializedName("adult_content")
    public boolean adultContent;
    @Getter
    @Setter
    public boolean following;
    @Getter
    @Setter
    @SerializedName("image_url_lge")
    public String imageUrl;
    @Getter
    @Setter
    @SerializedName("image_url_med")
    public String imageUrlMed;
    @Getter
    @Setter
    @SerializedName("image_url_banner")
    public String imageUrlBanner;

    /**
     * The title titleLanguage the users prefers like romanji, english or japanese
     */
    @Getter
    @Setter
    @SerializedName("title_language")
    public String titleLanguage;
    @Getter
    @Setter
    @SerializedName("score_type")
    public int scoreType = -1;
    @Getter
    @Setter
    @SerializedName("custom_list_anime")
    public ArrayList<String> customAnime;
    @Getter
    @Setter
    @SerializedName("custom_list_manga")
    public ArrayList<String> customManga;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("advanced_rating")
    public boolean advancedRating;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("advanced_rating_names")
    public ArrayList<String> advancedRatingNames;
    @Getter
    @Setter
    public int notifications;

    public net.somethingdreadful.MAL.api.BaseModels.Profile createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.Profile model = new net.somethingdreadful.MAL.api.BaseModels.Profile();
        model.setUsername(getDisplayName());
        model.setAbout(getAbout());
        model.setImageUrl(getImageUrl());
        model.setImageUrlBanner(getImageUrlBanner());
        model.setScoreType(getScoreType());
        model.setCustomAnime(getCustomAnime());
        model.setCustomManga(getCustomManga());
        model.setNotifications(getNotifications());
        model.setAnimeStats(new net.somethingdreadful.MAL.api.MALModels.Profile.AnimeStats());
        model.setMangaStats(new net.somethingdreadful.MAL.api.MALModels.Profile.MangaStats());
        model.getAnimeStats().setTimeDays(Double.valueOf((new DecimalFormat("#.##")).format((double) getAnimeTime() / 60 / 24).replace(",", ".")));
        model.getMangaStats().setCompleted(getMangaChapters());

        if (scoreType != -1) {
            PrefManager.setScoreType(scoreType);
            PrefManager.commitChanges();
            AppLog.log(Log.INFO, "Profile.createBaseModel()", "scoreType:" + scoreType);
        }
        return model;
    }
}
