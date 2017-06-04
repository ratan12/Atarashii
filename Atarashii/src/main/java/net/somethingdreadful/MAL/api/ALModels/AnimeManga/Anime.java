package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Anime extends GenericRecord implements Serializable {
    @Getter
    @Setter
    private int duration;
    @Getter
    @Setter
    @SerializedName("airing_status")
    private String airingStatus;
    @Getter
    @Setter
    @SerializedName("total_episodes")
    private int totalEpisodes;
    @Getter
    @Setter
    @SerializedName("youtube_id")
    private String youtubeId;

    @Getter
    @Setter
    private Airing airing;

    public static class Airing implements Serializable {
        @Getter
        @Setter
        public String time;
        @Getter
        @Setter
        public String normaltime;
        @Getter
        @Setter
        public int countdown;
        @Getter
        @Setter
        @SerializedName("next_episode")
        public int nextEpisode;
    }

    public static Schedule convertBaseSchedule(ArrayList<Anime> ALArray) {
        Schedule schedule = new Schedule();
        if (ALArray != null) {
            for (Anime anime : ALArray) {
                // Some records do not have airing info.
                if (anime.getAiring() != null && anime.getAiring().getTime() != null) {
                    switch (DateTools.INSTANCE.getDayOfWeek(anime.getAiring().getTime())) {
                        case 2: // Monday
                            schedule.getMonday().add(anime.createBaseModel());
                            break;
                        case 3: // Tuesday
                            schedule.getTuesday().add(anime.createBaseModel());
                            break;
                        case 4: // Wednesday
                            schedule.getWednesday().add(anime.createBaseModel());
                            break;
                        case 5: // Thursday
                            schedule.getThursday().add(anime.createBaseModel());
                            break;
                        case 6: // Friday
                            schedule.getFriday().add(anime.createBaseModel());
                            break;
                        case 7: // Saturday
                            schedule.getSaturday().add(anime.createBaseModel());
                            break;
                        case 1: // Sunday
                            schedule.getSunday().add(anime.createBaseModel());
                            break;
                    }
                }
            }
        }
        return schedule;
    }

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime();
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(true);
        createGeneralBaseModel(model);

        model.setDuration(getDuration());
        model.setStatus(getAiringStatus());
        model.setEpisodes(getTotalEpisodes());
        model.setYoutubeId(getYoutubeId());
        model.setAiring(getAiring());
        if (model.getAiring()!= null && model.getAiring().getTime() != null)
            model.getAiring().setNormaltime(DateTools.INSTANCE.parseDate(model.getAiring().getTime(), true));
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(false);
        return model;
    }

    public static IGFModel convertBaseArray(ArrayList<Anime> ALArray) {
        IGFModel igfModel = new IGFModel(0);
        if (ALArray != null)
            for (Anime anime : ALArray) {
                IGFModel.IGFItem igfItem = igfModel.new IGFItem();
                igfItem.setId(anime.getId());
                igfItem.setTitle(getLanguageTitle(anime.getTitleRomaji(), anime.getTitleEnglish(), anime.getTitleJapanese()));
                igfItem.setImageUrl(anime.getImageUrlLge());
                igfItem.setShortDetails(anime.getType());
                igfModel.getTitles().add(igfItem);
            }
        return igfModel;
    }
}
