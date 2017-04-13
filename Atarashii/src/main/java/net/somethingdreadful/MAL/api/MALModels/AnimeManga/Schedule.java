package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Schedule implements Serializable {
    /**
     * The list of monday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> monday;

    /**
     * The list of tuesday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> tuesday;

    /**
     * The list of wednesday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> wednesday;

    /**
     * The list of thursday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> thursday;

    /**
     * The list of friday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> friday;

    /**
     * The list of saturday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> saturday;
    /**
     * The list of sunday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> sunday;

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule convertBaseSchedule() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule schedule = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule();
        schedule.setMonday(convertBaseArray(getMonday()));
        schedule.setTuesday(convertBaseArray(getTuesday()));
        schedule.setWednesday(convertBaseArray(getWednesday()));
        schedule.setThursday(convertBaseArray(getThursday()));
        schedule.setFriday(convertBaseArray(getFriday()));
        schedule.setSaturday(convertBaseArray(getSaturday()));
        schedule.setSunday(convertBaseArray(getSunday()));

        return schedule;
    }

    private ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> convertBaseArray(ArrayList<Anime> animeList) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> list = new ArrayList<>();
        for (Anime anime : animeList)
            list.add(anime.createBaseModel());
        return list;
    }
}
