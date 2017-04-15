package net.somethingdreadful.MAL.api.MALModels;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/*
 * Base stub class for relations returned by API like side stories, sequels etc.
 * It contains both manga_id and anime_id to make it usable as response class for deserialization
 * through retrofit. Only one of those variables is set to a valid value.
 */
public class RecordStub implements Serializable {
    /**
     * The anime id of a record
     */
    @Getter
    @Setter
    private int anime_id = 0;

    /**
     * The manga id of a record
     */
    @Getter
    @Setter
    private int manga_id = 0;

    /**
     * The title of a record
     */
    @Getter
    @Setter
    private String title;

    /**
     * The anime/manga URL of a record
     */
    @Getter
    @Setter
    private String url;

    public int getId() {
        return anime_id != 0 ? anime_id : manga_id;
    }

    public boolean isAnime() {
        return anime_id != 0;
    }

    public boolean getType() {
        return anime_id > 0;
    }

    public void setId(int id, boolean anime) {
        if (anime)
            setAnime_id(id);
        else
            setManga_id(id);
    }
}
