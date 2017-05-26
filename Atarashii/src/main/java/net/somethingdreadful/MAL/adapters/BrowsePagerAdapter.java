package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.BrowseActivity;
import net.somethingdreadful.MAL.BrowseFragmentAL;
import net.somethingdreadful.MAL.BrowseFragmentMAL;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.cover.CoverFragment;

public class BrowsePagerAdapter extends FragmentPagerAdapter {
    private final Fragments fragments;

    public BrowsePagerAdapter(FragmentManager fm, BrowseActivity activity) {
        super(fm);
        fragments = new Fragments(activity);

        fragments.add(AccountService.isMAL() ? new BrowseFragmentMAL() : new BrowseFragmentAL(), R.string.title_activity_browse);
        fragments.add(new CoverFragment().setType(true), "ANIME");
    }

    public void isManga(boolean manga) {
        fragments.setName(1, manga ? "MANGA" : "ANIME");
        ((CoverFragment) fragments.getFragment(1)).setType(!manga);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.getFragment(position);
    }

    @Override
    public int getCount() {
        return fragments.getSize();
    }

    @Override
    public String getPageTitle(int position) {
        return fragments.getName(position);
    }
}