package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class ListStats implements Serializable {
    @Setter
    @SerializedName("plan_to_watch")
    public int planToWatch = 0;
    @Setter
    @SerializedName("plan_to_read")
    public int planToRead = 0;
    @Setter
    public int watching = 0;
    @Setter
    public int reading = 0;
    @Getter
    @Setter
    public int completed;
    @Getter
    @Setter
    @SerializedName("on_hold")
    public int onHold;
    @Getter
    @Setter
    public int dropped;

    public int getPlanned() {
        return planToWatch > 0 ? planToWatch : planToRead;
    }

    public int getReadWatch() {
        return watching > 0 ? watching : reading;
    }
}