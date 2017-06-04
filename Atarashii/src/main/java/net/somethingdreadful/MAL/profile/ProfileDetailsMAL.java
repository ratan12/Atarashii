package net.somethingdreadful.MAL.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import org.apache.commons.lang3.text.WordUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileDetailsMAL extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private Card imagecard;
    private Card animecard;
    private Card mangacard;
    private ProfileActivity activity;

    @BindView(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.network_Card) Card networkCard;

    @BindView(R.id.Image) SimpleDraweeView image;
    @BindView(R.id.birthdaysmall) TextView tv1;
    @BindView(R.id.locationsmall) TextView tv2;
    @BindView(R.id.commentspostssmall) TextView tv3;
    @BindView(R.id.forumpostssmall) TextView tv4;
    @BindView(R.id.lastonlinesmall) TextView tv5;
    @BindView(R.id.gendersmall) TextView tv6;
    @BindView(R.id.joindatesmall) TextView tv7;
    @BindView(R.id.accessranksmall) TextView tv8;
    @BindView(R.id.atimedayssmall) TextView tv11;
    @BindView(R.id.awatchingsmall) TextView tv12;
    @BindView(R.id.acompletedpostssmall) TextView tv13;
    @BindView(R.id.aonholdsmall) TextView tv14;
    @BindView(R.id.adroppedsmall) TextView tv15;
    @BindView(R.id.aplantowatchsmall) TextView tv16;
    @BindView(R.id.atotalentriessmall) TextView tv17;
    @BindView(R.id.mtimedayssmall) TextView tv18;
    @BindView(R.id.mwatchingsmall) TextView tv19;
    @BindView(R.id.mcompletedpostssmall) TextView tv20;
    @BindView(R.id.monholdsmall) TextView tv21;
    @BindView(R.id.mdroppedsmall) TextView tv22;
    @BindView(R.id.mplantowatchsmall) TextView tv23;
    @BindView(R.id.mtotalentriessmall) TextView tv24;
    @BindView(R.id.websitesmall) TextView tv25;
    @BindView(R.id.websitefront)
    TextView tv26;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_profile_mal, container, false);

        Card.Companion.fastInit(view, R.id.details_card, R.layout.card_profile_details);
        imagecard = ((Card) view.findViewById(R.id.name_card));
        animecard = (Card) view.findViewById(R.id.Anime_card);
        mangacard = (Card) view.findViewById(R.id.Manga_card);

        imagecard.setContent(R.layout.card_image);
        animecard.setContent(R.layout.card_profile_anime);
        mangacard.setContent(R.layout.card_profile_manga);

        ButterKnife.bind(this, view);

        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        TextView tv25 = (TextView) view.findViewById(R.id.websitesmall);
        tv25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webstiteclick = Uri.parse(activity.record.getDetails().getWebsite());
                startActivity(new Intent(Intent.ACTION_VIEW, webstiteclick));
            }
        });

        activity.setDetails(this);

        if (activity.record == null)
            toggle(1);

        NfcHelper.INSTANCE.disableBeam(activity);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ProfileActivity) activity;
    }

    private void card() { //settings for hide a card and text userprofile
        if (PrefManager.getHideAnime())
            animecard.setVisibility(View.GONE);
        if (PrefManager.getHideManga())
            mangacard.setVisibility(View.GONE);
        if (activity.record.getMangaStats() == null || activity.record.getMangaStats().getTotalEntries() < 1)  //if manga (total entry) is beneath the int then hide
            mangacard.setVisibility(View.GONE);
        if (activity.record.getAnimeStats() == null || activity.record.getAnimeStats().getTotalEntries() < 1)  //if anime (total entry) is beneath the int then hide
            animecard.setVisibility(View.GONE);

        Card namecard = (Card) view.findViewById(R.id.name_card);
        namecard.getHeader().setText(WordUtils.capitalize(activity.record.getUsername()));
    }

    private void setcolor() {
        TextView tv8 = (TextView) view.findViewById(R.id.accessranksmall);
        String name = activity.record.getUsername();
        String rank = activity.record.getDetails().getAccessRank() != null ? activity.record.getDetails().getAccessRank() : "";
        if (!PrefManager.getTextColor()) {
            setColor(true);
            setColor(false);
            if (rank.contains("Administrator")) {
                tv8.setTextColor(Color.parseColor("#850000"));
            } else if (rank.contains("Moderator")) {
                tv8.setTextColor(Color.parseColor("#003385"));
            } else {
                tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
            }
            TextView tv11 = (TextView) view.findViewById(R.id.websitesmall);
            tv11.setTextColor(Color.parseColor("#002EAB"));
        }
        if (Profile.isDeveloper(name)) {
            tv8.setText(R.string.access_rank_atarashii_developer); //Developer
            tv8.setTextColor(ContextCompat.getColor(activity, R.color.primary)); //Developer
        }
    }

    private void setColor(boolean isAnime) {
        int Hue;
        TextView textview;
        if (isAnime) {
            textview = (TextView) view.findViewById(R.id.atimedayssmall); //anime
            Hue = (int) (activity.record.getAnimeStats().getTimeDays() * 2.5);
        } else {
            textview = (TextView) view.findViewById(R.id.mtimedayssmall); // manga
            Hue = (int) (activity.record.getMangaStats().getTimeDays() * 5);
        }
        if (Hue > 359)
            Hue = 359;
        textview.setTextColor(Color.HSVToColor(new float[]{Hue, 1, (float) 0.7}));
    }

    private String getStringFromResourceArray(int resArrayId, int index) {
        try { // getResources will cause a crash if an users clicks the profile fast away
            Resources res = getResources();
            try {
                String[] types = res.getStringArray(resArrayId);
                if (index < 0 || index >= types.length) // make sure to have a valid array index
                    return res.getString(R.string.not_specified);
                else
                    return types[index];
            } catch (Exception e) {
                return res.getString(R.string.not_specified);
            }
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "ProfileDetailsMAL.getStringFromResourceArray(): " + e.getMessage());
            return "Error: could not receive resources";
        }
    }

    public void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void setText() {
        if (activity.record.getDetails().getBirthday() == null) {
            tv1.setText(R.string.not_specified);
        } else {
            tv1.setText(activity.record.getDetails().getBirthday());
        }
        if (activity.record.getDetails().getLocation() == null)
            tv2.setText(R.string.not_specified);
        else
            tv2.setText(activity.record.getDetails().getLocation());
        if (activity.record.getDetails().getWebsite() != null && activity.record.getDetails().getWebsite().contains("http://") && activity.record.getDetails().getWebsite().contains(".")) { // filter fake websites
            tv25.setText(activity.record.getDetails().getWebsite().replace("http://", ""));
        } else {
            tv25.setVisibility(View.GONE);
            tv26.setVisibility(View.GONE);
        }
        tv3.setText(String.valueOf(activity.record.getDetails().getComments()));
        tv4.setText(String.valueOf(activity.record.getDetails().getForumPosts()));
        if (activity.record.getDetails().getLastOnline() != null) {
            String lastOnline = DateTools.INSTANCE.parseDate(activity.record.getDetails().getLastOnline(), true);
            tv5.setText(lastOnline.equals("") ? activity.record.getDetails().getLastOnline() : lastOnline);
        } else
            tv5.setText("-");
        tv6.setText(getStringFromResourceArray(R.array.gender, activity.record.getDetails().getGenderInt()));
        if (activity.record.getDetails().getJoinDate() != null) {
            String joinDate = DateTools.INSTANCE.parseDate(activity.record.getDetails().getJoinDate(), false);
            tv7.setText(joinDate.equals("") ? activity.record.getDetails().getJoinDate() : joinDate);
        } else
            tv7.setText("-");
        tv8.setText(activity.record.getDetails().getAccessRank());

        tv11.setText(activity.record.getAnimeStats().getTimeDays().toString());
        tv12.setText(String.valueOf(activity.record.getAnimeStats().getWatching()));
        tv13.setText(String.valueOf(activity.record.getAnimeStats().getCompleted()));
        tv14.setText(String.valueOf(activity.record.getAnimeStats().getOnHold()));
        tv15.setText(String.valueOf(activity.record.getAnimeStats().getDropped()));
        tv16.setText(String.valueOf(activity.record.getAnimeStats().getPlanToWatch()));
        tv17.setText(String.valueOf(activity.record.getAnimeStats().getTotalEntries()));

        tv18.setText(activity.record.getMangaStats().getTimeDays().toString());
        tv19.setText(String.valueOf(activity.record.getMangaStats().getReading()));
        tv20.setText(String.valueOf(activity.record.getMangaStats().getCompleted()));
        tv21.setText(String.valueOf(activity.record.getMangaStats().getOnHold()));
        tv22.setText(String.valueOf(activity.record.getMangaStats().getDropped()));
        tv23.setText(String.valueOf(activity.record.getMangaStats().getPlanToRead()));
        tv24.setText(String.valueOf(activity.record.getMangaStats().getTotalEntries()));
    }

    public void refresh() {
        try {
            if (activity.record == null) {
                if (APIHelper.isNetworkAvailable(activity)) {
                    Theme.Snackbar(activity, R.string.toast_error_UserRecord);
                } else {
                    toggle(2);
                }
            } else {
                toggle(0);
                card();
                setText();
                setcolor();

                image.setImageURI(Uri.parse(activity.record.getImageUrl()));

                /*Glide.with(activity)
                        .load(activity.record.getImageUrl())
                        .error(R.drawable.cover_error)
                        .placeholder(R.drawable.cover_loading)
                        .into(new GlideDrawableImageViewTarget(image) {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                super.onResourceReady(resource, animation);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.cover_error);
                                imagecard.wrapImage(225, 320);
                                image.setImageDrawable(drawable);
                            }
                        });*/
            }
        } catch (IllegalStateException e) {
            AppLog.log(Log.ERROR, "Atarashii", "ProfileDetailsMAL.refresh(): has been closed too fast");
        }
    }

    @Override
    public void onRefresh() {
        activity.getRecords();
    }
}
