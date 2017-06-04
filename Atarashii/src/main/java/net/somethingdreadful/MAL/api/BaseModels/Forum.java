package net.somethingdreadful.MAL.api.BaseModels;

import net.somethingdreadful.MAL.api.MALModels.Profile;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Forum implements Serializable {

    /**
     * The ID used to get topic/board
     */
    @Setter
    @Getter
    public int id = 0;

    /**
     * The forum board/topic name.
     */
    @Setter
    @Getter
    public String name;

    /**
     * The username of the topic creator
     */
    @Setter
    @Getter
    public String username;

    /**
     * The number of replies of a topic
     */
    @Setter
    @Getter
    public int replies = 0;

    /**
     * The description of a board
     */
    @Setter
    @Getter
    public String description;

    /**
     * The info of the last reply inside a topic
     */
    @Setter
    @Getter
    public Forum reply;

    /**
     * The children of a forumboard
     */
    @Setter
    @Getter
    public ArrayList<Forum> children;

    /**
     * The comment content in an post
     */
    @Setter
    @Getter
    public String comment;

    /**
     * The creation time of this post
     */
    @Setter
    @Getter
    public String time;

    /**
     * The max amount of pages
     * <p/>
     * Note: Only the first item will contain this
     */
    @Setter
    @Getter
    public int maxPages;

    /**
     * The userprofile for the user details in topics
     */
    @Setter
    @Getter
    public Profile profile;
}
