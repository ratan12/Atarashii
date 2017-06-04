package net.somethingdreadful.MAL

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import butterknife.ButterKnife
import butterknife.bindView
import com.facebook.drawee.view.SimpleDraweeView
import com.lapism.searchview.SearchView
import net.somethingdreadful.MAL.Theme.context
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver
import net.somethingdreadful.MAL.cover.CoverAction
import net.somethingdreadful.MAL.cover.CoverFragment
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment
import net.somethingdreadful.MAL.dialog.InputDialogFragment
import net.somethingdreadful.MAL.tasks.TaskJob
import java.util.*

class Home : AppCompatActivity(), ChooseDialogFragment.onClickListener, CoverFragment.CoverListener, View.OnClickListener, ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener, InputDialogFragment.onClickListener, RecordStatusUpdatedReceiver.RecordStatusUpdatedListener, SearchView.OnQueryTextListener {
    private var af: CoverFragment? = null
    private var mf: CoverFragment? = null
    private var menu: Menu? = null
    private var networkReceiver: BroadcastReceiver? = null
    private var username: String? = null
    private var networkAvailable = true
    private var personalList = 0

    val navigationView: NavigationView by bindView(R.id.navigationView)
    val drawerLayout: DrawerLayout by bindView(R.id.drawerLayout)
    val tabs: TabLayout by bindView(R.id.tabs)
    val appBarLayout: AppBarLayout by bindView(R.id.tabsContainer)
    val viewPager: ViewPager by bindView(R.id.pager)
    val searchView: SearchView by bindView(R.id.searchView)

    var arrayMenuSort = Arrays.asList(R.id.sort_title, R.id.sort_score, R.id.sort_type, R.id.sort_status, R.id.sort_progress)
    var arrayMenuboolean = Arrays.asList(R.id.listType_all, R.id.listType_inprogress, R.id.listType_completed, R.id.listType_onhold,
            R.id.listType_dropped, R.id.listType_planned, R.id.listType_rewatching)
    var ArrayMenuCustom = Arrays.asList(R.id.customList1, R.id.customList2, R.id.customList3, R.id.customList4, R.id.customList5,
            R.id.customList6, R.id.customList7, R.id.customList8, R.id.customList9, R.id.customList10, R.id.customList11, R.id.customList12,
            R.id.customList13, R.id.customList14, R.id.customList15)

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        //Initializing activity and application
        context = applicationContext

        if (AccountService.AccountExists(this)) {
            //The following is state handling code
            if (state != null) {
                networkAvailable = state.getBoolean("networkAvailable", true)
            }

            //Initializing
            Theme.setTheme(this, R.layout.activity_home, false)
            Theme.setActionBar(this, IGFPagerAdapter(fragmentManager))

            ButterKnife.bind(this)
            username = AccountService.username
            if (PrefManager.getHideHomeTabs())
                tabs.visibility = View.GONE
            viewPager.addOnPageChangeListener(this)
            searchView.version = SearchView.VERSION_MENU_ITEM
            searchView.setOnQueryTextListener(this)
            searchView.setArrowOnly(true)

            //Initializing NavigationView
            navigationView.setNavigationItemSelectedListener(this)
            navigationView.menu.findItem(R.id.nav_list).isChecked = true
            Theme.setNavDrawer(navigationView, this, this)

            //Initializing navigation toggle button
            val drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, findViewById(R.id.actionbar) as Toolbar, R.string.drawer_open, R.string.drawer_close) {

            }
            drawerLayout.addDrawerListener(drawerToggle)
            drawerToggle.syncState()

            networkReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    checkNetworkAndDisplayCrouton()
                    myListChanged()
                }
            }

            val recordStatusReceiver = RecordStatusUpdatedReceiver(this)
            val filter = IntentFilter(RecordStatusUpdatedReceiver.RECV_IDENT)
            LocalBroadcastManager.getInstance(this).registerReceiver(recordStatusReceiver, filter)
        } else {
            val firstRunInit = Intent(this, FirstTimeInit::class.java)
            startActivity(firstRunInit)
            finish()
        }
        NfcHelper.disableBeam(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(if (AccountService.isMAL) R.menu.activity_home_mal else R.menu.activity_home_al, menu)
        this.menu = menu

        if (!AccountService.isMAL)
            setCustomList(PrefManager.getCustomAnimeList())
        return true
    }

    /**
     * Properly set the Custom lists.
     * Note: This is AL only!

     * @param customList The Anime or Manga customList names
     */
    private fun setCustomList(customList: Array<String>) {
        if (menu != null) {
            for (i in ArrayMenuCustom.indices) {
                setCustomItem(menu!!.findItem(ArrayMenuCustom[i]), customList, i)
            }
        }
    }

    /**
     * This is used to check if the MenuItem has a name in the Prefs and set it.

     * @param item  The menu item
     * @param list  The name list. Anime and Manga have different ones
     * @param index The name index number
     */
    private fun setCustomItem(item: MenuItem, list: Array<String>, index: Int) {
        if (list.size > index) {
            item.title = list[index]
            item.isVisible = true
        } else {
            item.isVisible = false
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (af != null)
            setChecked(menu.findItem(arrayMenuboolean[personalList]))
        menu.findItem(R.id.sort_title).isChecked = true
        myListChanged()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (af != null && mf != null) {
            if (arrayMenuboolean.contains(item.itemId)) {
                getPersonalList(TaskJob.GETLIST, arrayMenuboolean.indexOf(item.itemId), item)
            } else if (arrayMenuSort.contains(item.itemId)) {
                sortRecords(arrayMenuSort.indexOf(item.itemId) + 1, item)
            } else if (ArrayMenuCustom.contains(item.itemId)) {
                getPersonalList(TaskJob.GETLIST, ArrayMenuCustom.indexOf(item.itemId) + arrayMenuboolean.size, item)
            } else if (item.itemId == R.id.forceSync) {
                getPersonalList(TaskJob.FORCESYNC, personalList, null)
            } else if (item.itemId == R.id.action_search) {
                searchView.open(true, item)
                appBarLayout.setExpanded(true, true)
                return true
            } else if (item.itemId == R.id.menu_inverse) {
                item.isChecked = !item.isChecked
                af!!.inverse(item.isChecked)
                mf!!.inverse(item.isChecked)
                getPersonalList(TaskJob.GETLIST, personalList, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setChecked(item: MenuItem?) {
        if (item != null) {
            item.isChecked = true
        }
    }

    private fun sortRecords(sortType: Int, item: MenuItem) {
        if (af != null && mf != null) {
            af!!.sortType = sortType
            mf!!.sortType = sortType
            getPersonalList(TaskJob.GETLIST, personalList, item)
        }
    }

    private fun getPersonalList(task: TaskJob, list: Int, item: MenuItem?) {
        personalList = list
        if (item != null)
            setChecked(item)
        if (af != null && mf != null) {
            af!!.getPersonalList(task, list)
            mf!!.getPersonalList(task, list)
        }
    }

    public override fun onResume() {
        super.onResume()
        checkNetworkAndDisplayCrouton()
        registerReceiver(networkReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }

    public override fun onPause() {
        super.onPause()
        if (menu != null)
            menu!!.findItem(R.id.action_search).collapseActionView()
        unregisterReceiver(networkReceiver)
    }

    public override fun onSaveInstanceState(state: Bundle) {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("networkAvailable", networkAvailable)
        super.onSaveInstanceState(state)
    }

    private fun myListChanged() {
        if (menu != null)
            menu!!.findItem(R.id.action_search).isVisible = networkAvailable
    }

    private fun showLogoutDialog() {
        val lcdf = ChooseDialogFragment()
        val bundle = Bundle()
        bundle.putString("title", getString(R.string.dialog_label_logout))
        bundle.putString("message", getString(R.string.dialog_message_logout))
        bundle.putString("positive", getString(R.string.dialog_label_logout))
        lcdf.arguments = bundle
        lcdf.setCallback(this)
        lcdf.show(fragmentManager, "fragment_LogoutConfirmationDialog")
    }

    private fun checkNetworkAndDisplayCrouton() {
        if (APIHelper.isNetworkAvailable(this) && !networkAvailable)
            getPersonalList(TaskJob.FORCESYNC, personalList, null)
        networkAvailable = APIHelper.isNetworkAvailable(this)
    }

    override fun onCoverLoaded(igf: CoverFragment) {
        // TODO add notification and fail counter with customlist
        if (igf.isAnime)
            af = igf
        else
            mf = igf
        // do forced sync after FirstInit
        if (PrefManager.getForceSync()) {
            if (af != null && mf != null) {
                PrefManager.setForceSync(false)
                PrefManager.commitChanges()
                getPersonalList(TaskJob.FORCESYNC, personalList, null)
            }
        } else {
            personalList = PrefManager.getDefaultList()
            igf.getPersonalList(TaskJob.GETLIST, personalList)
        }
    }

    override fun onCoverRequest(isAnime: Boolean) {
        if (networkAvailable)
            getPersonalList(TaskJob.FORCESYNC, personalList, null)
        else {
            if (af != null && mf != null) {
                af!!.setLoading(false)
                mf!!.setLoading(false)
            }
            Theme.Snackbar(this, R.string.toast_error_noConnectivity)
        }
    }

    override fun onCoverClicked(position: Int, actionId: Int, isAnime: Boolean, item: IGFModel.IGFItem) {
        when (actionId) {
            1 -> CoverAction(this, isAnime).comProgress(item)
            2 -> CoverAction(this, isAnime).subProgress(item)
            3 -> CoverAction(this, isAnime).addProgress(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.Image -> {
                val Profile = Intent(this, ProfileActivity::class.java)
                Profile.putExtra("username", username)
                startActivity(Profile)
            }
            R.id.NDimage -> {
                val lcdf = InputDialogFragment()
                val bundle = Bundle()
                bundle.putInt("id", R.id.NDimage)
                bundle.putString("title", getString(R.string.dialog_title_update_navigation))
                bundle.putString("hint", getString(R.string.dialog_message_update_navigation))
                bundle.putString("message", PrefManager.getNavigationBackground())
                lcdf.arguments = bundle
                lcdf.setCallback(this)
                lcdf.show(fragmentManager, "fragment_InputDialogFragment")
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (AccountService.isMAL) {
            if (menu != null)
                menu!!.findItem(R.id.listType_rewatching).title = getString(if (position == 0) R.string.listType_rewatching else R.string.listType_rereading)
        } else {
            setCustomList(if (position == 0) PrefManager.getCustomAnimeList() else PrefManager.getCustomMangaList())
        }
    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPositiveButtonClicked() {
        AccountService.clearData()
        startActivity(Intent(this, FirstTimeInit::class.java))
        System.exit(0)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_list)
            item.isChecked = !item.isChecked
        drawerLayout.closeDrawers()

        //Performing the action
        when (item.itemId) {
            R.id.nav_list -> getPersonalList(TaskJob.GETLIST, personalList, null)
            R.id.nav_profile -> {
                val Profile = Intent(this, ProfileActivity::class.java)
                Profile.putExtra("username", username)
                startActivity(Profile)
            }
            R.id.nav_friends -> {
                val Friends = Intent(this, ProfileActivity::class.java)
                Friends.putExtra("username", username)
                Friends.putExtra("friends", username)
                startActivity(Friends)
            }
            R.id.nav_forum -> if (networkAvailable)
                startActivity(Intent(this, ForumActivity::class.java))
            else
                Theme.Snackbar(this, R.string.toast_error_noConnectivity)
            R.id.nav_schedule -> startActivity(Intent(this, ScheduleActivity::class.java))
            R.id.nav_charts -> startActivity(Intent(this, ChartActivity::class.java))
            R.id.nav_browse -> startActivity(Intent(this, BrowseActivity::class.java))
            R.id.nav_logout -> showLogoutDialog()
            R.id.nav_settings -> startActivity(Intent(this, Settings::class.java))
            R.id.nav_support -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://atarashii.freshdesk.com/support/tickets/new")))
            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        myListChanged()
        return false
    }

    override fun onPosInputButtonClicked(text: String, id: Int) {
        (findViewById(R.id.NDimage) as SimpleDraweeView).setImageURI(text)
        PrefManager.setNavigationBackground(text)
        PrefManager.commitChanges()
    }

    override fun onNegInputButtonClicked(id: Int) {
        (findViewById(R.id.NDimage) as SimpleDraweeView).setActualImageResource(R.drawable.atarashii_background)
        PrefManager.setNavigationBackground(null)
        PrefManager.commitChanges()
    }

    override fun onRecordStatusUpdated(isAnime: Boolean) {
        getPersonalList(TaskJob.GETLIST, personalList, null)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        searchView.close(true)
        val Search = Intent(this, SearchActivity::class.java)
        Search.putExtra("query", query)
        startActivity(Search)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }
}
