package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.cover.CoverFragment;
import net.somethingdreadful.MAL.tasks.TaskJob;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity implements CoverFragment.CoverListener, SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener {
    private CoverFragment af;
    private CoverFragment mf;
    private MenuItem drawerItem;

    @BindView(R.id.navigationView)
    NavigationView navigationView;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    private TaskJob taskjob;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.activity_charts, false);
        Theme.setActionBar(this, new IGFPagerAdapter(getFragmentManager()));
        ButterKnife.bind(this);

        //Initializing NavigationView
        navigationView.setNavigationItemSelectedListener(this);
        Theme.setNavDrawer(navigationView, this, null);

        //Initializing navigation toggle button
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, (Toolbar) findViewById(R.id.actionbar), R.string.drawer_open, R.string.drawer_close) {
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerLayout.openDrawer(ViewCompat.getLayoutDirection(drawerLayout) == ViewCompat.LAYOUT_DIRECTION_RTL ? Gravity.RIGHT : Gravity.LEFT);

        NfcHelper.disableBeam(this);
    }

    private void getRecords(boolean clear, TaskJob task) {
        taskjob = task != null ? task : taskjob;
        if (af != null && mf != null) {
            af.getCharts(clear, taskjob);
            mf.getCharts(clear, taskjob);
        }
    }

    @Override
    public void onRefresh() {
        if (APIHelper.isNetworkAvailable(this))
            getRecords(false, TaskJob.FORCESYNC);
        else {
            Theme.Snackbar(this, R.string.toast_error_noConnectivity);
        }
    }

    @Override
    public void onCoverLoaded(CoverFragment igf) {
        // TODO add notification and fail counter with customlist
        if (igf.getIsAnime())
            af = igf;
        else
            mf = igf;
    }

    @Override
    public void onCoverRequest(boolean isAnime) {
        getRecords(false, null);
    }

    @Override
    public void onCoverClicked(int position, int actionId, boolean isAnime, IGFModel.IGFItem item) {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (drawerItem != null)
            drawerItem.setChecked(false);
        item.setChecked(true);
        drawerItem = item;
        setTitle(item.getTitle());

        // disable swipeRefresh for other lists
        af.setLoading(true);
        mf.setLoading(true);

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        //Performing the action
        switch (item.getItemId()) {
            case R.id.nav_rated:
                getRecords(true, TaskJob.GETTOPRATED);
                break;
            case R.id.nav_rated_season:
                getRecords(true, TaskJob.GETTOPRATEDS);
                break;
            case R.id.nav_rated_year:
                getRecords(true, TaskJob.GETTOPRATEDY);
                break;
            case R.id.nav_popular:
                getRecords(true, TaskJob.GETMOSTPOPULAR);
                break;
            case R.id.nav_popular_season:
                getRecords(true, TaskJob.GETMOSTPOPULARS);
                break;
            case R.id.nav_popular_year:
                getRecords(true, TaskJob.GETMOSTPOPULARY);
                break;
            case R.id.nav_added:
                getRecords(true, TaskJob.GETJUSTADDED);
                break;
            case R.id.nav_upcoming:
                getRecords(true, TaskJob.GETUPCOMING);
                break;
            case R.id.nav_return:
                finish();
        }
        return false;
    }
}
