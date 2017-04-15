package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.cover.CoverFragment;

public class IGFPagerAdapter extends FragmentPagerAdapter {

    /**
     * Init page adapter
     */
    public IGFPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return new CoverFragment().setType(i == 0);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0? "ANIME" :  "MANGA";
    }
}