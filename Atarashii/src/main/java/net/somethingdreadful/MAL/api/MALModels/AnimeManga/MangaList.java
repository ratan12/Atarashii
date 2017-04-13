package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class MangaList implements Serializable {
    @Setter
    @Getter
    private ArrayList<Manga> manga;

    @Setter
    @Getter
    private Statistics statistics;

    public class Statistics implements Serializable {
        @Setter
        @Getter
        private float days;
    }

    public static UserList createBaseModel(MangaList MALArray) {
        UserList userList = new UserList();
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> MangaList = new ArrayList<>();
        //if (MALArray != null)
           // MangaList = convertBaseArray(MALArray.getManga());
        userList.setMangaList(MangaList);
        return userList;
    }

    public static IGFModel convertBaseArray(ArrayList<Manga> MALArray) {
        IGFModel igfModel = new IGFModel(0);
        boolean secamounts = PrefManager.getUseSecondaryAmountsEnabled();
        if (MALArray != null)
            for (Manga manga : MALArray) {
                IGFModel.IGFItem igfItem = igfModel.new IGFItem();
                igfItem.setId(manga.getId());
                igfItem.setTitle(manga.getTitle());
                igfItem.setImageUrl(manga.getImageUrl());
                igfItem.setScoreRaw((int) Math.floor(manga.getMembersScore()));
                igfItem.setProgress(IGFModel.coverText[7] + " " + (igfItem.getScoreRaw() > 0 ? igfItem.getScoreRaw() : "?"));
                igfItem.setScore("★"+ manga.getMembersScore());
                igfItem.setUserStatusRaw(manga.getReadStatus());
                igfItem.setStatusRaw(manga.getStatus());
                igfItem.setTypeRaw(manga.getType());
                if (secamounts) {
                    igfItem.setShortDetails(IGFModel.coverText[14] + " " + manga.getVolumesRead() + " (" + igfItem.getTypeRaw() + ") " + igfItem.getScore());
                } else {
                    igfItem.setShortDetails(IGFModel.coverText[13] + " " + manga.getChaptersRead() + " (" + igfItem.getTypeRaw() + ") " + igfItem.getScore());
                }
                igfModel.getTitles().add(igfItem);
            }
        return igfModel;
    }
}
