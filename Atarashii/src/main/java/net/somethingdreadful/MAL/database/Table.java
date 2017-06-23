package net.somethingdreadful.MAL.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.account.AccountService;

import static net.somethingdreadful.MAL.database.DatabaseHelper.COLUMN_ID;

public class Table {
    private String queryString = "";
    private static SQLiteDatabase db;

    static final String TABLE_ACCOUNTS = "accounts";
    static final String TABLE_ANIME = "anime";
    static final String TABLE_MANGA = "manga";
    static final String TABLE_PROFILE = "profile";
    static final String TABLE_FRIENDLIST = "friendlist";
    static final String TABLE_PRODUCER = "producer";
    static final String TABLE_ANIME_PRODUCER = "anime_producer";
    static final String TABLE_ANIME_MUSIC = "animemusic";
    static final String TABLE_ANIME_OTHER_TITLES = "animeothertitles";
    static final String TABLE_MANGA_OTHER_TITLES = "mangaothertitles";
    static final String TABLE_SCHEDULE = "schedule";

    static final String TABLE_ANIME_ANIME_RELATIONS = "rel_anime_anime";
    static final String TABLE_ANIME_MANGA_RELATIONS = "rel_anime_manga";
    static final String TABLE_MANGA_MANGA_RELATIONS = "rel_manga_manga";
    static final String TABLE_MANGA_ANIME_RELATIONS = "rel_manga_anime";

    static final String TABLE_GENRES = "genres";
    static final String TABLE_ANIME_GENRES = "anime_genres";
    static final String TABLE_MANGA_GENRES = "manga_genres";

    static final String TABLE_TAGS = "tags";
    static final String TABLE_ANIME_TAGS = "anime_tags";
    static final String TABLE_ANIME_PERSONALTAGS = "anime_personaltags";
    static final String TABLE_MANGA_TAGS = "manga_tags";
    static final String TABLE_MANGA_PERSONALTAGS = "manga_personaltags";

    public static Table create(SQLiteDatabase db) {
        Table.db = db;
        return new Table();
    }

    /**
     * Get the valid table name of an user account.
     *
     * @param table The table name
     * @return String that the real table will be
     */
    public static String getName(String table) {
        return table + "_" + AccountService.Companion.getAccountId() + "_";
    }

    /**
     * Get the valid table name of an user account.
     *
     * @param table The table name
     * @return String that the real table will be
     */
    public static String setName(String table, int id) {
        return table + "_" + id + "_";
    }

    public void createOtherTitles(String table, String ListTypeTable) {
        queryString += "CREATE TABLE "
                + table + " ("
                + COLUMN_ID + " integer NOT NULL REFERENCES " + ListTypeTable + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "titleType integer NOT NULL, "
                + "title varchar NOT NULL, "
                + "PRIMARY KEY(" + COLUMN_ID + ",titleType , title)"
                + ");";
        run();
    }

    public void createAccounts() {
        queryString += "CREATE TABLE "
                + TABLE_ACCOUNTS + " ("
                + COLUMN_ID + " integer primary key autoincrement,"
                + "username varchar, "
                + "imageUrl varchar, "
                + "website integer "
                + ");";
        run();
    }

    public void createRecord(String table) {
        queryString += "create TABLE "
                + table + " ("
                + COLUMN_ID + " integer primary key, "
                + "title varchar, "
                + "type varchar, "
                + "imageUrl varchar, "
                + "bannerUrl varchar, "
                + "synopsis varchar, "
                + "status varchar, "
                + "startDate varchar, "
                + "endDate varchar, "
                + "score integer, "
                + "priority integer, "
                + "classification varchar, "
                + "averageScore varchar, "
                + "averageScoreCount varchar, "
                + "popularity integer, "
                + "rank integer, "
                + "notes varchar, "
                + "favoritedCount integer, "
                + "dirty varchar, "
                + "createFlag integer, "
                + "deleteFlag integer, "
                + "lsPlanned integer, "
                + "lsReadWatch integer, "
                + "lsCompleted integer, "
                + "lsOnHold integer, "
                + "lsDropped integer, "
                + "customList varchar default '000000000000000', "
                + "lastSync long, ";

        if (table.contains(TABLE_ANIME))
            queryString += "duration integer, "
                    + "episodes integer, "
                    + "youtubeId varchar, "
                    + "airingTime varchar, "
                    + "nextEpisode integer, "
                    + "watchedStatus varchar, "
                    + "watchedEpisodes integer, "
                    + "watchingStart varchar, "
                    + "watchingEnd varchar, "
                    + "storage integer, "
                    + "storageValue float, "
                    + "rewatching integer, "
                    + "rewatchCount integer, "
                    + "rewatchValue integer, "
                    + "officialSite varchar, "
                    + "animeDB varchar, "
                    + "wikipedia varchar, "
                    + "animeNewsNetwork varchar "
                    + ");";
        else
            queryString += "chapters integer, "
                    + "volumes integer, "
                    + "readStatus varchar, "
                    + "chaptersRead integer, "
                    + "volumesRead integer, "
                    + "readingStart varchar, "
                    + "readingEnd varchar, "
                    + "rereading integer, "
                    + "rereadCount integer, "
                    + "rereadValue integer "
                    + ");";
        run();
    }

    public void createFriendlist(String table) {
        queryString += "create table "
                + table + " ("
                + "username varchar, "
                + "imageUrl varchar, "
                + "lastOnline varchar "
                + ");";
        run();
    }

    public void createSchedule(String table) {
        queryString += "create table "
                + table + " ("
                + COLUMN_ID + " integer, "
                + "title varchar, "
                + "imageUrl varchar, "
                + "type varchar, "
                + "episodes integer, "
                + "avarageScore varchar, "
                + "averageScoreCount varchar, "
                + "broadcast varchar, "
                + "day integer "
                + ");";
        run();
    }

    public void createProducer(String table, String main) {
        queryString += "create table "
                + table + " ("
                + "anime_id integer NOT NULL REFERENCES " + getName(TABLE_ANIME) + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "producer_id integer NOT NULL REFERENCES " + main + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "PRIMARY KEY(anime_id, producer_id)"
                + ");";
        run();
    }

    public void createMainLink(String table) {
        queryString += "create table "
                + table + " ("
                + COLUMN_ID + " integer primary key autoincrement, "
                + "title varchar NOT NULL "
                + ");";
        run();
    }

    public void createGenre(String table, String record) {
        queryString += "create table "
                + table + " ("
                + getTagsColumn(record) + " integer NOT NULL REFERENCES " + record + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "genre_id integer NOT NULL REFERENCES " + getName(TABLE_GENRES) + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "PRIMARY KEY(" + getTagsColumn(record) + ", genre_id)"
                + ");";
        run();
    }

    /**
     * Create the profile table.
     */
    public void createProfile(String table) {
        queryString += "create table "
                + table + " ("
                + "username varchar UNIQUE, "
                + "imageUrl varchar, "
                + "imageUrlBanner varchar, "
                + "notifications integer, "
                + "about varchar, "
                + "lastOnline varchar, "
                + "status varchar, "
                + "gender varchar, "
                + "birthday varchar, "
                + "location varchar, "
                + "website varchar, "
                + "joinDate varchar, "
                + "accessRank varchar, "
                + "animeListViews integer, "
                + "mangaListViews integer, "
                + "forumPosts integer, "
                + "comments integer, "

                + "AnimetimeDays double, "
                + "Animewatching integer, "
                + "Animecompleted integer, "
                + "AnimeonHold integer, "
                + "Animedropped integer, "
                + "AnimeplanToWatch integer, "
                + "AnimetotalEntries integer, "

                + "MangatimeDays double, "
                + "Mangareading integer, "
                + "Mangacompleted integer, "
                + "MangaonHold integer, "
                + "Mangadropped integer, "
                + "MangaplanToRead integer, "
                + "MangatotalEntries integer "
                + ");";
        run();
    }

    /**
     * Create tags table.
     *
     * @param table     The table name
     * @param refTable1 The table that should get referenced
     * @param refTable2 The table that will be referenced with
     */
    public void createTags(String table, String refTable1, String refTable2) {
        queryString += "CREATE TABLE " + table + " ("
                + getTagsColumn(table) + " integer NOT NULL REFERENCES " + refTable1 + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "tag_id integer NOT NULL REFERENCES " + refTable2 + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "PRIMARY KEY(" + getTagsColumn(table) + ", tag_id)"
                + ");";
        run();
    }

    /**
     * Get the id column name
     *
     * @param table The table name
     * @return String The id column name
     */
    private String getTagsColumn(String table) {
        return table.contains("anime") ? "anime_id" : "manga_id";
    }

    /**
     * Create relation table.
     *
     * @param table     The table name
     * @param refTable1 The table that should get referenced
     * @param refTable2 The table that will be referenced with
     */
    public void createRelation(String table, String refTable1, String refTable2) {
        queryString += "CREATE TABLE " + table + " ("
                + COLUMN_ID + " integer NOT NULL REFERENCES " + refTable1 + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "relationId integer NOT NULL REFERENCES " + refTable2 + " (" + COLUMN_ID + ") ON DELETE CASCADE, "
                + "relationType integer NOT NULL, "
                + "PRIMARY KEY(" + COLUMN_ID + ", relationType, relationId)"
                + ");";
        run();
    }

    private void run() {
        try {
            db.execSQL(queryString);
            queryString = "";
        } catch (Exception e) {
            AppLog.log(Log.INFO, "Atarashii", "Table.run(" + toString() + "): " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return queryString;
    }
}