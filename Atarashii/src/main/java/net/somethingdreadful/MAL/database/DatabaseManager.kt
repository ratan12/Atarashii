package net.somethingdreadful.MAL.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.gson.Gson
import com.lapism.searchview.SearchItem
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.PrefManager
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.api.BaseModels.Profile
import net.somethingdreadful.MAL.database.Table.*
import org.apache.commons.lang3.StringUtils
import java.util.*


class DatabaseManager(context: Context) {
    private val db: SQLiteDatabase = DatabaseHelper.getInstance(context).writableDatabase

    /**
     * Create tables when an account has been created.
     */
    fun createAccountTable(accountId: Int) {
        val table = Table.create(db)
        table.createRecord(setName(Table.TABLE_ANIME, accountId))
        table.createRecord(setName(Table.TABLE_MANGA, accountId))
        table.createMainLink(setName(Table.TABLE_TAGS, accountId))
        table.createMainLink(setName(Table.TABLE_GENRES, accountId))
        table.createMainLink(setName(Table.TABLE_PRODUCER, accountId))
        table.createProfile(setName(Table.TABLE_PROFILE, accountId))
        table.createFriendlist(setName(Table.TABLE_FRIENDLIST, accountId))
        table.createSchedule(setName(Table.TABLE_SCHEDULE, accountId))
        table.createRelation(setName(Table.TABLE_ANIME_ANIME_RELATIONS, accountId), setName(Table.TABLE_ANIME, accountId), setName(Table.TABLE_ANIME, accountId))
        table.createRelation(setName(Table.TABLE_ANIME_MANGA_RELATIONS, accountId), setName(Table.TABLE_ANIME, accountId), setName(Table.TABLE_MANGA, accountId))
        table.createRelation(setName(Table.TABLE_MANGA_MANGA_RELATIONS, accountId), setName(Table.TABLE_MANGA, accountId), setName(Table.TABLE_MANGA, accountId))
        table.createRelation(setName(Table.TABLE_MANGA_ANIME_RELATIONS, accountId), setName(Table.TABLE_MANGA, accountId), setName(Table.TABLE_ANIME, accountId))
        table.createTags(setName(Table.TABLE_ANIME_TAGS, accountId), setName(Table.TABLE_ANIME, accountId), setName(Table.TABLE_TAGS, accountId))
        table.createTags(setName(Table.TABLE_MANGA_TAGS, accountId), setName(Table.TABLE_MANGA, accountId), setName(Table.TABLE_TAGS, accountId))
        table.createTags(setName(Table.TABLE_ANIME_PERSONALTAGS, accountId), setName(Table.TABLE_ANIME, accountId), setName(Table.TABLE_TAGS, accountId))
        table.createTags(setName(Table.TABLE_MANGA_PERSONALTAGS, accountId), setName(Table.TABLE_MANGA, accountId), setName(Table.TABLE_TAGS, accountId))
        table.createGenre(setName(Table.TABLE_ANIME_GENRES, accountId), setName(Table.TABLE_ANIME, accountId))
        table.createGenre(setName(Table.TABLE_MANGA_GENRES, accountId), setName(Table.TABLE_MANGA, accountId))
        table.createOtherTitles(setName(Table.TABLE_ANIME_MUSIC, accountId), setName(Table.TABLE_ANIME, accountId))
        table.createOtherTitles(setName(Table.TABLE_ANIME_OTHER_TITLES, accountId), setName(Table.TABLE_ANIME, accountId))
        table.createOtherTitles(setName(Table.TABLE_MANGA_OTHER_TITLES, accountId), setName(Table.TABLE_MANGA, accountId))
        table.createProducer(setName(Table.TABLE_ANIME_PRODUCER, accountId), setName(Table.TABLE_PRODUCER, accountId))
    }

    fun removeAccountTable(accountId: Int) {
        val tables = ArrayList<String>()
        val cursor = Query.newQuery(db).selectFrom("*", "sqlite_master").where("type", "table").run()
        if (cursor.moveToFirst()) {
            do {
                val tableName = cursor.getString(1)
                if (tableName.contains("_" + accountId + "_")) {
                    AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.removeAccountTable($accountId): $tableName")
                    tables.add(tableName)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        try {
            db.beginTransaction()
            db.execSQL("DROP TABLE IF EXISTS " + StringUtils.join(tables, ", "))
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.removeAccountTable(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun addAccount(id: Int, username: String, imageUrl: String, website: Int) {
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.addAccount(id=$id, username=$username, website=$website)")
        val cv = ContentValues()
        cv.put(DatabaseHelper.COLUMN_ID, id)
        cv.put("username", username)
        cv.put("imageUrl", imageUrl)
        cv.put("website", website)

        try {
            db.beginTransaction()
            Query.newQuery(db).updateRecord(Table.TABLE_ACCOUNTS, cv, id)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.addAccount(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun removeAccount(id: Int) {
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.removeAccount()")
        try {
            db.beginTransaction()
            Query.newQuery(db).clear(id.toString(), DatabaseHelper.COLUMN_ID, Table.TABLE_ACCOUNTS)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.removeAccount(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun getAccounts(): ArrayList<AccountService.userAccount> {
        val result = ArrayList<AccountService.userAccount>()
        val cursor: Cursor = Query.newQuery(db).selectFrom("*", TABLE_ACCOUNTS).run()
        if (cursor.moveToFirst()) {
            do {
                var account:AccountService.userAccount = AccountService.userAccount()
                result.add(account.create(cursor))
            } while (cursor.moveToNext())
        }
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getAccounts(): got " + cursor.count.toString())
        cursor.close()
        return result
    }

    fun saveAnime(anime: Anime) {
        val cv = listDetails(anime)
        cv.put("duration", anime.duration)
        cv.put("episodes", anime.episodes)
        cv.put("youtubeId", anime.youtubeId)
        if (anime.airing != null) {
            cv.put("airingTime", anime.airing.time)
            cv.put("nextEpisode", anime.airing.nextEpisode)
        }

        // The app is offline
        if (anime.watchedStatus != null) {
            cv.put("watchedStatus", anime.watchedStatus)
            cv.put("watchedEpisodes", anime.watchedEpisodes)
        }

        // AniList does not provide this in the details
        if (AccountService.isMAL) {
            cv.put("watchedStatus", anime.watchedStatus)
            cv.put("watchedEpisodes", anime.watchedEpisodes)
            cv.put("watchingStart", anime.watchingStart)
            cv.put("watchingEnd", anime.watchingEnd)
            cv.put("storage", anime.storage)
            cv.put("storageValue", anime.storageValue)
            cv.put("rewatching", if (anime.rewatching) 1 else 0)
            cv.put("rewatchCount", anime.rewatchCount)
            cv.put("rewatchValue", anime.rewatchValue)

            cv.put("officialSite", anime.externalLinks.officialSite)
            cv.put("animeDB", anime.externalLinks.animeDB)
            cv.put("wikipedia", anime.externalLinks.wikipedia)
            cv.put("animeNewsNetwork", anime.externalLinks.animeNewsNetwork)
        }

        try {
            db.beginTransaction()
            Query.newQuery(db).updateRecord(getName(Table.TABLE_ANIME), cv, anime.id)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_ALTERNATIVE, anime.id, anime.alternativeVersions)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_CHARACTER, anime.id, anime.characterAnime)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SIDE_STORY, anime.id, anime.sideStories)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SPINOFF, anime.id, anime.spinOffs)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SUMMARY, anime.id, anime.summaries)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_MANGA_RELATIONS), DatabaseHelper.RELATION_TYPE_ADAPTATION, anime.id, anime.mangaAdaptations)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_PREQUEL, anime.id, anime.prequels)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SEQUEL, anime.id, anime.sequels)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_PARENT_STORY, anime.id, anime.parentStoryArray)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_OTHER, anime.id, anime.other)
            Query.newQuery(db).updateLink(getName(Table.TABLE_GENRES), getName(Table.TABLE_ANIME_GENRES), anime.id, anime.genres, "genre_id")
            Query.newQuery(db).updateLink(getName(Table.TABLE_GENRES), getName(Table.TABLE_ANIME_TAGS), anime.id, anime.tags, "tag_id")
            Query.newQuery(db).updateLink(getName(Table.TABLE_PRODUCER), getName(Table.TABLE_ANIME_PRODUCER), anime.id, anime.producers, "producer_id")
            Query.newQuery(db).updateLink(getName(Table.TABLE_TAGS), getName(Table.TABLE_ANIME_PERSONALTAGS), anime.id, anime.personalTags, "tag_id")
            Query.newQuery(db).updateTitles(anime.id, true, anime.titleJapanese, anime.titleEnglish, anime.titleSynonyms, anime.titleRomaji)
            Query.newQuery(db).updateMusic(anime.id, anime.openingTheme, anime.endingTheme)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveAnime(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun clearOldRecords(record: GenericRecord, table: String, methodName: String) {
        val lastSync = record.lastSync.time
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.$methodName(): removing records before $lastSync")

        try {
            db.beginTransaction()
            Query.newQuery(db).clearOldRecords(table, lastSync)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager." + methodName + "(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun saveAnimeList(result: ArrayList<Anime>) {
        for (anime in result) {
            saveAnimeList(anime)
        }
        if (result.size > 0)
            clearOldRecords(result[0], getName(Table.TABLE_ANIME), "saveAnimeList")
    }

    /**
     * Save MAL AnimeList records

     * @param anime The Anime model
     */
    private fun saveAnimeList(anime: Anime) {
        val cv = ContentValues()
        cv.put(DatabaseHelper.COLUMN_ID, anime.id)
        cv.put("title", anime.title)
        cv.put("type", anime.type)
        cv.put("status", anime.status)
        cv.put("episodes", anime.episodes)
        cv.put("imageUrl", anime.imageUrl)
        cv.put("watchedEpisodes", anime.watchedEpisodes)
        cv.put("score", anime.score)
        cv.put("watchedStatus", anime.watchedStatus)
        cv.put("lastSync", anime.lastSync.time)

        // AniList details only
        if (!AccountService.isMAL) {
            cv.put("popularity", anime.popularity)
            cv.put("averageScore", anime.averageScore)
            cv.put("priority", anime.priority)
            cv.put("rewatching", if (anime.rewatching) 1 else 0)
            cv.put("notes", anime.notes)
            cv.put("customList", anime.customList)
        }

        try {
            db.beginTransaction()
            Query.newQuery(db).updateRecord(getName(Table.TABLE_ANIME), cv, anime.id)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveAnimeList(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun saveManga(manga: Manga) {
        val cv = listDetails(manga)
        cv.put("chapters", manga.chapters)
        cv.put("volumes", manga.volumes)

        // The app is offline
        if (manga.readStatus != null) {
            cv.put("readStatus", manga.readStatus)
            cv.put("chaptersRead", manga.chaptersRead)
            cv.put("volumesRead", manga.volumesRead)
        }

        // AniList does not provide this in the details
        if (AccountService.isMAL) {
            cv.put("readingStart", manga.readingStart)
            cv.put("readingEnd", manga.readingEnd)
            cv.put("rereading", if (manga.rereading) 1 else 0)
            cv.put("rereadCount", manga.rereadCount)
            cv.put("rereadValue", manga.rereadValue)
        }

        try {
            db.beginTransaction()
            Query.newQuery(db).updateRecord(getName(Table.TABLE_MANGA), cv, manga.id)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_MANGA_MANGA_RELATIONS), DatabaseHelper.RELATION_TYPE_RELATED, manga.id, manga.relatedManga)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_MANGA_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_ADAPTATION, manga.id, manga.animeAdaptations)
            Query.newQuery(db).updateRelation(getName(Table.TABLE_MANGA_MANGA_RELATIONS), DatabaseHelper.RELATION_TYPE_ALTERNATIVE, manga.id, manga.alternativeVersions)
            Query.newQuery(db).updateLink(getName(Table.TABLE_GENRES), getName(Table.TABLE_MANGA_GENRES), manga.id, manga.genres, "genre_id")
            Query.newQuery(db).updateLink(getName(Table.TABLE_GENRES), getName(Table.TABLE_MANGA_TAGS), manga.id, manga.tags, "tag_id")
            Query.newQuery(db).updateLink(getName(Table.TABLE_TAGS), getName(Table.TABLE_MANGA_PERSONALTAGS), manga.id, manga.personalTags, "tag_id")
            Query.newQuery(db).updateTitles(manga.id, false, manga.titleJapanese, manga.titleEnglish, manga.titleSynonyms, manga.titleRomaji)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveManga(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun saveMangaList(result: ArrayList<Manga>) {
        for (manga in result) {
            saveMangaList(manga)
        }
        if (result.size > 0)
            clearOldRecords(result[0], getName(Table.TABLE_MANGA), "saveMangaList")
    }

    /**
     * Save MAL MangaList records

     * @param manga The Anime model
     */
    private fun saveMangaList(manga: Manga) {
        val cv = ContentValues()
        cv.put(DatabaseHelper.COLUMN_ID, manga.id)
        cv.put("title", manga.title)
        cv.put("type", manga.type)
        cv.put("status", manga.status)
        cv.put("chapters", manga.chapters)
        cv.put("volumes", manga.volumes)
        cv.put("imageUrl", manga.imageUrl)
        cv.put("rereading", if (manga.rereading) 1 else 0)
        cv.put("chaptersRead", manga.chaptersRead)
        cv.put("volumesRead", manga.volumesRead)
        cv.put("score", manga.score)
        cv.put("readStatus", manga.readStatus)
        cv.put("customList", manga.customList)
        cv.put("lastSync", manga.lastSync.time)

        try {
            db.beginTransaction()
            Query.newQuery(db).updateRecord(getName(Table.TABLE_MANGA), cv, manga.id)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveMangaList(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    private fun listDetails(record: GenericRecord): ContentValues {
        val cv = ContentValues()
        cv.put(DatabaseHelper.COLUMN_ID, record.id)
        cv.put("title", record.title)
        cv.put("type", record.type)
        cv.put("imageUrl", record.imageUrl)
        cv.put("bannerUrl", record.bannerUrl)
        cv.put("synopsis", record.synopsisString)
        cv.put("status", record.status)
        cv.put("startDate", record.startDate)
        cv.put("endDate", record.endDate)

        // MyAnimeList details only
        if (AccountService.isMAL) {
            cv.put("score", record.score)
            cv.put("priority", record.priority)
            cv.put("averageScoreCount", record.averageScoreCount)
            cv.put("rank", record.rank)
            cv.put("notes", record.notes)
            cv.put("favoritedCount", record.favoritedCount)
        } else if (record.notes != null) { // Offline details
            cv.put("notes", record.notes)
        } else { // AniList details only
            cv.put("lsPlanned", record.listStats.planned)
            cv.put("lsReadWatch", record.listStats.readWatch)
            cv.put("lsCompleted", record.listStats.completed)
            cv.put("lsOnHold", record.listStats.onHold)
            cv.put("lsDropped", record.listStats.dropped)

            if (-1 < record.score) {
                cv.put("score", record.score)
                cv.put("customList", record.customList)
            }
        }
        cv.put("classification", record.classification)
        cv.put("averageScore", record.averageScore)
        cv.put("popularity", record.popularity)
        cv.put("dirty", if (record.dirty != null) Gson().toJson(record.dirty) else null)
        cv.put("createFlag", record.createFlag)
        cv.put("deleteFlag", record.deleteFlag)
        return cv
    }

    val suggestions: List<SearchItem>
        get() {
            val result = ArrayList<SearchItem>()
            result.addAll(getSuggestions(Query.newQuery(db).selectFrom("title", getName(Table.TABLE_ANIME)).run()))
            result.addAll(getSuggestions(Query.newQuery(db).selectFrom("title", getName(Table.TABLE_MANGA)).run()))
            return result
        }

    private fun getSuggestions(cursor: Cursor): List<SearchItem> {
        val result = ArrayList<SearchItem>()
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getSuggestions(): got " + cursor.count.toString())
        if (cursor.moveToFirst()) {
            do
                result.add(SearchItem(cursor.getString(cursor.getColumnIndex("title"))))
            while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun getAnime(id: Int): Anime? {
        val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_ANIME)).where(DatabaseHelper.COLUMN_ID, id.toString()).run()

        var result: Anime? = null
        if (cursor.moveToFirst()) {
            result = Anime.fromCursor(cursor)
            result!!.titleEnglish = Query.newQuery(db).getTitles(result.id, true, DatabaseHelper.TITLE_TYPE_ENGLISH)
            result.titleSynonyms = Query.newQuery(db).getTitles(result.id, true, DatabaseHelper.TITLE_TYPE_SYNONYM)
            result.titleJapanese = Query.newQuery(db).getTitles(result.id, true, DatabaseHelper.TITLE_TYPE_JAPANESE)
            result.titleRomaji = Query.newQuery(db).getTitles(result.id, true, DatabaseHelper.TITLE_TYPE_ROMAJI)
            result.openingTheme = Query.newQuery(db).getMusic(result.id, DatabaseHelper.MUSIC_TYPE_OPENING)
            result.endingTheme = Query.newQuery(db).getMusic(result.id, DatabaseHelper.MUSIC_TYPE_ENDING)
            result.alternativeVersions = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_ALTERNATIVE, true)
            result.characterAnime = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_CHARACTER, true)
            result.sideStories = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SIDE_STORY, true)
            result.spinOffs = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SPINOFF, true)
            result.summaries = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SUMMARY, true)
            result.mangaAdaptations = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_MANGA_RELATIONS), DatabaseHelper.RELATION_TYPE_ADAPTATION, false)
            result.prequels = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_PREQUEL, true)
            result.sequels = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_SEQUEL, true)
            result.parentStoryArray = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_PARENT_STORY, true)
            result.other = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_ANIME_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_OTHER, true)
            result.genres = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_GENRES), getName(Table.TABLE_ANIME_GENRES), "genre_id", true)
            result.tags = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_TAGS), getName(Table.TABLE_ANIME_TAGS), "tag_id", true)
            result.personalTags = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_TAGS), getName(Table.TABLE_ANIME_PERSONALTAGS), "tag_id", true)
            result.producers = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_PRODUCER), getName(Table.TABLE_ANIME_PRODUCER), "producer_id", true)
        }
        cursor.close()
        GenericRecord.fromCursor = false;
        return result
    }

    fun getManga(id: Int): Manga? {
        val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_MANGA)).where(DatabaseHelper.COLUMN_ID, id.toString()).run()

        var result: Manga? = null
        if (cursor.moveToFirst()) {
            result = Manga.fromCursor(cursor)
            result!!.titleEnglish = Query.newQuery(db).getTitles(result.id, false, DatabaseHelper.TITLE_TYPE_ENGLISH)
            result.titleSynonyms = Query.newQuery(db).getTitles(result.id, false, DatabaseHelper.TITLE_TYPE_SYNONYM)
            result.titleJapanese = Query.newQuery(db).getTitles(result.id, false, DatabaseHelper.TITLE_TYPE_JAPANESE)
            result.titleRomaji = Query.newQuery(db).getTitles(result.id, false, DatabaseHelper.TITLE_TYPE_ROMAJI)
            result.genres = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_GENRES), getName(Table.TABLE_MANGA_GENRES), "genre_id", false)
            result.tags = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_TAGS), getName(Table.TABLE_MANGA_TAGS), "tag_id", false)
            result.personalTags = Query.newQuery(db).getArrayList(result.id, getName(Table.TABLE_TAGS), getName(Table.TABLE_MANGA_PERSONALTAGS), "tag_id", false)
            result.alternativeVersions = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_MANGA_MANGA_RELATIONS), DatabaseHelper.RELATION_TYPE_ALTERNATIVE, false)
            result.relatedManga = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_MANGA_MANGA_RELATIONS), DatabaseHelper.RELATION_TYPE_RELATED, false)
            result.animeAdaptations = Query.newQuery(db).getRelation(result.id, getName(Table.TABLE_MANGA_ANIME_RELATIONS), DatabaseHelper.RELATION_TYPE_ADAPTATION, true)
        }
        cursor.close()
        GenericRecord.fromCursor = false;
        return result
    }

    val dirtyAnimeList: ArrayList<Anime>
        get() {
            val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_ANIME)).isNotNull("dirty").run()
            return getAnimeList(cursor)
        }

    val dirtyMangaList: ArrayList<Manga>
        get() {
            val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_MANGA)).isNotNull("dirty").run()
            return getMangaList(cursor)
        }

    private fun regCustomList(type: String): String {
        var reg = ""
        val listNumber = Integer.parseInt(type.replace(GenericRecord.CUSTOMLIST, ""))
        for (i in 1..15) {
            if (i == listNumber)
                reg += "1"
            else
                reg += "_"
        }
        return reg
    }

    fun getAnimeList(type: String, sortType: Int, inv: Int): IGFModel {
        val cursor: Cursor
        val query = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_ANIME))
        if (type.contains(GenericRecord.CUSTOMLIST)) {
            cursor = sort(query.like("customList", regCustomList(type)), sortType, inv)
        } else {
            when (type) {
                "" // All
                -> cursor = sort(query.isNotNull("type"), sortType, inv)
                "rewatching" // rewatching/rereading
                -> cursor = sort(query.whereEqGr("rewatching", "1"), sortType, inv)
                else // normal lists
                -> cursor = sort(query.where("watchedStatus", type), sortType, inv)
            }
        }
        return getAnimeList(cursor, sortType)
    }

    fun getMangaList(type: String, sortType: Int, inv: Int): IGFModel {
        var sortType = sortType
        sortType = if (sortType == 5) -5 else sortType
        val cursor: Cursor
        val query = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_MANGA))
        if (type.contains(GenericRecord.CUSTOMLIST)) {
            cursor = sort(query.like("customList", regCustomList(type)), sortType, inv)
        } else {
            when (type) {
                "" // All
                -> cursor = sort(query.isNotNull("type"), sortType, inv)
                "rereading" // rewatching/rereading
                -> cursor = sort(query.whereEqGr("rereading", "1"), sortType, inv)
                else // normal lists
                -> cursor = sort(query.where("readStatus", type), sortType, inv)
            }
        }
        return getMangaList(cursor, sortType)
    }

    /*
     * Do not forget to modify the IGF sortList method also!
     */
    private fun sort(query: Query, sortType: Int, inverse: Int): Cursor {
        when (sortType) {
            1 -> return query.OrderBy(inverse, "title").run()
            2 -> return query.OrderBy(if (inverse == 2) 1 else 2, "score").andOrderBy(inverse, "title").run()
            3 -> return query.OrderBy(inverse, "type").andOrderBy(inverse, "title").run()
            4 -> return query.OrderBy(inverse, "status").andOrderBy(inverse, "title").run()
            5 -> return query.OrderBy(inverse, "watchedEpisodes").andOrderBy(inverse, "title").run()
            -5 -> return query.OrderBy(inverse, "chaptersRead").andOrderBy(inverse, "title").run()
            else -> return query.OrderBy(inverse, "title").run()
        }
    }

    private fun getAnimeList(cursor: Cursor): ArrayList<Anime> {
        val result = ArrayList<Anime>()
        GenericRecord.fromCursor = true;
        if (cursor.moveToFirst()) {
            do
                result.add(Anime.fromCursor(cursor))
            while (cursor.moveToNext())
        }
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getAnimeList(): got " + cursor.count.toString())
        cursor.close()
        GenericRecord.fromCursor = false;
        return result
    }

    fun getAnimeList(cursor: Cursor, sortType: Int): IGFModel {
        val igfModel = IGFModel(sortType)
        if (cursor.moveToFirst()) {
            do
                igfModel.AnimeFromCursor(cursor)
            while (cursor.moveToNext())
        }
        cursor.close()
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getAnimeList(): got " + cursor.count.toString())
        return igfModel
    }

    fun getMangaList(cursor: Cursor, sortType: Int): IGFModel {
        val igfModel = IGFModel(sortType)
        val chapters = !PrefManager.getUseSecondaryAmountsEnabled()
        if (cursor.moveToFirst()) {
            do
                igfModel.MangaFromCursor(cursor, chapters)
            while (cursor.moveToNext())
        }
        cursor.close()
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getMangaList(): got " + cursor.count.toString())
        return igfModel
    }

    fun getMangaList(cursor: Cursor): ArrayList<Manga> {
        val result = ArrayList<Manga>()
        GenericRecord.fromCursor = true;
        if (cursor.moveToFirst()) {
            do
                result.add(Manga.fromCursor(cursor))
            while (cursor.moveToNext())
        }
        cursor.close()
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getMangaList(): got " + cursor.count.toString())
        GenericRecord.fromCursor = false;

        return result
    }

    fun cleanupAnimeTable() {
        db.rawQuery("DELETE FROM " + getName(Table.TABLE_ANIME) + " WHERE " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + getName(Table.TABLE_ANIME_ANIME_RELATIONS) + ") AND " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + getName(Table.TABLE_MANGA_ANIME_RELATIONS) + ")", null)
    }

    fun cleanupMangaTable() {
        db.rawQuery("DELETE FROM " + getName(Table.TABLE_MANGA) + " WHERE " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + getName(Table.TABLE_MANGA_MANGA_RELATIONS) + ") AND " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + getName(Table.TABLE_MANGA_ANIME_RELATIONS) + ")", null)
    }

    val friendList: ArrayList<Profile>
        get() {
            val result = ArrayList<Profile>()
            val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_FRIENDLIST)).OrderBy(1, "username").run()

            if (cursor.moveToFirst()) {
                do
                    result.add(Profile.friendFromCursor(cursor))
                while (cursor.moveToNext())
            }
            cursor.close()
            return result
        }

    fun saveFriendList(list: ArrayList<Profile>) {
        try {
            db.beginTransaction()
            for (profile in list) {
                val cv = ContentValues()
                cv.put("username", profile.username.capitalize())
                cv.put("imageUrl", profile.imageUrl)
                cv.put("lastOnline", if (AccountService.isMAL) profile.details.lastOnline else "")
                Query.newQuery(db).updateRecord(getName(Table.TABLE_FRIENDLIST), cv, profile.username)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveFriendList(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    val profile: Profile
        get() {
            val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_PROFILE)).run()
            var profile: Profile? = null
            if (cursor.moveToFirst())
                profile = Profile.fromCursor(cursor)
            cursor.close()
            return profile!!
        }

    fun saveProfile(profile: Profile) {
        val cv = ContentValues()

        cv.put("username", profile.username)
        cv.put("imageUrl", profile.imageUrl)
        cv.put("imageUrlBanner", profile.imageUrlBanner)
        cv.put("notifications", profile.notifications)
        cv.put("about", profile.about)

        // AniList also supports these
        cv.put("AnimetimeDays", profile.animeStats.timeDays)
        cv.put("Mangacompleted", profile.mangaStats.completed)

        if (AccountService.isMAL) {
            cv.put("lastOnline", profile.details.lastOnline)
            cv.put("status", profile.details.status)
            cv.put("gender", profile.details.gender)
            cv.put("birthday", profile.details.birthday)
            cv.put("location", profile.details.location)
            cv.put("website", profile.details.website)
            cv.put("joinDate", profile.details.joinDate)
            cv.put("accessRank", profile.details.accessRank)

            cv.put("animeListViews", profile.details.animeListViews)
            cv.put("mangaListViews", profile.details.mangaListViews)
            cv.put("forumPosts", profile.details.forumPosts)
            cv.put("comments", profile.details.comments)

            cv.put("Animewatching", profile.animeStats.watching)
            cv.put("Animecompleted", profile.animeStats.completed)
            cv.put("AnimeonHold", profile.animeStats.onHold)
            cv.put("Animedropped", profile.animeStats.dropped)
            cv.put("AnimeplanToWatch", profile.animeStats.planToWatch)
            cv.put("AnimetotalEntries", profile.animeStats.totalEntries)

            cv.put("MangatimeDays", profile.mangaStats.timeDays)
            cv.put("Mangareading", profile.mangaStats.reading)
            cv.put("MangaonHold", profile.mangaStats.onHold)
            cv.put("Mangadropped", profile.mangaStats.dropped)
            cv.put("MangaplanToRead", profile.mangaStats.planToRead)
            cv.put("MangatotalEntries", profile.mangaStats.totalEntries)
        }

        try {
            db.beginTransaction()
            Query.newQuery(db).updateRecord(getName(Table.TABLE_PROFILE), cv, profile.username)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveProfile(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    fun deleteAnime(id: Int) {
        var result = false
        try {
            db.beginTransaction()
            result = db.delete(getName(Table.TABLE_ANIME), DatabaseHelper.COLUMN_ID + " = ?", arrayOf(id.toString())) == 1
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.deleteAnime(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
        if (result)
            cleanupAnimeTable()
    }

    fun deleteManga(id: Int) {
        var result = false
        try {
            db.beginTransaction()
            result = db.delete(getName(Table.TABLE_MANGA), DatabaseHelper.COLUMN_ID + " = ?", arrayOf(id.toString())) == 1
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.deleteManga(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
        if (result)
            cleanupMangaTable()
    }

    fun saveSchedule(schedule: Schedule) {
        Query.newQuery(db).clear(getName(Table.TABLE_SCHEDULE))
        saveScheduleDay(schedule.monday, 2)
        saveScheduleDay(schedule.tuesday, 3)
        saveScheduleDay(schedule.wednesday, 4)
        saveScheduleDay(schedule.thursday, 5)
        saveScheduleDay(schedule.friday, 6)
        saveScheduleDay(schedule.saturday, 7)
        saveScheduleDay(schedule.sunday, 1)
    }

    private fun saveScheduleDay(list: ArrayList<Anime>, day: Int) {
        try {
            db.beginTransaction()
            for (anime in list) {
                val cv = ContentValues()
                cv.put(DatabaseHelper.COLUMN_ID, anime.id)
                cv.put("title", anime.title)
                cv.put("imageUrl", anime.imageUrl)
                cv.put("type", anime.type)
                cv.put("episodes", anime.episodes)
                cv.put("avarageScore", anime.averageScore)
                cv.put("averageScoreCount", anime.averageScoreCount)
                if (anime.airing != null && anime.airing.time != null)
                    cv.put("broadcast", anime.airing.time)
                cv.put("day", day)
                Query.newQuery(db).updateRecord(getName(Table.TABLE_SCHEDULE), cv, anime.id)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveScheduleDay(): " + e.message)
            AppLog.logException(e)
        } finally {
            db.endTransaction()
        }
    }

    private fun getScheduleDay(day: Int): ArrayList<Anime> {
        val result = ArrayList<Anime>()
        val cursor = Query.newQuery(db).selectFrom("*", getName(Table.TABLE_SCHEDULE)).where("day", day.toString()).run()

        if (cursor.moveToFirst()) {
            do
                result.add(Schedule.fromCursor(cursor))
            while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    val schedule: Schedule
        get() {
            val schedule = Schedule()
            schedule.monday = getScheduleDay(2)
            schedule.tuesday = getScheduleDay(3)
            schedule.wednesday = getScheduleDay(4)
            schedule.thursday = getScheduleDay(5)
            schedule.friday = getScheduleDay(6)
            schedule.saturday = getScheduleDay(7)
            schedule.sunday = getScheduleDay(1)
            return schedule
        }
}
