package net.somethingdreadful.MAL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lapism.searchview.SearchView;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver;
import net.somethingdreadful.MAL.cover.CoverAction;
import net.somethingdreadful.MAL.cover.CoverFragment;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;
import net.somethingdreadful.MAL.dialog.InputDialogFragment;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static net.somethingdreadful.MAL.Theme.context;

public class Home extends AppCompatActivity implements ChooseDialogFragment.onClickListener, CoverFragment.CoverListener, View.OnClickListener, ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener, InputDialogFragment.onClickListener, RecordStatusUpdatedReceiver.RecordStatusUpdatedListener, SearchView.OnQueryTextListener {
    private CoverFragment af;
    private CoverFragment mf;
    private Menu menu;
    private BroadcastReceiver networkReceiver;
    private String username;
    private boolean networkAvailable = true;
    private int personalList = 0;
    @BindView(R.id.navigationView) NavigationView navigationView;
    @BindView(R.id.drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.tabsContainer) AppBarLayout appBarLayout;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.searchView) SearchView searchView;

    List<Integer> arrayMenuSort = Arrays.asList(R.id.sort_title, R.id.sort_score, R.id.sort_type, R.id.sort_status, R.id.sort_progress);
    List<Integer> arrayMenuListType = Arrays.asList(R.id.listType_all, R.id.listType_inprogress, R.id.listType_completed, R.id.listType_onhold,
            R.id.listType_dropped, R.id.listType_planned, R.id.listType_rewatching);
    List<Integer> ArrayMenuCustom = Arrays.asList(R.id.customList1, R.id.customList2, R.id.customList3, R.id.customList4, R.id.customList5,
            R.id.customList6, R.id.customList7, R.id.customList8, R.id.customList9, R.id.customList10, R.id.customList11, R.id.customList12,
            R.id.customList13, R.id.customList14, R.id.customList15);

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        //Initializing activity and application
        context = getApplicationContext();

        if (AccountService.AccountExists(this)) {
            //The following is state handling code
            if (state != null) {
                networkAvailable = state.getBoolean("networkAvailable", true);
            }

            //Initializing
            Theme.setTheme(this, R.layout.activity_home, false);
            Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));

            ButterKnife.bind(this);
            username = AccountService.getUsername();
            if (PrefManager.getHideHomeTabs())
                tabs.setVisibility(View.GONE);
            viewPager.addOnPageChangeListener(this);
            searchView.setVersion(SearchView.VERSION_MENU_ITEM);
            searchView.setOnQueryTextListener(this);
            searchView.setArrowOnly(true);

            //Initializing NavigationView
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().findItem(R.id.nav_list).setChecked(true);
            Theme.setNavDrawer(navigationView, this, this);

            //Initializing navigation toggle button
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.actionbar), R.string.drawer_open, R.string.drawer_close) {
            };
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();

            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkNetworkAndDisplayCrouton();
                    myListChanged();
                }
            };

            RecordStatusUpdatedReceiver recordStatusReceiver = new RecordStatusUpdatedReceiver(this);
            IntentFilter filter = new IntentFilter(RecordStatusUpdatedReceiver.RECV_IDENT);
            LocalBroadcastManager.getInstance(this).registerReceiver(recordStatusReceiver, filter);
        } else {
            Intent firstRunInit = new Intent(this, FirstTimeInit.class);
            startActivity(firstRunInit);
            finish();
        }
        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(AccountService.isMAL() ? R.menu.activity_home_mal : R.menu.activity_home_al, menu);
        this.menu = menu;

        if (!AccountService.isMAL())
            setCustomList(PrefManager.getCustomAnimeList());
        return true;
    }

    /**
     * Properly set the Custom lists.
     * Note: This is AL only!
     *
     * @param customList The Anime or Manga customList names
     */
    private void setCustomList(String[] customList) {
        if (menu != null) {
            for (int i = 0; i < ArrayMenuCustom.size(); i++) {
                setCustomItem(menu.findItem(ArrayMenuCustom.get(i)), customList, i);
            }
        }
    }

    /**
     * This is used to check if the MenuItem has a name in the Prefs and set it.
     *
     * @param item  The menu item
     * @param list  The name list. Anime and Manga have different ones
     * @param index The name index number
     */
    private void setCustomItem(MenuItem item, String[] list, int index) {
        if (list.length > index) {
            item.setTitle(list[index]);
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (af != null)
            setChecked(menu.findItem(arrayMenuListType.get(personalList)));
        menu.findItem(R.id.sort_title).setChecked(true);
        myListChanged();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (af != null && mf != null) {
            if (arrayMenuListType.contains(item.getItemId())) {
                getPersonalList(TaskJob.GETLIST, arrayMenuListType.indexOf(item.getItemId()), item);
            } else if (arrayMenuSort.contains(item.getItemId())) {
                sortRecords(arrayMenuSort.indexOf(item.getItemId()) + 1, item);
            } else if (ArrayMenuCustom.contains(item.getItemId())) {
                getPersonalList(TaskJob.GETLIST, ArrayMenuCustom.indexOf(item.getItemId()) + arrayMenuListType.size(), item);
            } else if (item.getItemId() == R.id.forceSync) {
                getPersonalList(TaskJob.FORCESYNC, personalList, null);
            } else if (item.getItemId() == R.id.action_search) {
                searchView.open(true, item);
                appBarLayout.setExpanded(true, true);
                return true;
            } else if (item.getItemId() == R.id.menu_inverse) {
                item.setChecked(!item.isChecked());
                af.inverse(item.isChecked());
                mf.inverse(item.isChecked());
                getPersonalList(TaskJob.GETLIST, personalList, null);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setChecked(MenuItem item) {
        if (item != null)
            item.setChecked(true);
    }

    private void sortRecords(int sortType, MenuItem item) {
        if (af != null && mf != null) {
            af.setSortType(sortType);
            mf.setSortType(sortType);
            getPersonalList(TaskJob.GETLIST, personalList, item);
        }
    }

    private void getPersonalList(TaskJob task, int list, MenuItem item) {
        personalList = list;
        if (item != null)
            setChecked(item);
        if (af != null && mf != null) {
            af.getPersonalList(task, list);
            mf.getPersonalList(task, list);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNetworkAndDisplayCrouton();
        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (menu != null)
            menu.findItem(R.id.action_search).collapseActionView();
        unregisterReceiver(networkReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("networkAvailable", networkAvailable);
        super.onSaveInstanceState(state);
    }

    private void myListChanged() {
        if (menu != null)
            menu.findItem(R.id.action_search).setVisible(networkAvailable);
    }

    private void showLogoutDialog() {
        ChooseDialogFragment lcdf = new ChooseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.dialog_label_logout));
        bundle.putString("message", getString(R.string.dialog_message_logout));
        bundle.putString("positive", getString(R.string.dialog_label_logout));
        lcdf.setArguments(bundle);
        lcdf.setCallback(this);
        lcdf.show(getFragmentManager(), "fragment_LogoutConfirmationDialog");
    }

    private void checkNetworkAndDisplayCrouton() {
        if (APIHelper.isNetworkAvailable(this) && !networkAvailable)
            getPersonalList(TaskJob.FORCESYNC, personalList, null);
        networkAvailable = APIHelper.isNetworkAvailable(this);
    }

    @Override
    public void onCoverLoaded(CoverFragment igf) {
        // TODO add notification and fail counter with customlist
        if (igf.isAnime)
            af = igf;
        else
            mf = igf;
        // do forced sync after FirstInit
        if (PrefManager.getForceSync()) {
            if (af != null && mf != null) {
                PrefManager.setForceSync(false);
                PrefManager.commitChanges();
                getPersonalList(TaskJob.FORCESYNC, personalList, null);
            }
        } else {
            personalList = PrefManager.getDefaultList();
            igf.getPersonalList(TaskJob.GETLIST, personalList);
        }
    }

    @Override
    public void onCoverRequest(boolean isAnime) {
        if (networkAvailable)
            getPersonalList(TaskJob.FORCESYNC, personalList, null);
        else {
            if (af != null && mf != null) {
                af.setLoading(false);
                mf.setLoading(false);
            }
            Theme.Snackbar(this, R.string.toast_error_noConnectivity);
        }
    }

    @Override
    public void onCoverClicked(int position, int actionId, boolean isAnime, IGFModel.IGFItem item) {
        switch (actionId) {
            case 1:
                new CoverAction(this, isAnime).comProgress(item);
                break;
            case 2:
                new CoverAction(this, isAnime).subProgress(item);
                break;
            case 3:
                new CoverAction(this, isAnime).addProgress(item);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Image:
                Intent Profile = new Intent(this, ProfileActivity.class);
                Profile.putExtra("username", username);
                startActivity(Profile);
                break;
            case R.id.NDimage:
                InputDialogFragment lcdf = new InputDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id", R.id.NDimage);
                bundle.putString("title", getString(R.string.dialog_title_update_navigation));
                bundle.putString("hint", getString(R.string.dialog_message_update_navigation));
                bundle.putString("message", PrefManager.getNavigationBackground());
                lcdf.setArguments(bundle);
                lcdf.setCallback(this);
                lcdf.show(getFragmentManager(), "fragment_InputDialogFragment");
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (AccountService.isMAL()) {
            if (menu != null)
                menu.findItem(R.id.listType_rewatching).setTitle(getString(position == 0 ? R.string.listType_rewatching : R.string.listType_rereading));
        } else {
            setCustomList(position == 0 ? PrefManager.getCustomAnimeList() : PrefManager.getCustomMangaList());
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPositiveButtonClicked() {
        AccountService.clearData();
        startActivity(new Intent(this, FirstTimeInit.class));
        System.exit(0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_list)
            item.setChecked(!item.isChecked());
        drawerLayout.closeDrawers();

        //Performing the action
        switch (item.getItemId()) {
            case R.id.nav_list:
                getPersonalList(TaskJob.GETLIST, personalList, null);
                break;
            case R.id.nav_profile:
                Intent Profile = new Intent(this, ProfileActivity.class);
                Profile.putExtra("username", username);
                startActivity(Profile);
                break;
            case R.id.nav_friends:
                Intent Friends = new Intent(this, ProfileActivity.class);
                Friends.putExtra("username", username);
                Friends.putExtra("friends", username);
                startActivity(Friends);
                break;
            case R.id.nav_forum:
                if (networkAvailable)
                    startActivity(new Intent(this, ForumActivity.class));
                else
                    Theme.Snackbar(this, R.string.toast_error_noConnectivity);
                break;
            case R.id.nav_schedule:
                startActivity(new Intent(this, ScheduleActivity.class));
                break;
            case R.id.nav_charts:
                startActivity(new Intent(this, ChartActivity.class));
                break;
            case R.id.nav_browse:
                startActivity(new Intent(this, BrowseActivity.class));
                break;
            case R.id.nav_logout: // Others subgroup
                showLogoutDialog();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.nav_support:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://atarashii.freshdesk.com/support/tickets/new")));
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        myListChanged();
        return false;
    }

    @Override
    public void onPosInputButtonClicked(String text, int id) {
        ((SimpleDraweeView) findViewById(R.id.NDimage)).setImageURI(text);
        PrefManager.setNavigationBackground(text);
        PrefManager.commitChanges();
    }

    @Override
    public void onNegInputButtonClicked(int id) {
        ((SimpleDraweeView) findViewById(R.id.NDimage)).setActualImageResource(R.drawable.atarashii_background);
        PrefManager.setNavigationBackground(null);
        PrefManager.commitChanges();
    }

    @Override
    public void onRecordStatusUpdated(MALApi.ListType type) {
        getPersonalList(TaskJob.GETLIST, personalList, null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.close(true);
        Intent Search = new Intent(this, SearchActivity.class);
        Search.putExtra("query", query);
        startActivity(Search);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
