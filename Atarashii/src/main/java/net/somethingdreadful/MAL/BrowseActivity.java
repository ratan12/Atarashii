package net.somethingdreadful.MAL;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.somethingdreadful.MAL.adapters.BrowsePagerAdapter;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.cover.CoverAction;
import net.somethingdreadful.MAL.cover.CoverFragment;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class BrowseActivity extends AppCompatActivity implements CoverFragment.CoverListener {
    public CoverFragment coverFragment;
    @Getter BrowsePagerAdapter browsePagerAdapter;
    @BindView(R.id.pager) ViewPager viewPager;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.theme_viewpager, false);
        browsePagerAdapter = (BrowsePagerAdapter) Theme.setActionBar(this, new BrowsePagerAdapter(getFragmentManager(), this));
        ButterKnife.bind(this);

        NfcHelper.disableBeam(this);
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

    /**
     * Show the dialog with the tag
     */
    public void showDialog(String tag, DialogFragment dialog, Bundle args) {
        FragmentManager fm = getFragmentManager();
        dialog.setArguments(args);
        dialog.show(fm, "fragment_" + tag);
    }

    /**
     * Get the translation for the API.
     *
     * @param input      The array with the locale strings
     * @param inputarray The array ID with the locale strings
     * @param fixedarray The Fixed array
     * @return ArrayList<String> The api values
     */
    public String getAPIValue(String input, int inputarray, int fixedarray) {
        String[] inputString = getResources().getStringArray(inputarray);
        String[] fixedString = getResources().getStringArray(fixedarray);
        return fixedString[Arrays.asList(inputString).indexOf(input)];
    }

    @Override
    public void onCoverLoaded(CoverFragment coverFragment) {
        this.coverFragment = coverFragment;
    }

    @Override
    public void onCoverRequest(boolean isAnime) {
    }

    @Override
    public void onCoverClicked(int position, int actionId, boolean isAnime, IGFModel.IGFItem item) {
        new CoverAction(this, isAnime).openDetails(item);
    }
}
