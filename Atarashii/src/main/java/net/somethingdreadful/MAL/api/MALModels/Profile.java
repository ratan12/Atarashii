package net.somethingdreadful.MAL.api.MALModels;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

public class Profile implements Serializable {

    /**
     * A ProfileDetails object containing general information on the user
     */
    @Getter
    @Setter
    public Details details;

    /**
     * Fully qualified URL to the user's avatar image
     */
    @Getter
    @Setter
    @SerializedName("avatar_url")
    public String avatarUrl = "http://cdn.myanimelist.net/images/na.gif";

    /**
     * An AnimeStats object containing information on the user's anime statistics
     */
    @Getter
    @Setter
    @SerializedName("anime_stats")
    public AnimeStats animeStats;

    /**
     * A MangaStats object containing information on the user's manga statistics
     */
    @Getter
    @Setter
    @SerializedName("manga_stats")
    public MangaStats mangaStats;

    public String getSpecialAccesRank(String username) {
        if (getDetails() != null && getDetails().getAccessRank() != null) {
            String rank = getDetails().getAccessRank();
            if (rank.contains("IRC"))
                return "IRC";
            else if (rank.contains("News"))
                return "News Team";
            else if (rank.contains("Social"))
                return "SMT";
            else if (rank.contains("Mod"))
                return "Mod";
            else if (rank.contains("Admin"))
                return "Admin";
            else if (net.somethingdreadful.MAL.api.BaseModels.Profile.isDeveloper(username))
                return "Atarashii dev";
        }
        if (net.somethingdreadful.MAL.api.BaseModels.Profile.isDeveloper(username))
            return "Atarashii dev";
        return "";
    }

    public static class Details implements Serializable {
        /**
         * The date of when the user was last online.
         */
        @Getter
        @Setter
        @SerializedName("last_online")
        public String lastOnline;

        /**
         * The status of an user.
         * <p/>
         * This will indicate if an user is online or offline.
         * Example: "Online" or "Offline".
         */
        @Getter
        @Setter
        public String status;

        /**
         * The gender of an user.
         * <p/>
         * This will indicate if an user is a male or female
         * Example: "Male" or "Female"
         */
        @Getter
        @Setter
        public String gender;

        /**
         * The birthday of an user.
         */
        @Getter
        @Setter
        public String birthday;

        /**
         * The location of an user.
         */
        @Getter
        @Setter
        public String location;

        /**
         * The website of an user.
         */
        @Getter
        @Setter
        public String website;

        /**
         * The date of when the user joined MAL.
         */
        @Getter
        @Setter
        @SerializedName("join_date")
        public String joinDate;

        /**
         * The rank of an user.
         * <p/>
         * This is usually "Member" but in case of mods and admins it will display something else.
         */
        @Getter
        @Setter
        @SerializedName("access_rank")
        public String accessRank;

        /**
         * The amount of animelist views of an user.
         */
        @Getter
        @Setter
        @SerializedName("anime_list_views")
        public int animeListViews;

        /**
         * The amount of manga views of an user.
         */
        @Getter
        @Setter
        @SerializedName("manga_list_views")
        public int mangaListViews;

        /**
         * The amount of forum posts of an user
         */
        @Getter
        @Setter
        @SerializedName("forum_posts")
        public int forumPosts;

        /**
         * The amount of comments of an user
         */
        @Getter
        @Setter
        public int comments;

        public int getGenderInt() {
            String[] gender = {
                    "Female",
                    "Male"
            };
            return Arrays.asList(gender).indexOf(getGender());
        }
    }

    public static class AnimeStats implements Serializable {
        /**
         * The total amount of watched days.
         * <p/>
         * it will be counted as 24h
         */
        @Getter
        @Setter
        @SerializedName("time_days")
        public Double timeDays;

        /**
         * The amount of animes on your watching list
         */
        @Getter
        @Setter
        public int watching;

        /**
         * The amount of animes on your completed list
         */
        @Getter
        @Setter
        public int completed;

        /**
         * The amount of animes on your on hold list
         */
        @Getter
        @Setter
        @SerializedName("on_hold")
        public int onHold;

        /**
         * The amount of animes on your dropped list
         */
        @Getter
        @Setter
        public int dropped;

        /**
         * The amount of animes on your plan to watch list
         */
        @Getter
        @Setter
        @SerializedName("plan_to_watch")
        public int planToWatch;

        /**
         * The total amount of anime entries.
         */
        @Getter
        @Setter
        @SerializedName("total_entries")
        public int totalEntries;
    }

    public static class MangaStats implements Serializable {
        /**
         * The total amount of read days.
         * <p/>
         * it will be counted as 24h
         */
        @Getter
        @Setter
        @SerializedName("time_days")
        public Double timeDays;

        /**
         * The amount of mangas on your reading list
         */
        @Getter
        @Setter
        public int reading;

        /**
         * The amount of mangas on your completed list
         */
        @Getter
        @Setter
        public int completed;

        /**
         * The amount of mangas on your on hold list
         */
        @Getter
        @Setter
        @SerializedName("on_hold")
        public int onHold;

        /**
         * The amount of mangas on your dropped list
         */
        @Getter
        @Setter
        public int dropped;

        /**
         * The amount of mangas on your plan to read list
         */
        @Getter
        @Setter
        @SerializedName("plan_to_read")
        public int planToRead;

        /**
         * The total amount of manga entries.
         */
        @Getter
        @Setter
        @SerializedName("total_entries")
        public int totalEntries;
    }

    public net.somethingdreadful.MAL.api.BaseModels.Profile createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.Profile model = new net.somethingdreadful.MAL.api.BaseModels.Profile();
        model.setDetails(getDetails());
        model.setImageUrl(getAvatarUrl());
        model.setAnimeStats(getAnimeStats());
        model.setMangaStats(getMangaStats());
        return model;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.Profile> convertBaseList(ArrayList<Profile> MALprofiles) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.Profile> profiles = new ArrayList<>();
        for (Profile profile : MALprofiles) {
            profiles.add(profile.createBaseModel());
        }
        return profiles;
    }
}
