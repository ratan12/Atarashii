package net.somethingdreadful.MAL;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.ALModels.ForumAL;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Forum;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALModels.Recommendations;
import net.somethingdreadful.MAL.database.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class ContentManager {
    private MALApi malApi;
    private ALApi alApi;
    private final DatabaseManager dbMan;

    public ContentManager(Context context) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.ContentManager(context)");
        if (AccountService.Companion.isMAL())
            malApi = new MALApi();
        else
            alApi = new ALApi();
        dbMan = new DatabaseManager(context);
    }

    public ContentManager(Activity activity) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.ContentManager(activity)");
        if (AccountService.Companion.isMAL())
            malApi = new MALApi(activity);
        else
            alApi = new ALApi(activity);
        dbMan = new DatabaseManager(activity);
    }

    public static String listSortFromInt(int i, boolean isAnime) {
        if (6 < i) // custom lists
            return GenericRecord.CUSTOMLIST + (i - 6);

        switch (i) {
            case 0:
                return "";
            case 1:
                return isAnime ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
            case 2:
                return Anime.STATUS_COMPLETED;
            case 3:
                return Anime.STATUS_ONHOLD;
            case 4:
                return Anime.STATUS_DROPPED;
            case 5:
                return isAnime ? Anime.STATUS_PLANTOWATCH : Manga.STATUS_PLANTOREAD;
            case 6:
                return isAnime ? Anime.STATUS_REWATCHING : Manga.STATUS_REREADING;
            default:
                return isAnime ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
        }
    }

    public Anime getAnime(int id) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getAnime(): id=" + id);
        return dbMan.getAnime(id);
    }

    public Manga getManga(int id) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getManga(): id=" + id);
        return dbMan.getManga(id);
    }

    public IGFModel getProfileAnimeList(String username) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getProfileAnimeList(): username=" + username);
        return AccountService.Companion.isMAL() ? malApi.getProfileAnimeList(username) : alApi.getProfileAnimeList(username);
    }

    public IGFModel getProfileMangaList(String username) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getProfileMangaList(): username=" + username);
        return AccountService.Companion.isMAL() ? malApi.getProfileMangaList(username) : alApi.getProfileMangaList(username);
    }

    public ArrayList<Anime> downloadAnimeList(String username) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.downloadAnimeList(): username=" + username);
        UserList animeList = AccountService.Companion.isMAL() ? malApi.getAnimeList(username) : alApi.getAnimeList(username);

        if (animeList != null && username.equals(AccountService.Companion.getUsername())) {
            dbMan.saveAnimeList(animeList.getAnimeList());
            dbMan.cleanupAnimeTable();
            return animeList.getAnimeList();
        } else if (animeList != null) {
            return animeList.getAnimeList();
        }
        return new ArrayList<>();
    }

    public ArrayList<Manga> downloadMangaList(String username) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.downloadMangaList(): username=" + username);
        UserList mangaList = AccountService.Companion.isMAL() ? malApi.getMangaList(username) : alApi.getMangaList(username);

        if (mangaList != null && username.equals(AccountService.Companion.getUsername())) {
            dbMan.saveMangaList(mangaList.getMangaList());
            dbMan.cleanupMangaTable();
            return mangaList.getMangaList();
        } else if (mangaList != null) {
            return mangaList.getMangaList();
        }
        return new ArrayList<>();
    }

    public IGFModel getAnimeListFromDB(String ListType, int sortType, String inverse) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getAnimeListFromDB(): listType=" + ListType + " sortType=" + sortType + " inverse=" + inverse);
        return dbMan.getAnimeList(ListType, sortType, inverse.equals("false") ? 1 : 2);
    }

    public IGFModel getMangaListFromDB(String ListType, int sortType, String inverse) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getMangaListFromDB(): listType=" + ListType + " sortType=" + sortType + " inverse=" + inverse);
        return dbMan.getMangaList(ListType, sortType, inverse.equals("false") ? 1 : 2);
    }

    public Manga updateWithDetails(int id, Manga manga) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.updateWithDetails(): id=" + id);
        Manga manga_api = AccountService.Companion.isMAL() ? malApi.getManga(id, 1) : alApi.getManga(id);

        if (manga_api != null) {
            dbMan.saveManga(manga_api);
            return AccountService.Companion.isMAL() ? manga_api : dbMan.getManga(id);
        }
        return manga;
    }

    public Anime updateWithDetails(int id, Anime anime) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.updateWithDetails(): id=" + id);
        Anime anime_api = AccountService.Companion.isMAL() ? malApi.getAnime(id, 1) : alApi.getAnime(id);

        if (anime_api != null) {
            dbMan.saveAnime(anime_api);
            return AccountService.Companion.isMAL() ? anime_api : dbMan.getAnime(id);
        }
        return anime;
    }

    public ArrayList<Profile> downloadAndStoreFriendList(String user) {
        ArrayList<Profile> result = new ArrayList<>();
        try {
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.downloadAndStoreFriendList(): username=" + user);
            result = AccountService.Companion.isMAL() ? malApi.getFriends(user) : alApi.getFollowing(user);

            if (result != null && result.size() > 0 && AccountService.Companion.getUsername().equals(user))
                dbMan.saveFriendList(result);
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "ContentManager.downloadAndStoreFriendList(): " + e.getMessage());
            AppLog.logException(e);
        }

        return sortFriendlist(result);
    }

    public ArrayList<Profile> getFollowers(String user) {
        ArrayList<Profile> result = new ArrayList<>();
        try {
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.getFollowers(): username=" + user);
            result = alApi.getFollowers(user);
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "ContentManager.getFollowers(): " + e.getMessage());
            AppLog.logException(e);
        }

        return sortFriendlist(result);
    }

    private ArrayList<Profile> sortFriendlist(ArrayList<Profile> result) {
        //sort friendlist
        Collections.sort(result != null ? result : new ArrayList<Profile>(), new Comparator<Profile>() {
            @Override
            public int compare(Profile profile1, Profile profile2) {
                return profile1.getUsername().toLowerCase().compareTo(profile2.getUsername().toLowerCase());
            }
        });
        return result;
    }

    public ArrayList<Profile> getFriendListFromDB() {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getFriendListFromDB()");
        return dbMan.getFriendList();
    }

    public Profile getProfile(String name) {
        Profile profile = new Profile();
        try {
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.getProfile(): username=" + name);
            profile = AccountService.Companion.isMAL() ? malApi.getProfile(name) : alApi.getProfile(name);

            if (profile != null) {
                profile.setUsername(name);
                if (name.equalsIgnoreCase(AccountService.Companion.getUsername())) {
                    PrefManager.setProfileImage(profile.getImageUrl());
                    PrefManager.commitChanges();
                    dbMan.saveProfile(profile);
                }
            }
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "ContentManager.getProfile(): " + e.getMessage());
            AppLog.logException(e);
        }
        return profile;
    }

    public Profile getProfileFromDB() {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getProfileFromDB()");
        return dbMan.getProfile();
    }

    public void saveAnimeToDatabase(Anime anime) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.saveAnimeToDatabase(): id=" + anime.getId());
        dbMan.saveAnime(anime);
    }

    public void saveMangaToDatabase(Manga manga) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.saveMangaToDatabase(): id=" + manga.getId());
        dbMan.saveManga(manga);
    }

    public void cleanDirtyAnimeRecords() {
        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList();

        if (dirtyAnimes != null) {
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.cleanDirtyAnimeRecords(): size=" + dirtyAnimes.size());

            for (Anime anime : dirtyAnimes) {
                if (writeAnimeDetails(anime)) {
                    anime.clearDirty();
                    saveAnimeToDatabase(anime);
                } else if (anime != null) {
                    AppLog.log(Log.ERROR, "Atarashii", "ContentManager.cleanDirtyAnimeRecords(): Failed to update " + anime.getId());
                }
            }
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.cleanDirtyAnimeRecords()");
        }
    }

    public void cleanDirtyMangaRecords() {
        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList();

        if (dirtyMangas != null) {
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.cleanDirtyMangaRecords(): size=" + dirtyMangas.size());

            for (Manga manga : dirtyMangas) {
                if (writeMangaDetails(manga)) {
                    manga.clearDirty();
                    saveMangaToDatabase(manga);
                } else if (manga != null) {
                    AppLog.log(Log.ERROR, "Atarashii", "ContentManager.cleanDirtyMangaRecords(): Failed to update " + manga.getId() + ".");
                }
            }
            AppLog.log(Log.INFO, "Atarashii", "ContentManager.cleanDirtyMangaRecords()");
        }
    }

    public ArrayList<History> getActivity(String username, int page) {
        ArrayList<History> result = AccountService.Companion.isMAL() ? malApi.getActivity(username) : alApi.getActivity(username, page);
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getActivity(): got " + String.valueOf(result != null ? result.size() : 0) + " records on " + page);
        return result;
    }

    /**
     * Api Requests
     * <p/>
     * All the methods below this block is used to determine and make request to the API.
     */

    public Anime getAnimeRecord(int id) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getAnimeRecord(): id=" + id);
        return AccountService.Companion.isMAL() ? malApi.getAnime(id, 0) : alApi.getAnime(id);
    }

    public Manga getMangaRecord(int id) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getMangaRecord(): id=" + id);
        return AccountService.Companion.isMAL() ? malApi.getManga(id, 0) : alApi.getManga(id);
    }

    public void verifyAuthentication() {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.verifyAuthentication()");
        if (AccountService.Companion.isMAL())
            malApi.isAuth();
        else if (AccountService.Companion.getAccesToken() == null)
            alApi.getAccesToken();
    }

    public boolean writeAnimeDetails(Anime anime) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.writeAnimeDetails(): id=" + anime.getId());
        boolean result;
        if (anime.getDeleteFlag())
            result = AccountService.Companion.isMAL() ? malApi.deleteAnimeFromList(anime.getId()) : alApi.deleteAnimeFromList(anime.getId());
        else
            result = AccountService.Companion.isMAL() ? malApi.addOrUpdateAnime(anime) : alApi.addOrUpdateAnime(anime);
        AppLog.log(result ? Log.INFO : Log.ERROR, "Atarashii", "ContentManager.writeAnimeDetails(): successfully=" + result);
        return result;
    }

    public boolean writeMangaDetails(Manga manga) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.writeMangaDetails(): id=" + manga.getId());
        boolean result;
        if (manga.getDeleteFlag())
            result = AccountService.Companion.isMAL() ? malApi.deleteMangaFromList(manga.getId()) : alApi.deleteMangaFromList(manga.getId());
        else
            result = AccountService.Companion.isMAL() ? malApi.addOrUpdateManga(manga) : alApi.addOrUpdateManga(manga);
        AppLog.log(result ? Log.INFO : Log.ERROR, "Atarashii", "ContentManager.writeMangaDetails(): successfully=" + result);
        return result;
    }

    public IGFModel getMostPopularAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getMostPopularAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getMostPopularAnime(page) : alApi.getMostPopularAnime(page);
    }

    public IGFModel getMostPopularManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getMostPopularManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getMostPopularManga(page) : alApi.getMostPopularManga(page);
    }

    public IGFModel getTopRatedAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopRatedAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getTopRatedAnime(page) : alApi.getTopRatedAnime(page);
    }

    public IGFModel getTopRatedManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopRatedManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getTopRatedManga(page) : alApi.getTopRatedManga(page);
    }

    public IGFModel getJustAddedAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getJustAddedAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getJustAddedAnime(page) : alApi.getJustAddedAnime(page);
    }

    public IGFModel getJustAddedManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getJustAddedManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getJustAddedManga(page) : alApi.getJustAddedManga(page);
    }

    public IGFModel getUpcomingAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getUpcomingAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getUpcomingAnime(page) : alApi.getUpcomingAnime(page);
    }

    public IGFModel getUpcomingManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getUpcomingManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getUpcomingManga(page) : alApi.getUpcomingManga(page);
    }

    public IGFModel searchAnime(String query, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.searchAnime(): page=" + page + " query=" + query);
        return AccountService.Companion.isMAL() ? malApi.searchAnime(query, page) : alApi.searchAnime(query, page);
    }

    public IGFModel searchManga(String query, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.searchManga(): page=" + page + " query=" + query);
        return AccountService.Companion.isMAL() ? malApi.searchManga(query, page) : alApi.searchManga(query, page);
    }

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getAnimeReviews(): page=" + page + " id=" + id);
        return AccountService.Companion.isMAL() ? malApi.getAnimeReviews(id, page) : alApi.getAnimeReviews(id, page);
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getMangaReviews(): page=" + page + " id=" + id);
        return AccountService.Companion.isMAL() ? malApi.getMangaReviews(id, page) : alApi.getMangaReviews(id, page);
    }

    public ArrayList<Forum> getForumCategories() {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getForumCategories()");
        return AccountService.Companion.isMAL() ? malApi.getForum().createBaseModel() : ForumAL.getForum();
    }

    public ArrayList<Forum> getCategoryTopics(int id, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getCategoryTopics(): page=" + page + " id=" + id);
        return AccountService.Companion.isMAL() ? malApi.getCategoryTopics(id, page).createBaseModel() : alApi.getTags(id, page).getForumListBase();
    }

    public ArrayList<Forum> getTopic(int id, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopic(): page=" + page + " id=" + id);
        return AccountService.Companion.isMAL() ? malApi.getPosts(id, page).createBaseModel() : alApi.getPosts(id, page).convertBaseModel();
    }

    public void deleteAnime(Anime anime) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.deleteAnime(): id=" + anime.getId());
        dbMan.deleteAnime(anime.getId());
    }

    public void deleteManga(Manga manga) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.deleteManga(): id=" + manga.getId());
        dbMan.deleteManga(manga.getId());
    }

    public ArrayList<Forum> search(String query) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.search(): query=" + query);
        return AccountService.Companion.isMAL() ? malApi.search(query).createBaseModel() : alApi.search(query).getForumListBase();
    }

    public ArrayList<Forum> getSubCategory(int id, int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getSubCategory(): page=" + page + " id=" + id);
        return malApi.getSubBoards(id, page).createBaseModel();
    }

    public boolean addComment(int id, String message) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.addComment(): message=" + message + " id=" + id);
        return AccountService.Companion.isMAL() ? malApi.addComment(id, message) : alApi.addComment(id, message);
    }

    public boolean updateComment(int id, String message) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.updateComment(): message=" + message + " id=" + id);
        return AccountService.Companion.isMAL() ? malApi.updateComment(id, message) : alApi.updateComment(id, message);
    }

    public ArrayList<Recommendations> getAnimeRecs(int id) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getAnimeRecs(): id=" + id);
        return malApi.getAnimeRecs(id);
    }

    public ArrayList<Recommendations> getMangaRecs(int id) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getMangaRecs(): id=" + id);
        return malApi.getMangaRecs(id);
    }

    public Schedule getSchedule() {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getSchedule()");
        return AccountService.Companion.isMAL() ? malApi.getSchedule() : alApi.getSchedule();
    }

    public void saveSchedule(Schedule schedule) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.saveSchedule()");
        dbMan.saveSchedule(schedule);
    }

    public Schedule getScheduleFromDB() {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getScheduleFromDB()");
        return dbMan.getSchedule();
    }

    public IGFModel  getPopularSeasonAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getPopularSeasonAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getPopularSeasonAnime(page) : alApi.getPopularSeasonAnime(page);
    }

    public IGFModel  getPopularYearAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getPopularYearAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getPopularYearAnime(page) : alApi.getPopularYearAnime(page);
    }

    public IGFModel  getTopSeasonAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopSeasonAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getTopSeasonAnime(page) : alApi.getTopSeasonAnime(page);
    }

    public IGFModel  getTopYearAnime(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopYearAnime(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getTopYearAnime(page) : alApi.getTopYearAnime(page);
    }

    public IGFModel getPopularSeasonManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getPopularSeasonManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getPopularSeasonManga(page) : alApi.getPopularSeasonManga(page);
    }

    public IGFModel getPopularYearManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getPopularYearManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getPopularYearManga(page) : alApi.getPopularYearManga(page);
    }

    public IGFModel getTopSeasonManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopSeasonManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getTopSeasonManga(page) : alApi.getTopSeasonManga(page);
    }

    public IGFModel getTopYearManga(int page) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getTopYearManga(): page=" + page);
        return AccountService.Companion.isMAL() ? malApi.getTopYearManga(page) : alApi.getTopYearManga(page);
    }

    public IGFModel getBrowseAnime(Map<String, String> queries) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getBrowseAnime(): queries=" + queries.toString());
        return AccountService.Companion.isMAL() ? malApi.getBrowseAnime(queries) : alApi.getBrowseAnime(queries);
    }

    public IGFModel getBrowseManga(Map<String, String> queries) {
        AppLog.log(Log.INFO, "Atarashii", "ContentManager.getBrowseManga(): queries=" + queries.toString());
        return AccountService.Companion.isMAL() ? malApi.getBrowseManga(queries) : alApi.getBrowseManga(queries);
    }
}
