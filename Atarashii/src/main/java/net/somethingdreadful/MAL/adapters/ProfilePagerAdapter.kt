package net.somethingdreadful.MAL.adapters

import android.app.Fragment
import android.app.FragmentManager
import android.support.v13.app.FragmentPagerAdapter

import net.somethingdreadful.MAL.ProfileActivity
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.cover.CoverFragment
import net.somethingdreadful.MAL.profile.ProfileDetailsAL
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL
import net.somethingdreadful.MAL.profile.ProfileFriends
import net.somethingdreadful.MAL.profile.ProfileHistory

class ProfilePagerAdapter(fm: FragmentManager, activity: ProfileActivity) : FragmentPagerAdapter(fm) {
    private val fragments: Fragments = Fragments(activity)

    init {
        if (AccountService.isMAL) {
            fragments.add(ProfileDetailsMAL(), R.string.tab_name_details)
            fragments.add(ProfileFriends(), R.string.tab_name_friends)
            if (APIHelper.isNetworkAvailable(activity)) {
                fragments.add(ProfileHistory(), R.string.tab_name_history)
                fragments.add(CoverFragment().setType(true), "ANIME")
                fragments.add(CoverFragment().setType(false), "MANGA")
            }
        } else {
            fragments.add(ProfileDetailsAL(), R.string.tab_name_details)
            fragments.add(ProfileFriends().setId(0), R.string.tab_name_following)
            if (APIHelper.isNetworkAvailable(activity)) {
                fragments.add(ProfileFriends().setId(1), R.string.tab_name_followers)
                fragments.add(ProfileHistory(), R.string.layout_card_title_activity)
                fragments.add(CoverFragment().setType(true), "ANIME")
                fragments.add(CoverFragment().setType(false), "MANGA")
            }
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragments.getFragment(position)
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): String {
        return fragments.getName(position)
    }
}