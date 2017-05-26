package net.somethingdreadful.MAL.cover;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Setter;

public class CoverFragment extends Fragment implements NetworkTask.NetworkTaskListener, SwipeRefreshLayout.OnRefreshListener, QuickActionsDialog.QuickActionsListener {
    Activity activity;
    @BindView(R.id.recyclerView) FastScrollRecyclerView recyclerView;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout swipeRefresh;
    public boolean isAnime;                     // True if it is an anime
    public boolean clear;                       // True if it the list will be cleared
    @Setter public int sortType = 1;
    private int coverHeight = 0;                // coverheight calculated
    private int page = 1;                       // pagenumber
    @Setter public boolean isInversed = false;         // True if it is an inversed list
    public CoverAdapter recyclerAdapter;
    CoverListener listener;
    ArrayList<IGFModel.IGFItem> rawModel = new ArrayList<>();

    // infinite scroll handler
    LinearLayoutManager recyclerManager;
    boolean pagesAvailable = true;
    public boolean isLoading = true;
    int totalItemCount;
    int firstVisibleItem;
    int lastVisibleItem;
    int visibleItemCount;
    int firstLoadingItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        activity = getActivity();
        listener = (CoverFragment.CoverListener) activity;
        View view = inflater.inflate(R.layout.igf_layout, container, false);
        ButterKnife.bind(this, view);
        setLoading(true);

        int recyclerViewColumns = getColumns();
        swipeRefresh.setOnRefreshListener(this);
        recyclerAdapter = new CoverAdapter(this, isAnime, coverHeight, listener, CoverAction.getpersonalIcons());
        recyclerManager = new GridLayoutManager(activity, recyclerViewColumns);
        recyclerView.setLayoutManager(recyclerManager);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.addItemDecoration(new SpacesItemDecoration(recyclerViewColumns));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = recyclerManager.getItemCount();
                lastVisibleItem = recyclerManager.findLastVisibleItemPosition();
                firstVisibleItem = recyclerManager.findFirstVisibleItemPosition();
                visibleItemCount = lastVisibleItem - firstVisibleItem;
                if (!isLoading && pagesAvailable && totalItemCount <= (lastVisibleItem + visibleItemCount * 2)) {
                    AppLog.log(Log.INFO, "Atarashii", "IGFTest.onCoverRequest(anime:" + isAnime + "): invoked");
                    isLoading = true;
                    firstLoadingItem = firstVisibleItem;
                    listener.onCoverRequest(isAnime);
                }
            }
        });

        if (state != null) {
            isAnime = state.getBoolean("isAnime");
            clear = state.getBoolean("clear");
            recyclerAdapter.setRecordList(getSavedList("recordList" + isAnime + activity.getClass().getSimpleName()));
            IGFModel.coverText = state.getStringArray("strings");
        } else {
            IGFModel.coverText = getResources().getStringArray(R.array.igf_strings);
        }
        if (PrefManager.getTraditionalListEnabled()) {
            Theme.setBackgroundColor(activity, view, Theme.darkTheme ? R.color.bg_dark_card : R.color.bg_light_card);
        } else {
            Theme.setBackgroundColor(activity, view, R.color.bg_dark_card);
        }

        // Notify activity that the fragment has been created
        AppLog.log(Log.INFO, "Atarashii", "IGFTest.onCoverLoaded(anime:" + isAnime + "): invoked");
        listener.onCoverLoaded(this);
        return view;
    }

    public CoverFragment setType(boolean isAnime) {
        this.isAnime = isAnime;
        return this;
    }

    /**
     * NOTE: this part of the code should only be used for records that won't be saved in the DB!
     * <p>
     * Instead of reloading we just sort them.
     * do not change only this part but also the DatabaseManager part!
     *
     * @param sortType The sort type
     * @param inverse  if the list should be inverted
     */
    public void sortUnsavedList(final int sortType, boolean inverse) {
        ArrayList<IGFModel.IGFItem> list = recyclerAdapter.getRecordList();
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<IGFModel.IGFItem>() {
                @Override
                public int compare(IGFModel.IGFItem X1, IGFModel.IGFItem X2) {
                    switch (sortType) {
                        case 2:
                            return X2.getScore().compareTo(X1.getScore());
                        case 3:
                            return X1.getTypeRaw().compareTo(X2.getTypeRaw());
                        case 4:
                            return X1.getStatus().toLowerCase().compareTo(X2.getStatus().toLowerCase());
                        case 5:
                        case -5:
                            return String.valueOf(X1.getProgressRaw()).compareTo(String.valueOf(X2.getProgressRaw()));
                        default:
                            return X1.getTitle().toLowerCase().compareTo(X2.getTitle().toLowerCase());
                    }
                }
            });
            if (isInversed != inverse) {
                Collections.reverse(list);
                isInversed = inverse;
            }
        }
        recyclerAdapter.setRecordList(list);
        recyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Filter the list by status type.
     */
    public void filter(int statusType) {
        switch (statusType) {
            case 2:
                filterStatus(isAnime ? "watching" : "reading");
                break;
            case 3:
                filterStatus("completed");
                break;
            case 4:
                filterStatus("on-hold");
                break;
            case 5:
                filterStatus("dropped");
                break;
            case 6:
                filterStatus(isAnime ? "plan to watch" : "plan to read");
                break;
            default:
                recyclerAdapter.setRecordList(rawModel);
                sortUnsavedList(sortType, isInversed);
                break;
        }
        recyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Filter the status by the provided String.
     *
     * @param status The status of the record
     */
    private void filterStatus(String status) {
        ArrayList<IGFModel.IGFItem> list = new ArrayList<>();
        if (rawModel != null && rawModel.size() > 0) {
            for (IGFModel.IGFItem record : rawModel) {
                if (record.getUserStatusRaw().equals(status))
                    list.add(record);
            }
        }
        recyclerAdapter.setRecordList(list);
        sortUnsavedList(sortType, isInversed);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putBoolean("isAnime", isAnime);
        state.putBoolean("clear", clear);
        state.putStringArray("strings", IGFModel.coverText);
        saveList("recordList" + isAnime + activity.getClass().getSimpleName(), recyclerAdapter.getRecordList());
        super.onSaveInstanceState(state);
    }

    public void setLoading(boolean enabled) {
        swipeRefresh.setRefreshing(enabled);
        swipeRefresh.setEnabled(!enabled);
    }

    /**
     * Get the anime/manga search result lists.
     *
     * @param clear If true then the whole list will be cleared and loaded
     */
    public void getSearchRecords(boolean clear, String query) {
        this.clear = clear;
        if (clear)
            page = 1;

        Bundle data = new Bundle();
        data.putInt("page", page);
        ArrayList<String> args = new ArrayList<>();
        args.add(query);

        NetworkTask networkTask = new NetworkTask(TaskJob.SEARCH, isAnime, activity, data, this);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));
    }


    public void inverse(boolean isInversed) {
        this.isInversed = isInversed;
    }

    /**
     * Get the anime/manga lists.
     *
     * @param task Which list should be shown (top, popular, upcoming...)
     */
    public void getCharts(boolean clear, TaskJob task) {
        Log.e("aa", "d" + clear + task);
        if (task == null)
            return;
        this.clear = clear;
        if (clear)
            page = 1;
        Bundle data = new Bundle();
        data.putInt("page", page);

        NetworkTask networkTask = new NetworkTask(task, isAnime, activity, data, this);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Get the anime/manga lists.
     */
    public void getBrowse(HashMap<String, String> query, boolean clear) {
        this.clear = clear;
        if (clear)
            page = 1;
        Bundle data = new Bundle();
        data.putInt("page", page);
        NetworkTask networkTask = new NetworkTask(activity, isAnime, query, this);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Get the anime/manga lists.
     *
     * @param task Which list should be shown (top, popular, upcoming...)
     * @param list Which list type should be shown (completed, dropped, in progress...)
     */
    public void getPersonalList(TaskJob task, int list) {
        if (task == null)
            return;
        clear = true;
        pagesAvailable = false;

        ArrayList<String> args = new ArrayList<>();
        if (task == TaskJob.GETLIST || task == TaskJob.FORCESYNC || task == TaskJob.GETFRIENDLIST) {
            args.add(ContentManager.listSortFromInt(list, isAnime));
            args.add(String.valueOf(sortType));
            args.add(String.valueOf(isInversed));
        }

        NetworkTask networkTask = new NetworkTask(task, isAnime, activity, new Bundle(), this);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));
    }

    /**
     * Get the anime/manga lists.
     *
     * @param username Which profile records should be requested
     */
    public void getProfileList(String username) {
        clear = true;
        NetworkTask networkTask = new NetworkTask(TaskJob.GETFRIENDLIST, isAnime, activity, new Bundle(), this);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username);
    }

    /**
     * Set the numbers columns for the best overview.
     */
    private int getColumns() {
        int screenWidth = Theme.convert(activity.getResources().getConfiguration().screenWidthDp);
        if (PrefManager.getTraditionalListEnabled()) {
            return 1;
        } else if (PrefManager.getIGFColumns() == 0) {
            int columns = (int) Math.ceil(screenWidth / Theme.floatConvert(225));
            int coverwidth = screenWidth / columns;
            coverHeight = (int) (coverwidth / 0.7);
            PrefManager.setIGFColumns(columns);
            PrefManager.commitChanges();
            return columns;
        } else {
            coverHeight = (int) (screenWidth / PrefManager.getIGFColumns() / 0.7);
            return PrefManager.getIGFColumns();
        }
    }

    /**
     * Get the max amount of columns before the design breaks.
     *
     * @param portrait The orientation of the screen.
     * @return int The amount of max columns
     */
    public static int getMaxColumns(boolean portrait) {
        int screen;
        if (Theme.isPortrait() && portrait || !Theme.isPortrait() && !portrait)
            screen = Theme.convert(Theme.context.getResources().getConfiguration().screenWidthDp);
        else
            screen = Theme.convert(Theme.context.getResources().getConfiguration().screenHeightDp);
        return (int) Math.ceil(screen / Theme.convert(225)) + 2;
    }

    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, boolean isAnime) {
        setLoading(false);
        if (clear) {
            recyclerAdapter.getRecordList().clear();
            page = 1;
        }

        try {
            IGFModel igfModel = (IGFModel) result;
            rawModel = igfModel.getTitles();
            if (page == 1) {
                recyclerAdapter.setRecordList(igfModel.getTitles());
            } else {
                recyclerAdapter.addRecords(igfModel.getTitles());
            }
            recyclerAdapter.notifyDataSetChanged();
            recyclerAdapter.setFastScrollText(igfModel.getFastScrollText());
            pagesAvailable = igfModel.getTitles() != null && igfModel.getTitles().size() != 0 && job != TaskJob.GETLIST && job != TaskJob.FORCESYNC && job != TaskJob.GETFRIENDLIST;
            if (job == TaskJob.GETFRIENDLIST)
                sortUnsavedList(sortType, isInversed);
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "IGFTest.onNetworkTaskFinished(anime:" + isAnime + "): " + result.getClass().toString());
            AppLog.logException(e);
            e.printStackTrace();
        }

        //TODO add message of failure
        isLoading = false;
        page++;
    }

    @Override
    public void onNetworkTaskError(TaskJob job) {

    }

    @Override
    public void onRefresh() {
        AppLog.log(Log.INFO, "Atarashii", "IGFTest.onCoverRequest(anime:" + isAnime + "): invoked");
        listener.onCoverRequest(isAnime);
    }

    /**
     * Save the lists temp. in the storage.
     * <p/>
     * Since android 7 they no not suppress onSaveInstanceState which caused TransactionTooLargeExceptions errors.
     * Currently we are saving it as a cache file in the storage.
     * We chose not for the DB because it can cause errors when the user will press back too fast.
     *
     * @param key     The cache key name
     * @param records The saved records
     */
    private void saveList(String key, ArrayList<IGFModel.IGFItem> records) {
        AppLog.log(Log.INFO, "Atarashii", "IGF.saveList(" + key + ")");
        try {
            Gson gson = new Gson();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(activity.openFileOutput(key + ".cache", Context.MODE_PRIVATE));
            JsonWriter writer = new JsonWriter(outputStreamWriter);
            writer.setIndent("  ");
            writer.beginArray();
            for (IGFModel.IGFItem record : records) {
                gson.toJson(record, IGFModel.IGFItem.class, writer);
            }
            writer.endArray();
            writer.close();
            AppLog.log(Log.INFO, "Atarashii", "IGF.saveList(" + key + "): has been saved");
        } catch (Exception e) {
            AppLog.logException(e);
        }
    }

    /**
     * Get the cache records from the storage.
     *
     * @param key The cache key
     * @return The record arraylist
     */
    private ArrayList<IGFModel.IGFItem> getSavedList(String key) {
        AppLog.log(Log.INFO, "Atarashii", "IGF.getSavedList(" + key + ")");
        ArrayList<IGFModel.IGFItem> records = new ArrayList<>();
        Gson gson = new Gson();
        try {
            InputStreamReader inputStream = new InputStreamReader(activity.openFileInput(key + ".cache"));
            JsonReader reader = new JsonReader(inputStream);
            reader.beginArray();
            while (reader.hasNext()) {
                IGFModel.IGFItem record = gson.fromJson(reader, IGFModel.IGFItem.class);
                records.add(record);
            }
            reader.endArray();
            reader.close();
            AppLog.log(Log.INFO, "Atarashii", "IGF.getSavedList(" + key + "): has been loaded");
        } catch (Exception e) {
            AppLog.logException(e);
        }
        return records;
    }

    @Override
    public void onDismiss() {

    }

    public interface CoverListener {
        void onCoverLoaded(CoverFragment coverFragment);

        void onCoverRequest(boolean isAnime);

        void onCoverClicked(int position, int actionId, boolean isAnime, IGFModel.IGFItem item);
    }
}
