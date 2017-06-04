package net.somethingdreadful.MAL.adapters

import android.app.Fragment
import android.app.FragmentManager
import android.support.v13.app.FragmentPagerAdapter

import net.somethingdreadful.MAL.cover.CoverFragment

class IGFPagerAdapter
/**
 * Init page adapter
 */
(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(i: Int): Fragment {
        return CoverFragment().setType(i == 0)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0) "ANIME" else "MANGA"
    }
}