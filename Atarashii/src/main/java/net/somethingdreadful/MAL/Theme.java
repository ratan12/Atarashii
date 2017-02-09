package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static net.somethingdreadful.MAL.AppLog.initFabric;

public class Theme extends Application {
    public static boolean darkTheme;
    private static float density;
    private Configuration config;
    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        net.somethingdreadful.MAL.account.AccountService.create(getApplicationContext());
        net.somethingdreadful.MAL.PrefManager.create(getApplicationContext());
        context = getApplicationContext();

        initFabric(context);

        Locale locale = net.somethingdreadful.MAL.PrefManager.getLocale();
        darkTheme = net.somethingdreadful.MAL.PrefManager.getDarkTheme();
        config = new Configuration();
        config.locale = locale;
        setLanguage(); //Change language when it is started
        AppLog.setCrashData("Language", locale.toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLanguage(); //Change language after orientation.
    }

    /**
     * Checks if the device is in portrait orientation.
     *
     * @return Boolean true or false
     */
    public static boolean isPortrait() {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Changes the language to the preferred one.
     */
    private void setLanguage() {
        try {
            Resources res = getBaseContext().getResources();
            res.updateConfiguration(config, res.getDisplayMetrics());
        } catch (Exception e) {
            AppLog.logTaskCrash("Theme", "setLanguage", e);
        }
    }

    /**
     * Init the actionbar right.
     * This method should only be used for activities without tabs.
     *
     * @param activity The view activity
     */
    public static void setActionBar(AppCompatActivity activity) {
        Toolbar mViewPager = (Toolbar) activity.findViewById(R.id.pager);
        activity.setSupportActionBar(mViewPager);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Init the actionbar right.
     * This method should only be used for activities with tabs.
     *
     * @param activity The view activity
     * @param adapter  The page adapter
     * @return FragmentPagerAdapter The page adapter
     */
    public static FragmentPagerAdapter setActionBar(AppCompatActivity activity, FragmentPagerAdapter adapter) {
        ViewPager viewPager = (ViewPager) activity.findViewById(R.id.pager);
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.actionbar);
        TabLayout tabs = (TabLayout) activity.findViewById(R.id.tabs);

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(viewPager.getAdapter().getCount());

        tabs.setupWithViewPager(viewPager);
        return adapter;
    }

    /**
     * Get the display density.
     *
     * @return Float The display density
     */
    private static Float getDensity() {
        if (density == 0)
            density = (context.getResources().getDisplayMetrics().densityDpi / 160f);
        return density;
    }

    /**
     * Convert dp to pixels.
     *
     * @param number The number in dp to convert in pixels
     * @return int The converted dp in pixels
     */
    public static int convert(int number) {
        return Math.round(getDensity() * number);
    }

    /**
     * Convert dp to pixels.
     *
     * @param number The number in dp to convert in pixels
     * @return float The converted dp in pixels
     */
    public static Float floatConvert(int number) {
        return getDensity() * number;
    }

    /**
     * This will apply the right theme and background.
     *
     * @param activity The activity which should be themed
     * @param view     The main view id
     * @param card     If the contents contains a card
     */
    public static void setTheme(Activity activity, int view, boolean card) {
        if (view != 0)
            activity.setContentView(view);
        if (darkTheme) {
            activity.setTheme(R.style.AtarashiiDarkBg);
            activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, card ? R.color.bg_dark_card : R.color.bg_dark));
        } else {
            activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, card ? R.color.bg_light_card : R.color.bg_light));
        }
    }

    /**
     * Init navigationDrawer properly.
     *
     * @param navigationView The navigationView
     * @param activity       The current activity
     * @param listener       The listener which can be null
     */
    public static void setNavDrawer(NavigationView navigationView, Activity activity, View.OnClickListener listener) {
        try {
            View view = navigationView.getHeaderView(0);
            String username = net.somethingdreadful.MAL.account.AccountService.getUsername();
            ImageView image = (ImageView) view.findViewById(R.id.Image);
            ImageView image2 = (ImageView) view.findViewById(R.id.NDimage);
            ((TextView) view.findViewById(R.id.siteName)).setText(activity.getString(net.somethingdreadful.MAL.account.AccountService.isMAL() ? R.string.init_hint_myanimelist : R.string.init_hint_anilist));
            ((TextView) view.findViewById(R.id.name)).setText(username);

            // Apply dark theme if an user enabled it in the settings.
            if (Theme.darkTheme) {
                int[][] states = new int[][]{
                        new int[]{-android.R.attr.state_checked}, // unchecked
                        new int[]{android.R.attr.state_checked} // checked
                };

                int[] colors = new int[]{
                        ContextCompat.getColor(activity, R.color.bg_light_card),
                        ContextCompat.getColor(activity, R.color.primary)
                };

                ColorStateList myList = new ColorStateList(states, colors);
                navigationView.setBackgroundColor(ContextCompat.getColor(activity, R.color.bg_dark));
                navigationView.setItemTextColor(myList);
                navigationView.setItemIconTintList(myList);
            }

            // init images
            Glide.with(activity)
                    .load(net.somethingdreadful.MAL.PrefManager.getProfileImage())

                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(image);
            if (net.somethingdreadful.MAL.PrefManager.getNavigationBackground() != null)
                Glide.with(activity)
                        .load(net.somethingdreadful.MAL.PrefManager.getNavigationBackground())
                        .placeholder(R.drawable.atarashii_background)
                        .error(R.drawable.atarashii_background)
                        .into(image2);
            if (listener != null) {
                image.setOnClickListener(listener);
                image2.setOnClickListener(listener);
            }
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "Theme.setNavDrawer(): " + e.getMessage());
        }
    }

    /**
     * Set a background with the default card theme.
     *
     * @param c    The context
     * @param view The view which should use this drawable
     */
    public static void setBackground(Context c, View view) {
        setBackground(c, view, getDrawable());
    }

    /**
     * Set the background of a view.
     *
     * @param c    The context
     * @param view The view which should use this drawable
     * @param id   The drawable/color id of the wanted color/drawable
     */
    public static void setBackground(Context c, View view, int id) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(c, id));
        } else {
            view.setBackground(ContextCompat.getDrawable(c, id));
        }
    }

    /**
     * Get the default drawable.
     *
     * @return int The id of the default card drawable
     */
    private static int getDrawable() {
        return darkTheme ? R.drawable.highlite_dark : R.drawable.highlite;
    }

    /**
     * Create a snackbar which handles the queue.
     *
     * @param activity    The activity where the snackbar should be shown
     * @param stringResID The string resource ID
     */
    public static void Snackbar(Activity activity, int stringResID) {
        if (activity != null) {
            Snackbar snack = Snackbar.make(activity.getWindow().getDecorView(), stringResID, Snackbar.LENGTH_LONG);
            TextView tv = (TextView) snack.getView().findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            snack.show();
        }
    }

    /**
     * Converts a score to the display format
     *
     * @param score The score that should be converted
     * @return The string to display
     */
    public static String getDisplayScore(float score) {
        switch (net.somethingdreadful.MAL.PrefManager.getScoreType()) {
            case 0:
                int score1 = (int) (net.somethingdreadful.MAL.account.AccountService.isMAL() ? score : Math.floor(score / 10));
                return score1 > 0 ? String.valueOf(score1) : "?";
            case 1:
                return score > 0 ? String.valueOf((int) score) : "?";
            case 2:
                if (score <= 0)
                    return "?";
                else if (score <= 29)
                    return "1";
                else if (score <= 49)
                    return "2";
                else if (score <= 69)
                    return "3";
                else if (score <= 89)
                    return "4";
                else
                    return "5";
            case 3:
                if (score <= 0)
                    return "?";
                else if (score <= 30)
                    return ":(";
                else if (score <= 60)
                    return ":|";
                else
                    return ":)";
            case 4:
                float score5 = score / 10;
                return score5 > 0.0 ? String.valueOf(score5) : "?";
            default:
                return "?";
        }
    }

    /**
     * Display the right format for AL API commands.
     * <p>
     * 0. 0 - 10
     * 1. 0 - 100
     * 2. 0 - 5
     * 3. :( & :| & :)
     * 4. 0.0 - 10.0
     *
     * @param score The score from the DB.
     * @return The API score
     */
    public static String getALAPIScore(float score) {
        String scoreStr = getDisplayScore(score);
        if (scoreStr.contains("?"))
            switch (net.somethingdreadful.MAL.PrefManager.getScoreType()) {
                case 0:
                case 1:
                case 2:
                case 4:
                    return "0";
                case 3:
                    return "";
                default:
                    return "?";
            }
        else
            return scoreStr;
    }

    /**
     * Converts a score to the raw format
     *
     * @param score The score that should be converted
     * @return The raw integer
     */
    public static int getRawScore(String score) {
        if (score.equals(""))
            return 0;
        switch (net.somethingdreadful.MAL.PrefManager.getScoreType()) {
            case 0:
                return TextUtils.isDigitsOnly(score) ? (int) (Double.parseDouble(score) * 10) : 0;
            case 1:
                return TextUtils.isDigitsOnly(score) ? Integer.parseInt(score) : 0;
            case 2:
                return TextUtils.isDigitsOnly(score) ? Integer.parseInt(score) * 20 : 0;
            case 3:
                switch (score) {
                    case ":(":
                        return 30;
                    case ":|":
                        return 60;
                    case ":)":
                        return 100;
                    default:
                        return 0;
                }
            case 4:
                String scoreStr = score.replaceFirst("\\.", "").replaceFirst(",", "");
                return TextUtils.isDigitsOnly(scoreStr) ? (int) (Double.parseDouble(score.replace(",", ".")) * 10) : 0;
            default:
                return TextUtils.isDigitsOnly(score) ? (int) (Double.parseDouble(score) * 10) : 0;
        }
    }
}
