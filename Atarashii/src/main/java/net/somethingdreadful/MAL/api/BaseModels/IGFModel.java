package net.somethingdreadful.MAL.api.BaseModels;

import android.database.Cursor;
import android.net.Uri;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.database.DatabaseHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class IGFModel implements Serializable {
    @Getter
    ArrayList<IGFItem> titles = new ArrayList<>();
    @Getter
    ArrayList<String> fastScrollText = new ArrayList<>();
    int sortType;

    @Setter public static String[] coverText;

    public IGFModel(int sortType) {
        this.sortType = sortType;
    }

    public class IGFItem {
        /**
         * The record id
         */
        @Getter
        @Setter public int id;

        /**
         * The profile image of the user
         */
        @Getter
        public String imageUrl;
        @Getter
        public ImageRequest[] imageUrlArray;

        /**
         * The names of the anime/manga
         */
        @Getter
        @Setter
        public String title;


        /**
         * The names of the custom manga list.
         */
        @Getter
        @Setter
        public String status;
        @Getter
        @Setter
        public String statusRaw;
        @Getter
        @Setter
        public String userStatusRaw;

        /**
         * Short details
         */
        @Getter
        private String shortDetails;

        /**
         * The profile banner.
         */
        @Getter
        @Setter
        public String score;
        @Getter
        @Setter
        public int scoreRaw;

        /**
         * The type of the record
         */
        @Getter
        @Setter
        public String typeRaw;

        /**
         * The number of notifications
         */
        @Getter
        @Setter
        public String progress;
        @Getter
        @Setter
        public int progressRaw;
        @Getter
        @Setter
        public int progressRawMax;

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            ImageRequest request1 = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl))
                    .setResizeOptions(new ResizeOptions(PrefManager.getCoverWidth(), PrefManager.getCoverHeight()))
                    .build();
            ImageRequest request2 = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl.replace("l.jpg", ".jpg")))
                    .setResizeOptions(new ResizeOptions(PrefManager.getCoverWidth(), PrefManager.getCoverHeight()))
                    .build();
            imageUrlArray = new ImageRequest[]{ request1, request2 };
        }

        public void setShortDetails(String type) {
            this.shortDetails = type;
        }

        public IGFItem refreshDetails() {
            setProgress(createProgress(getUserStatusRaw(), getProgressRaw(), getProgressRawMax()));
            setShortDetails(getProgress() + " (" + getTypeRaw() + ") " + getScore());
            return this;
        }
    }

    public void AnimeFromCursor(Cursor cursor) {
        List<String> columnNames = Arrays.asList(cursor.getColumnNames());

        IGFItem igfTitle = createModel(cursor, columnNames);
        igfTitle.setProgressRawMax(cursor.getInt(columnNames.indexOf("episodes")));
        igfTitle.setUserStatusRaw(cursor.getString(columnNames.indexOf("watchedStatus")));
        igfTitle.setProgressRaw(cursor.getInt(columnNames.indexOf("watchedEpisodes")));
        igfTitle.setStatusRaw(cursor.getString(columnNames.indexOf("status")));
        igfTitle.setTypeRaw(cursor.getString(columnNames.indexOf("type")));
        igfTitle.setProgress(createProgress(igfTitle.getUserStatusRaw(), igfTitle.getProgressRaw(), igfTitle.getProgressRawMax()));
        igfTitle.setStatus(coverText[8] + " " + igfTitle.getStatusRaw() + " (" + igfTitle.getTypeRaw() + ")");
        igfTitle.setShortDetails(igfTitle.getProgress() + " (" + igfTitle.getTypeRaw() + ") " + igfTitle.getScore());
        this.titles.add(igfTitle);
        setFastScrollText(igfTitle);
    }

    public void MangaFromCursor(Cursor cursor, boolean chapters) {
        List<String> columnNames = Arrays.asList(cursor.getColumnNames());

        IGFItem igfTitle = createModel(cursor, columnNames);
        int maxprogress = cursor.getInt(columnNames.indexOf(chapters ? "chapters" : "volumes"));
        igfTitle.setUserStatusRaw(cursor.getString(columnNames.indexOf("readStatus")));
        igfTitle.setProgressRaw(cursor.getInt(columnNames.indexOf(chapters ? "chaptersRead" : "volumesRead")));
        igfTitle.setStatusRaw(cursor.getString(columnNames.indexOf("status")));
        igfTitle.setTypeRaw(cursor.getString(columnNames.indexOf("type")));
        igfTitle.setProgress(createProgress(igfTitle.getUserStatusRaw(), igfTitle.getProgressRaw(), maxprogress));
        igfTitle.setStatus(coverText[8] + " " + igfTitle.getStatusRaw() + " (" + igfTitle.getTypeRaw() + ")");
        igfTitle.setShortDetails(igfTitle.getProgress() + " (" + igfTitle.getTypeRaw() + ") " + igfTitle.getScore());
        this.titles.add(igfTitle);
        setFastScrollText(igfTitle);
    }

    private static String createProgress(String recordStatus, int progress, int maxProgress) {
        if (coverText == null)
            return "";
        String maxProgressString = maxProgress == 0 ? "?" : String.valueOf(maxProgress);
        switch (recordStatus) {
            case "watching":
                return coverText[0] + " " + progress + "/" + maxProgressString;
            case "reading":
                return coverText[1] + " " + progress + "/" + maxProgressString;
            case "completed":
                return coverText[2];
            case "on-hold":
                return coverText[3];
            case "dropped":
                return coverText[4];
            case "plan to watch":
                return coverText[5];
            case "plan to read":
                return coverText[6];
            default:
                return "";
        }
    }

    private IGFItem createModel(Cursor cursor, List<String> columnNames) {
        IGFItem igfItem = new IGFItem();
        igfItem.setId(cursor.getInt(columnNames.indexOf(DatabaseHelper.COLUMN_ID)));
        igfItem.setTitle(cursor.getString(columnNames.indexOf("title")));
        igfItem.setImageUrl(cursor.getString(columnNames.indexOf("imageUrl")));
        igfItem.setScoreRaw(cursor.getInt(columnNames.indexOf("score")));
        igfItem.setScore("☆" + (igfItem.getScoreRaw() > 0 ? igfItem.getScoreRaw() : "?"));

        return igfItem;
    }

    private void setFastScrollText(IGFItem igfItem) {
        switch (sortType) {
            case 1:
                this.fastScrollText.add(igfItem.getTitle().substring(0, 1));
                break;
            case 2:
                this.fastScrollText.add(igfItem.getScoreRaw() > 0 ? String.valueOf(igfItem.getScoreRaw()) : "?");
                break;
            case 3:
                this.fastScrollText.add(igfItem.getTypeRaw());
                break;
            case 4:
                this.fastScrollText.add(igfItem.getStatusRaw());
                break;
            case 5:
            case -5:
                this.fastScrollText.add(String.valueOf(igfItem.getProgressRaw()));
                break;
            default:
                this.fastScrollText.add(String.valueOf(this.titles.size() + 1));
                break;
        }
    }
}

