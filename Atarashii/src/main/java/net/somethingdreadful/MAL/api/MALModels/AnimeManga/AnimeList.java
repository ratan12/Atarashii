package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class AnimeList implements Serializable {
    @Setter
    @Getter
    private ArrayList<Anime> anime;

    @Setter
    @Getter
    private Statistics statistics;

    public class Statistics implements Serializable {
        @Setter
        @Getter
        private float days;
    }

    public static UserList createBaseModel(AnimeList MALArray) {
        UserList userList = new UserList();
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> animeList = new ArrayList<>();
        if (MALArray != null)
            for (Anime anime : MALArray.getAnime()) {
                animeList.add(anime.createBaseModel());
            }
        userList.setAnimeList(animeList);
        return userList;
    }

    public static IGFModel convertBaseArray(ArrayList<Anime> MALArray) {
        IGFModel igfModel = new IGFModel(0);
        if (MALArray != null)
            for (Anime anime : MALArray) {
                IGFModel.IGFItem igfItem = igfModel.new IGFItem();
                igfItem.setId(anime.getId());
                igfItem.setTitle(anime.getTitle());
                igfItem.setImageUrl(anime.getImageUrl());
                igfItem.setScoreRaw((int) Math.floor(anime.getMembersScore()));
                igfItem.setProgress(IGFModel.coverText[7] + " " + (igfItem.getScoreRaw() > 0 ? igfItem.getScoreRaw() : "?"));
                igfItem.setScore("★"+ (anime.getWatchedStatus() != null ? anime.getScore() : anime.getMembersScore()));
                igfItem.setStatusRaw(anime.getStatus());
                igfItem.setUserStatusRaw(anime.getWatchedStatus());
                igfItem.setTypeRaw(anime.getType());
                igfItem.setShortDetails(IGFModel.coverText[10] + " " + anime.getEpisodes() + " (" + igfItem.getTypeRaw() + ") " + igfItem.getScore());
                igfModel.getTitles().add(igfItem);
            }
        return igfModel;
    }
}
