package net.somethingdreadful.MAL.adapters

import android.app.Fragment
import android.app.FragmentManager
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter

import net.somethingdreadful.MAL.DetailView
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.detailView.DetailViewDetails
import net.somethingdreadful.MAL.detailView.DetailViewPersonal
import net.somethingdreadful.MAL.detailView.DetailViewRecs
import net.somethingdreadful.MAL.detailView.DetailViewReviews

class DetailViewPagerAdapter(fm: FragmentManager, private val activity: DetailView) : FragmentPagerAdapter(fm) {
    private val fragments: Fragments = Fragments(activity)
    private var hidePersonal = false
    private var fragmentId: Long = 0

    init {
        reCreate()
    }

    private fun reCreate() {
        fragments.clear()
        fragments.add(DetailViewDetails(), R.string.tab_name_details)
        if (!hidePersonal)
            fragments.add(DetailViewPersonal(), R.string.tab_name_personal)
        if (APIHelper.isNetworkAvailable(activity)) {
            fragments.add(DetailViewReviews(), R.string.tab_name_reviews)
            if (AccountService.isMAL)
                fragments.add(DetailViewRecs(), R.string.tab_name_recommendations)
        }
    }

    fun hidePersonal(hidePersonal: Boolean) {
        if (hidePersonal != this.hidePersonal) {
            this.hidePersonal = hidePersonal
            reCreate()
            notifyChangeInPosition(2)
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragments.getFragment(position)
    }

    override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItemId(position: Int): Long {
        return fragmentId + position
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): String {
        return fragments.getName(position)
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment

     * @param number number of items which have been changed
     */
    private fun notifyChangeInPosition(number: Int) {
        fragmentId += (count + number).toLong()
    }
}