package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Manga extends GenericRecord implements Serializable {
    @Getter
    @Setter
    @SerializedName("publishing_status")
    private String publishingStatus;
    @Getter
    @Setter
    @SerializedName("total_chapters")
    private int totalChapters;
    @Getter
    @Setter
    @SerializedName("total_volumes")
    private int totalVolumes;

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga();
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(true);
        createGeneralBaseModel(model);
        model.setStatus(getPublishingStatus());
        model.setChapters(getTotalChapters());
        model.setVolumes(getTotalVolumes());
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(false);
        return model;
    }

    public static IGFModel convertBaseArray(ArrayList<Manga> ALArray) {
        IGFModel igfModel = new IGFModel(0);
        if (ALArray != null)
            for (Manga anime : ALArray) {
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
