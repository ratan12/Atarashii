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
import android.view.View;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.dialog.SearchIdDialogFragment;
import net.somethingdreadful.MAL.tasks.TaskJob;

import org.apache.commons.lang3.text.WordUtils;

public class SearchActivity extends AppCompatActivity implements IGF.IGFCallbackListener {
    public String query;
    private IGF af;
    private IGF mf;

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
            } else {
                if (af != null && mf != null) {
                    af.searchRecords(query);
                    mf.searchRecords(query);
                }
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
    public void onIGFReady(IGF igf) {
        /* Set Username to the search IGFs, looks strange but has a reason:
         * The username is passed to DetailViews if clicked, the DetailView tries to get user-specific
         * details (read/watch status, score). To do this it needs the username to determine the correct
         * anime-/mangalist
         */
        igf.setUsername(AccountService.getUsername());
        if (igf.isAnime())
            af = igf;
        else
            mf = igf;
        if (query != null && !TextUtils.isDigitsOnly(query)) // there is already a search to do
            igf.searchRecords(WordUtils.capitalize(query));
    }

    @Override
    public void onRecordsLoadingFinished(TaskJob job) {
    }

    @Override
    public void onItemClick(int id, ListType listType, String username, View view, String coverImage) {
        DetailView.createDV(this, view, id, listType, username, coverImage);
    }
}