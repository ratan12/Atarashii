package net.somethingdreadful.MAL;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.cover.CoverAction;
import net.somethingdreadful.MAL.cover.CoverFragment;
import net.somethingdreadful.MAL.database.DatabaseManager;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity implements CoverFragment.CoverListener, SearchView.OnQueryTextListener {
    public String query;
    private CoverFragment af;
    private CoverFragment mf;
    @BindView(R.id.searchView) SearchView searchView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.activity_search, false);
        Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));
        query = getIntent().getStringExtra("query");
        ButterKnife.bind(this);
        searchView.setVersion(SearchView.VERSION_TOOLBAR);
        searchView.setOnQueryTextListener(this);
        searchView.setQuery(query, false);
        searchView.setArrowOnly(true);

        final Activity activity = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final DatabaseManager dbManager = new DatabaseManager(activity);
                final List<SearchItem> suggestion = dbManager.getSuggestions();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initSuggestion(suggestion);
                    }
                });
            }
        });
    }

    @OnClick(R.id.action_home)
    public void close(View view) {
        finish();
    }

    @Override
    public void onCoverLoaded(final CoverFragment coverFragment) {
        if (coverFragment.isAnime)
            af = coverFragment;
        else
            mf = coverFragment;
        if (query != null && !TextUtils.isDigitsOnly(query)) // there is already a search to do
            coverFragment.getSearchRecords(true, WordUtils.capitalize(query));
    }

    public void initSuggestion(List<SearchItem> suggestion) {
        SearchAdapter searchAdapter = new SearchAdapter(this, suggestion);
        searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                query = textView.getText().toString();
                if (af != null && mf != null) {
                    af.getSearchRecords(true, WordUtils.capitalize(query));
                    mf.getSearchRecords(true, WordUtils.capitalize(query));
                    searchView.setQuery(query, false);
                    searchView.close(true);
                }
            }
        });
        searchView.setAdapter(searchAdapter);
    }

    @Override
    public void onCoverRequest(boolean isAnime) {
        if (af != null && mf != null) {
            af.getSearchRecords(false, WordUtils.capitalize(query));
            mf.getSearchRecords(false, WordUtils.capitalize(query));
        }
    }

    @Override
    public void onCoverClicked(int position, int actionId, boolean isAnime, IGFModel.IGFItem item) {
        if (actionId == 1)
            new CoverAction(this, isAnime).addCoverItem(item);
        else if (actionId == 2)
            new CoverAction(this, isAnime).addCoverItem(item);
        else if (actionId == 3)
            new CoverAction(this, isAnime).addCoverItem(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (af != null && mf != null && !this.query.equals(query)) {
            af.getSearchRecords(true, WordUtils.capitalize(query));
            mf.getSearchRecords(true, WordUtils.capitalize(query));
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}