package net.somethingdreadful.MAL;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.cover.CoverAction;
import net.somethingdreadful.MAL.cover.CoverFragment;
import net.somethingdreadful.MAL.dialog.SearchIdDialogFragment;

import org.apache.commons.lang3.text.WordUtils;

public class SearchActivity extends AppCompatActivity implements CoverFragment.CoverListener {
    public String query;
    private CoverFragment af;
    private CoverFragment mf;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.theme_viewpager, false);
        Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            if (TextUtils.isDigitsOnly(query)) {
                FragmentManager fm = getFragmentManager();
                (new SearchIdDialogFragment()).show(fm, "fragment_id_search");
            } else if (af != null && mf != null) {
                af.getSearchRecords(true, query);
                mf.getSearchRecords(true, query);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search_view, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(query, false);
        return true;
    }

    @Override
    protected void onResume() {
        if (getIntent() != null)
            handleIntent(getIntent());
        super.onResume();
    }

    @Override
    public void onCoverLoaded(CoverFragment coverFragment) {
        if (coverFragment.isAnime)
            af = coverFragment;
        else
            mf = coverFragment;
        if (query != null && !TextUtils.isDigitsOnly(query)) // there is already a search to do
            coverFragment.getSearchRecords(true, WordUtils.capitalize(query));
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
}