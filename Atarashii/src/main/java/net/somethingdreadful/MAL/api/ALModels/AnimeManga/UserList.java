package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;

import static net.somethingdreadful.MAL.api.ALModels.AnimeManga.GenericRecord.getLanguageTitle;

public class UserList implements Serializable {
    @Getter
    private Lists lists;
    private ArrayList<String> custom_list_anime;
    private ArrayList<String> custom_list_manga;

    @Getter
    private int score_type;
    @Getter
    private int notifications;
    @Getter
    @SerializedName("title_language")
    private String titleLanguage;

    class Lists implements Serializable {
        @Getter @SerializedName("plan_to_watch") public ArrayList<ListDetails> planToWatch;
        @Getter @SerializedName("plan_to_read") public ArrayList<ListDetails> planToRead;
        @Getter @SerializedName("on_hold") public ArrayList<ListDetails> onHold;
        @Getter public ArrayList<ListDetails> completed;
        @Getter public ArrayList<ListDetails> dropped;
        @Getter public ArrayList<ListDetails> watching;
        @Getter public ArrayList<ListDetails> reading;
    }

    class ListDetails implements Serializable {
        @Getter @SerializedName("record_id") private int id;
        @Getter @SerializedName("custom_lists") private ArrayList<Integer> customLists;
        @Getter @SerializedName("list_status") private String listStatus;
        @Getter @SerializedName("updated_time") private String updatedtime;
        @Getter @SerializedName("added_time") private String addedtime;
        @Getter @SerializedName("score_raw") private int scoreraw;
        @Getter @SerializedName("episodes_watched") private int episodesWatched;
        @Getter @SerializedName("chapters_read") private int chaptersRead;
        @Getter @SerializedName("volumes_read") private int volumesRead;
        @Getter private int priorty;
        @Getter private String notes;
        @Getter private Anime anime;
        @Getter private Manga manga;
        private int rewatched;
        private int reread;

        public boolean getRewatched() {
            return rewatched > 0;
        }
    }

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList();
        PrefManager.setTitleNameLang(getTitleLanguage());
        PrefManager.commitChanges();
        model.setAnimeList(combineArrayAnime());
        model.setMangaList(combineArrayManga());

        if (custom_list_anime != null && custom_list_anime.size() > 0)
            PrefManager.setCustomAnimeList(custom_list_anime);
        if (custom_list_manga != null && custom_list_manga.size() > 0)
            PrefManager.setCustomMangaList(custom_list_manga);
        PrefManager.commitChanges();
        return model;
    }

    private ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> combineArrayAnime() {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> newList = new ArrayList<>();
        newList.addAll(convertAnime(getLists().completed));
        newList.addAll(convertAnime(getLists().planToWatch));
        newList.addAll(convertAnime(getLists().dropped));
        newList.addAll(convertAnime(getLists().watching));
        newList.addAll(convertAnime(getLists().onHold));
        return newList;
    }

    public IGFModel arrayManga() {
        IGFModel igfModel = new IGFModel(0);
        convertM(getLists().completed, igfModel);
        convertM(getLists().planToRead, igfModel);
        convertM(getLists().dropped, igfModel);
        convertM(getLists().reading, igfModel);
        convertM(getLists().onHold, igfModel);
        return igfModel;
    }

    public static IGFModel convertM(ArrayList<ListDetails> ALArray, IGFModel igfModel) {
        if (ALArray != null)
            for (ListDetails anime : ALArray) {
                IGFModel.IGFItem igfItem = igfModel.new IGFItem();
                Manga AD = anime.getManga();
                igfItem.setId(anime.getId());
                igfItem.setTitle(getLanguageTitle(AD.getTitleRomaji(), AD.getTitleEnglish(), AD.getTitleJapanese()));
                igfItem.setImageUrl(AD.getImageUrlLge());
                igfItem.setShortDetails(AD.getType());
                igfItem.setUserStatusRaw(anime.getListStatus());
                igfModel.getTitles().add(igfItem);
            }
        return igfModel;
    }

    public IGFModel arrayAnime() {
        IGFModel igfModel = new IGFModel(0);
        convertA(getLists().completed, igfModel);
        convertA(getLists().planToWatch, igfModel);
        convertA(getLists().dropped, igfModel);
        convertA(getLists().watching, igfModel);
        convertA(getLists().onHold, igfModel);
        return igfModel;
    }

    public static IGFModel convertA(ArrayList<ListDetails> ALArray, IGFModel igfModel) {
        if (ALArray != null)
            for (ListDetails anime : ALArray) {
                IGFModel.IGFItem igfItem = igfModel.new IGFItem();
                Anime AD = anime.getAnime();
                igfItem.setId(anime.getId());
                igfItem.setTitle(getLanguageTitle(AD.getTitleRomaji(), AD.getTitleEnglish(), AD.getTitleJapanese()));
                igfItem.setImageUrl(AD.getImageUrlLge());
                igfItem.setShortDetails(AD.getType());
                igfItem.setUserStatusRaw(anime.getListStatus());
                igfModel.getTitles().add(igfItem);
            }
        return igfModel;
    }

    private ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> convertAnime(ArrayList<ListDetails> list) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> newList = new ArrayList<>();
        if (list != null)
            for (ListDetails detail : list) {
                if (detail.getManga() == null && detail.getAnime() != null) {
                    net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime anime = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime();
                    Anime AD = detail.getAnime();
                    anime.setId(AD.getId());
                    anime.setTitle(getLanguageTitle(AD.getTitleRomaji(), AD.getTitleEnglish(), AD.getTitleJapanese()));
                    anime.setType(AD.getType());
                    anime.setImageUrl(AD.getImageUrlLge());
                    anime.setPopularity(AD.getPopularity());
                    anime.setStatus(AD.getAiringStatus());
                    anime.setAverageScore(AD.getAverageScore());
                    anime.setEpisodes(AD.getTotalEpisodes());
                    anime.setWatchedStatus(detail.getListStatus());
                    anime.setScore(detail.getScoreraw());
                    anime.setPriority(detail.getPriorty());
                    anime.setRewatching(detail.getRewatched());
                    anime.setNotes(detail.getNotes());
                    anime.setWatchedEpisodes(detail.getEpisodesWatched());
                    anime.setCustomList(getCustomListString(detail.getCustomLists()));
                    anime.setLastSync(new Date());
                    newList.add(anime);
                }
            }
        return newList;
    }

    /**
     * Get the custom list strings.
     *
     * @param customLists The list where the record was placed
     * @return String Atarashii custom list string.
     */
    private String getCustomListString(ArrayList<Integer> customLists) {
        String result = "";
        if (customLists != null && customLists.size() != 0) {
            for (int customList : customLists) {
                result = result + customList;
            }
            for (int i = result.length(); i < 15; i++) {
                result = result + "0";
            }
        } else {
            result = "000000000000000";
        }
        return result;
    }

    private ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> combineArrayManga() {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> newList = new ArrayList<>();
        newList.addAll(convertManga(getLists().completed));
        newList.addAll(convertManga(getLists().planToRead));
        newList.addAll(convertManga(getLists().dropped));
        newList.addAll(convertManga(getLists().reading));
        newList.addAll(convertManga(getLists().onHold));
        return newList;
    }

    private ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> convertManga(ArrayList<ListDetails> list) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> newList = new ArrayList<>();
        if (list != null)
            for (ListDetails detail : list) {
                if (detail.getAnime() == null && detail.getManga() != null) {
                    net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga manga = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga();
                    Manga MD = detail.getManga();
                    manga.setId(MD.getId());
                    manga.setTitle(getLanguageTitle(MD.getTitleRomaji(), MD.getTitleEnglish(), MD.getTitleJapanese()));
                    manga.setImageUrl(MD.getImageUrlLge());
                    manga.setType(MD.getType());
                    manga.setReadStatus(detail.getListStatus());
                    manga.setPriority(detail.getPriorty());
                    manga.setChaptersRead(detail.getChaptersRead());
                    manga.setVolumesRead(detail.getVolumesRead());
                    manga.setRereading(detail.getRewatched() ? 1 : 0);
                    manga.setNotes(detail.getNotes());
                    manga.setScore(detail.getScoreraw());
                    manga.setCustomList(getCustomListString(detail.getCustomLists()));
                    manga.setLastSync(new Date());
                    newList.add(manga);
                }
            }
        return newList;
    }
}
