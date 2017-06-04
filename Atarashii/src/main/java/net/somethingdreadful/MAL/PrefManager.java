package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

public class PrefManager {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefEditor;
    private static Context context;

    @SuppressLint("CommitPrefEdits")
    public static void create(Context mContext) {
        context = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefEditor = prefs.edit();
    }

    public static boolean isCreated() {
        return context != null;
    }

    /**
     * Remove the preferences.
     * <p/>
     * Reset all the preferences
     */
    public static void clear() {
        prefEditor.clear();
        prefEditor.commit();
    }

    /**
     * Returns if the app should provide NSFW content.
     */
    public static boolean getNSFWEnabled() {
        return prefs.getBoolean("adultcontent", false);
    }

    /**
     * Returns the custom share text.
     *
     * @return String The custom share text that the app should use.
     * @see DetailView
     */
    public static String getCustomShareText() {
        return prefs.getString("customShareText", context.getString(R.string.preference_default_customShareText));
    }

    /**
     * Returns if we should use a Listview.
     *
     * @return boolean If true the app will use a Listview
     */
    public static boolean getTraditionalListEnabled() {
        return prefs.getBoolean("traditionalList", false);
    }

    /**
     * Returns if we should use a Listview.
     *
     * @return boolean If true the app will use a Listview
     */
    public static boolean getListEnabled() {
        return prefs.getBoolean("traditionalList", false);
    }

    /**
     * Returns if the app should use Volumes instead of Chapters.
     *
     * @return boolean If it is true the app will use Volumes
     */
    public static boolean getUseSecondaryAmountsEnabled() {
        return prefs.getBoolean("displayVolumes", false);
    }

    /**
     * Returns if auto synchronisation is enabled.
     *
     * @return boolean This returns true when auto synchronisation is enabled
     * @see Settings
     */
    public static boolean getSyncEnabled() {
        return prefs.getBoolean("synchronisation", false);
    }

    /**
     * Returns the auto synchronisation interval.
     *
     * @return int The auto synchronisation interval in seconds
     * @see Settings
     */
    public static int getSyncTime() {
        return Integer.parseInt(prefs.getString("synchronisation_time", "60"));
    }

    /**
     * Returns the locale of the language that this app should use.
     *
     * @return Locale The locale of the language that the app should use
     * @see Theme
     */
    public static Locale getLocale() {
        String localeName = prefs.getString("locale", Locale.getDefault().toString());
        Locale locale;
        switch (localeName) {
            case "pt-br":
                locale = new Locale("pt", "PT");
                break;
            case "pt-pt":
                locale = new Locale("pt", "BR");
                break;
            default:
                locale = new Locale(localeName);
                break;
        }
        return locale;
    }

    /**
     * Returns if the app should synchronise the Anime/Manga records.
     *
     * @return boolean If all the records should be synced
     * @see Home
     */
    public static boolean getForceSync() {
        return prefs.getBoolean("ForceSync", false);
    }

    /**
     * Set the force synchronisation, if true all the records will be synchronised.
     *
     * @param force This will determine if all the records should be synchronised
     * @see FirstTimeInit
     * @see Home
     */
    public static void setForceSync(boolean force) {
        prefEditor.putBoolean("ForceSync", force);
    }

    /**
     * Returns if the app should disable the coloured text and use black.
     *
     * @return boolean If it should be black
     * @see ProfileActivity
     */
    public static boolean getTextColor() {
        return prefs.getBoolean("text_colours", false);
    }

    /**
     * This will disable the text colors.
     *
     * @param disable True if the text colors should be default
     * @see ProfileActivity
     */
    public static void setTextColor(boolean disable) {
        prefEditor.putBoolean("text_colours", disable);
    }

    /**
     * Returns if the app should hide the anime stats in all profiles.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public static boolean getHideAnime() {
        return prefs.getBoolean("A_hide", false);
    }

    /**
     * Returns if the app should hide the manga stats in all profiles.
     *
     * @return boolean If it should be hidden
     * @see ProfileActivity
     */
    public static boolean getHideManga() {
        return prefs.getBoolean("M_hide", false);
    }

    /**
     * Returns the default list that will open on start.
     *
     * @return int The number of the list that should get loaded.
     * @see Home
     */
    public static int getDefaultList() {
        return Integer.parseInt(prefs.getString("defList", "1"));
    }

    /**
     * Returns the url of the navigation drawer background.
     *
     * @return string The url of the image.
     * @see Home
     */
    public static String getNavigationBackground() {
        return prefs.getString("navigationDrawer_image", null);
    }

    /**
     * Set the navigation drawer background.
     *
     * @param image The URL
     */
    public static void setNavigationBackground(String image) {
        prefEditor.putString("navigationDrawer_image", image);
    }

    /**
     * Returns the profile image url of the logged in user.
     *
     * @return string The url of the image.
     */
    public static String getProfileImage() {
        return prefs.getString("profile_image", null);
    }

    /**
     * Set the profile image url.
     *
     * @param image The URL
     */
    public static void setProfileImage(String image) {
        prefEditor.putString("profile_image", image);
    }

    /**
     * Returns the width of an cover.
     *
     * @return int The width of an cover.
     */
    public static int getCoverWidth() {
        return prefs.getInt("coverWidth", 500);
    }

    /**
     * Set the width of an cover.
     *
     * @param coverWidth The width of an cover
     */
    public static void setCoverWidth(int coverWidth) {
        prefEditor.putInt("coverWidth", coverWidth).commit();
    }

    /**
     * Returns the height of an cover.
     *
     * @return int The height of an cover.
     */
    public static int getCoverHeight() {
        return prefs.getInt("coverHeight", 500);
    }

    /**
     * Set the height of an cover.
     *
     * @param coverHeight The height of an cover
     */
    public static void setCoverHeight(int coverHeight) {
        prefEditor.putInt("coverHeight", coverHeight).commit();
    }

    /**
     * Set the score display type.
     * <p/>
     * 0. 0 - 10
     * 1. 0 - 100
     * 2. 0 - 5
     * 3. :( & :| & :)
     * 4. 0.0 - 10.0
     *
     * @param type The type number
     */
    public static void setScoreType(int type) {
        prefEditor.putInt("Score_type", type);
    }

    /**
     * Returns the score type.
     *
     * @return int the score type to display.
     */
    public static int getScoreType() {
        return prefs.getInt("Score_type", 0);
    }

    /**
     * Returns the max score.
     *
     * @return int the score type to display.
     */
    public static int getMaxScore() {
        switch (getScoreType()) {
            case 0:
                return 10;
            case 1:
                return 100;
            case 2:
                return 5;
            default:
                return 10;
        }
    }

    /**
     * Returns the list where a record should be added.
     *
     * @return int the list type to display.
     */
    public static int getAddList() {
        return Integer.parseInt(prefs.getString("addList", "1"));
    }

    /**
     * Returns if a record will update the date automatically.
     *
     * @return boolean True if it should update.
     */
    public static boolean getAutoDateSetter() {
        return prefs.getBoolean("autoDate", true);
    }

    /**
     * Commit all the changed made.
     */
    public static void commitChanges() {
        prefEditor.commit();
    }

    /**
     * Get the prefs for the dark theme.
     *
     * @return boolean true if the user wants a dark theme
     */
    public static boolean getDarkTheme() {
        return prefs.getBoolean("darkTheme", false);
    }

    /**
     * Get the amount of the IGF columns.
     */
    public static int getIGFColumns() {
        return prefs.getInt(Theme.isPortrait() ? "IGFcolumnsportrait" : "IGFcolumnslandscape", 0);
    }

    /**
     * Get the amount of the IGF columns.
     *
     * @param portrait The screen orientation
     */
    public static int getIGFColumns(boolean portrait) {
        int prefvalue = prefs.getInt(portrait ? "IGFcolumnsportrait" : "IGFcolumnslandscape", 0);
        if (prefvalue == 0) {
            int screen;
            if (Theme.isPortrait() && portrait || !Theme.isPortrait() && !portrait)
                screen = Theme.convert(Theme.context.getResources().getConfiguration().screenWidthDp);
            else
                screen = Theme.convert(Theme.context.getResources().getConfiguration().screenHeightDp);
            prefvalue = (int) Math.ceil(screen / Theme.floatConvert(225));
        }
        return prefvalue;
    }

    /**
     * set the amount of the IGF columns.
     *
     * @param columns The amount of columns
     */
    public static void setIGFColumns(int columns) {
        prefEditor.putInt(Theme.isPortrait() ? "IGFcolumnsportrait" : "IGFcolumnslandscape", columns);
    }

    /**
     * Get the amount of the IGF columns.
     *
     * @param columns  The amount of columns
     * @param portrait The screen orientation
     */
    public static void setIGFColumns(int columns, boolean portrait) {
        prefEditor.putInt(portrait ? "IGFcolumnsportrait" : "IGFcolumnslandscape", columns);
    }

    /**
     * Mark that the auto-sync completed refreshing the records.
     *
     * @param done Set on true when it was completed
     */
    public static void setAutosyncDone(boolean done) {
        prefEditor.putBoolean("AutosyncStatus", done).commit();
    }

    /**
     * Get the status of the last auto-sync.
     *
     * @return boolean The status in true or false
     */
    public static boolean getAutosyncDone() {
        return prefs.getBoolean("AutosyncStatus", false);
    }

    /**
     * Get if the user does not want to see the tabs.
     *
     * @return boolean If the tabs should be hidden in true or false
     */
    public static boolean getHideHomeTabs() {
        return prefs.getBoolean("hideTabs", false);
    }

    /**
     * Get AniList title language.
     *
     * @return String The language name
     */
    public static String getTitleNameLang() {
        return prefs.getString("titleNameLang", "romaji");
    }

    /**
     * set AniList title language.
     */
    public static void setTitleNameLang(String titleName) {
        prefEditor.putString("titleNameLang", titleName);
    }

    /**
     * set AniList custom anime lists.
     */
    public static void setCustomAnimeList(ArrayList<String> custom_list_anime) {
        prefEditor.putString("customAnimeList", StringUtils.join(custom_list_anime, ","));
    }

    /**
     * set AniList custom manga lists.
     */
    public static void setCustomMangaList(ArrayList<String> custom_list_manga) {
        prefEditor.putString("customMangaList", StringUtils.join(custom_list_manga, ","));
    }

    public static String[] getCustomAnimeList() {
        return StringUtils.split(prefs.getString("customAnimeList", ""), ",");
    }

    public static String[] getCustomMangaList() {
        return StringUtils.split(prefs.getString("customMangaList", ""), ",");
    }
}
