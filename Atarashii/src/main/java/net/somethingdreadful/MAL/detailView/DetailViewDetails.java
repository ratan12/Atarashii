package net.somethingdreadful.MAL.detailView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TableRow;
import android.widget.TextView;

import net.somethingdreadful.MAL.Card;
import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.DetailViewRelationsAdapter;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailViewDetails extends Fragment implements Serializable {
    private View view;
    private Card cardSynopsis;
    private Card cardMediainfo;
    private Card cardMediaStats;
    private Card cardRelations;
    private Card cardTitles;
    private Card cardMusic;
    private Card cardExternal;
    private DetailView activity;
    private ExpandableListView relations;
    private ExpandableListView titles;
    private ExpandableListView musics;
    private DetailViewRelationsAdapter relation;
    private DetailViewRelationsAdapter title;
    private DetailViewRelationsAdapter music;

    @BindView(R.id.swiperefresh) public SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.SynopsisContent) TextView synopsis;
    @BindView(R.id.type) TextView type;
    @BindView(R.id.episodes) TextView episodes;
    @BindView(R.id.episodesLabel) TextView episodesLabel;
    @BindView(R.id.volumes) TextView volumes;
    @BindView(R.id.volumesLabel) TextView volumesLabel;
    @BindView(R.id.volumesRow) TableRow volumesRow;
    @BindView(R.id.status) TextView status;
    @BindView(R.id.start) TextView start;
    @BindView(R.id.startRow) TableRow startRow;
    @BindView(R.id.duration) TextView duration;
    @BindView(R.id.durationRow) TableRow durationRow;
    @BindView(R.id.broadcast) TextView broadcast;
    @BindView(R.id.broadcastRow) TableRow broadcastRow;
    @BindView(R.id.end) TextView end;
    @BindView(R.id.endRow) TableRow endRow;
    @BindView(R.id.classification) TextView classification;
    @BindView(R.id.classificationRow) TableRow classificationRow;
    @BindView(R.id.genres) TextView genres;
    @BindView(R.id.producers) TextView producers;
    @BindView(R.id.producersRow) TableRow producersRow;
    @BindView(R.id.officialPanel) TextView officialPanel;
    @BindView(R.id.divider4) View officialdivider;

    @BindView(R.id.infoValue1) TextView infoValue1;
    @BindView(R.id.infoText2) TextView infoText2;
    @BindView(R.id.infoValue2) TextView infoValue2;
    @BindView(R.id.infoText3) TextView infoText3;
    @BindView(R.id.infoValue3) TextView infoValue3;
    @BindView(R.id.infoText4) TextView infoText4;
    @BindView(R.id.infoValue4) TextView infoValue4;
    @BindView(R.id.infoText5) TextView infoText5;
    @BindView(R.id.infoValue5) TextView infoValue5;
    @BindView(R.id.infoText6) TextView infoText6;
    @BindView(R.id.infoValue6) TextView infoValue6;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        super.onCreate(state);
        view = inflater.inflate(R.layout.activity_detailview_details, container, false);

        setViews();
        swipeRefresh.setOnRefreshListener(activity);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        activity.setDetails(this);

        if (activity.isDone())
            setText();
        else if (!APIHelper.isNetworkAvailable(activity))
            toggleView(false);
        return view;
    }

    /*
     * The scrollview bugs when you use viewflipper in it!
     */
    private void toggleView(Boolean show) {
        if (show) {
            cardSynopsis.setVisibility(View.VISIBLE);
            cardMediainfo.setVisibility(View.VISIBLE);
            cardMediaStats.setVisibility(View.VISIBLE);
            cardRelations.setVisibility(View.VISIBLE);
            cardTitles.setVisibility(View.VISIBLE);
            cardMusic.setVisibility(View.VISIBLE);
            cardExternal.setVisibility(View.VISIBLE);
        } else {
            cardSynopsis.setVisibility(View.GONE);
            cardMediainfo.setVisibility(View.GONE);
            cardMediaStats.setVisibility(View.GONE);
            cardRelations.setVisibility(View.GONE);
            cardTitles.setVisibility(View.GONE);
            cardMusic.setVisibility(View.GONE);
            cardExternal.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((DetailView) activity);
    }

    /**
     * Set all views once
     */
    private void setViews() {
        // set all the card views
        cardSynopsis = (Card) view.findViewById(R.id.synopsis);
        cardMediainfo = (Card) view.findViewById(R.id.mediainfo);
        cardMediaStats = (Card) view.findViewById(R.id.mediastats);
        cardRelations = (Card) view.findViewById(R.id.relations);
        cardTitles = (Card) view.findViewById(R.id.titles);
        cardMusic = (Card) view.findViewById(R.id.music);
        cardExternal = (Card) view.findViewById(R.id.externalLinks);

        cardSynopsis.setContent(R.layout.card_detailview_synopsis);
        cardMediainfo.setContent(R.layout.card_detailview_details_mediainfo);
        cardMediaStats.setContent(R.layout.card_detailview_details_mediastats);
        cardRelations.setContent(R.layout.card_detailview_details_relations);
        cardTitles.setContent(R.layout.card_detailview_details_relations);
        cardMusic.setContent(R.layout.card_detailview_details_relations);
        cardExternal.setContent(R.layout.card_detailview_details_external);
        cardExternal.setAllPadding(0, 0, 0, 0);

        // set all the views
        ButterKnife.bind(this, view);

        if (!AccountService.Companion.isMAL()) {
            producers.setVisibility(View.GONE);
            producersRow.setVisibility(View.GONE);
            classification.setVisibility(View.GONE);
            classificationRow.setVisibility(View.GONE);
            cardMusic.setVisibility(View.GONE);
        }

        relation = new DetailViewRelationsAdapter(activity.getApplicationContext());
        relations = (ExpandableListView) cardRelations.findViewById(R.id.ListView);
        relations.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPos, int childPos, long l) {
                Intent detailView = new Intent(activity, DetailView.class);
                detailView.putExtra("recordID", relation.getRecordStub(groupPos, childPos).getId());
                detailView.putExtra("recordType", relation.getRecordStub(groupPos, childPos).getType());
                startActivity(detailView);
                return true;
            }
        });
        relations.setAdapter(relation);

        music = new DetailViewRelationsAdapter(activity.getApplicationContext());
        musics = (ExpandableListView) cardMusic.findViewById(R.id.ListView);
        musics.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPos, int childPos, long l) {
                // #[TRACK NUMBER]: \"[TITLE] ([JAPANESE TITLE])\" [SINGER] ([EPISODES])
                Pattern pattern = Pattern.compile("\"(.+?)\\((.+?)\"(.+?)\\(");
                Matcher matcher = pattern.matcher(music.getRecordStub(groupPos, childPos).getTitle());
                String query;
                if (matcher.find()) {
                    query = TextUtils.htmlEncode(matcher.group(1) + matcher.group(3));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + query)));
                } else {
                    // #[TRACK NUMBER]: \"[TITLE] [SINGER] ([EPISODES])
                    pattern = Pattern.compile("\"(.+?)\"(.+?)\\(");
                    matcher = pattern.matcher(music.getRecordStub(groupPos, childPos).getTitle());
                    if (matcher.find()) {
                        query = TextUtils.htmlEncode(matcher.group(1) + matcher.group(2));
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + query)));
                    } else {
                        query = TextUtils.htmlEncode(music.getRecordStub(groupPos, childPos).getTitle().replace("\"", ""));
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + query)));
                    }
                }
                return true;
            }
        });
        musics.setAdapter(music);

        title = new DetailViewRelationsAdapter(activity.getApplicationContext());
        titles = (ExpandableListView) cardTitles.findViewById(R.id.ListView);
        titles.setAdapter(title);

        clickListeners();
    }

    /**
     * Place all the text in the right textview
     */
    @SuppressLint("SetTextI18n")
    public void setText() {
        GenericRecord record = (activity.isAnime ? activity.animeRecord : activity.mangaRecord);
        if (record.getSynopsis() == null)
            return;

        toggleView(true);
        activity.setMenu();

        synopsis.setText(record.getSynopsis());
        synopsis.setMovementMethod(LinkMovementMethod.getInstance());
        genres.setText("\u200F" + TextUtils.join(", ", record.getGenresString(activity)));
        if (activity.isAnime) {
            type.setText(activity.animeRecord.getType());
            episodes.setText(activity.nullCheck(activity.animeRecord.getEpisodes()));
            if (activity.animeRecord.getDuration() == 0)
                durationRow.setVisibility(View.GONE);
            else
                duration.setText(activity.nullCheck(activity.animeRecord.getDuration()) + " " + getString(R.string.card_content_minutes));
            if (activity.animeRecord.getAiring() == null || activity.animeRecord.getAiring().getTime() == null)
                broadcastRow.setVisibility(View.GONE);
            else
                broadcast.setText(activity.nullCheck(DateTools.INSTANCE.parseDate(activity.animeRecord.getAiring().getTime(), true)));
            volumes.setVisibility(View.GONE);
            volumesRow.setVisibility(View.GONE);
            volumesLabel.setVisibility(View.GONE);
            status.setText(activity.animeRecord.getStatusString(activity));
            classification.setText(activity.animeRecord.getClassificationString(activity));
            start.setText(activity.getDate(activity.animeRecord.getStartDate()));
            end.setText(activity.getDate(activity.animeRecord.getEndDate()));
            producers.setText("\u200F" + activity.animeRecord.getProducersString());
            if (activity.animeRecord.getExternalLinks() == null || activity.animeRecord.getExternalLinks().getOfficialSite() == null) {
                officialPanel.setVisibility(View.GONE);
                officialdivider.setVisibility(View.GONE);
            }
        } else {
            type.setText(activity.mangaRecord.getType());
            episodes.setText(activity.nullCheck(activity.mangaRecord.getChapters()));
            episodesLabel.setText(R.string.label_Chapters);
            volumes.setText(activity.nullCheck(activity.mangaRecord.getVolumes()));
            status.setText(activity.mangaRecord.getStatusString(activity));
            classificationRow.setVisibility(View.GONE);
            startRow.setVisibility(View.GONE);
            endRow.setVisibility(View.GONE);
            durationRow.setVisibility(View.GONE);
            broadcastRow.setVisibility(View.GONE);
            producersRow.setVisibility(View.GONE);
            cardMusic.setVisibility(View.GONE);
            cardExternal.setVisibility(View.GONE);
        }

        // Information card
        infoValue1.setText(record.getAverageScore());
        if (AccountService.Companion.isMAL()) {
            infoValue2.setText(String.valueOf(record.getRank()));
            infoValue3.setText(String.valueOf(record.getPopularity()));
            infoValue4.setText(String.valueOf(record.getAverageScoreCount()));
            infoValue5.setText(String.valueOf(record.getFavoritedCount()));
            infoValue6.setVisibility(View.GONE);
            infoText6.setVisibility(View.GONE);
        } else {
            infoValue2.setText(String.valueOf(record.getListStats().getPlanned()));
            infoValue3.setText(String.valueOf(record.getListStats().getReadWatch()));
            infoValue4.setText(String.valueOf(record.getListStats().getCompleted()));
            infoValue5.setText(String.valueOf(record.getListStats().getOnHold()));
            infoValue6.setText(String.valueOf(record.getListStats().getDropped()));
            infoText2.setText(getString(R.string.listType_planned));
            infoText3.setText(getString(R.string.listType_InProgress));
            infoText4.setText(getString(R.string.listType_Completed));
            infoText5.setText(getString(R.string.listType_onhold));
            infoText6.setText(getString(R.string.listType_dropped));
        }

        relation.clear();
        title.clear();
        music.clear();

        if (activity.isAnime) {
            relation.addRelations(activity.animeRecord.getMangaAdaptations(), getString(R.string.card_content_adaptions));
            relation.addRelations(activity.animeRecord.getParentStory(), getString(R.string.card_content_parentstory));
            relation.addRelations(activity.animeRecord.getPrequels(), getString(R.string.card_content_prequel));
            relation.addRelations(activity.animeRecord.getSequels(), getString(R.string.card_content_sequel));
            relation.addRelations(activity.animeRecord.getSideStories(), getString(R.string.card_content_sidestories));
            relation.addRelations(activity.animeRecord.getSpinOffs(), getString(R.string.card_content_spinoffs));
            relation.addRelations(activity.animeRecord.getSummaries(), getString(R.string.card_content_summaries));
            relation.addRelations(activity.animeRecord.getCharacterAnime(), getString(R.string.card_content_character));
            relation.addRelations(activity.animeRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));
            relation.addRelations(activity.animeRecord.getOther(), getString(R.string.card_content_other));

            music.addTitles(activity.animeRecord.getOpeningTheme(), getString(R.string.card_content_opening));
            music.addTitles(activity.animeRecord.getEndingTheme(), getString(R.string.card_content_ending));

            title.addTitles(activity.animeRecord.getTitleRomaji(), getString(R.string.card_content_romaji));
            title.addTitles(activity.animeRecord.getTitleJapanese(), getString(R.string.card_content_japanese));
            title.addTitles(activity.animeRecord.getTitleEnglish(), getString(R.string.card_content_english));
            title.addTitles(activity.animeRecord.getTitleSynonyms(), getString(R.string.card_content_synonyms));
        } else {
            relation.addRelations(activity.mangaRecord.getAnimeAdaptations(), getString(R.string.card_content_adaptions));
            relation.addRelations(activity.mangaRecord.getRelatedManga(), getString(R.string.card_content_related));
            relation.addRelations(activity.mangaRecord.getAlternativeVersions(), getString(R.string.card_content_alternativeversions));

            title.addTitles(activity.mangaRecord.getTitleRomaji(), getString(R.string.card_content_romaji));
            title.addTitles(activity.mangaRecord.getTitleJapanese(), getString(R.string.card_content_japanese));
            title.addTitles(activity.mangaRecord.getTitleEnglish(), getString(R.string.card_content_english));
            title.addTitles(activity.mangaRecord.getTitleSynonyms(), getString(R.string.card_content_synonyms));
        }

        relation.notifyDataSetChanged();
        title.notifyDataSetChanged();
        music.notifyDataSetChanged();
        cardRelations.refreshList(relation);
        cardTitles.refreshList(title);
        cardMusic.refreshList(music);
    }

    /**
     * Handle the click events (expand and collapse)
     */
    private void clickListeners() {
        titles.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                title.expand(i);
                cardTitles.refreshList(title);
            }
        });
        titles.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                title.collapse(i);
                cardTitles.refreshList(title);
            }
        });

        relations.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                relation.expand(i);
                cardRelations.refreshList(relation);
            }
        });
        relations.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                relation.collapse(i);
                cardRelations.refreshList(relation);
            }
        });

        musics.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                music.expand(i);
                cardMusic.refreshList(music);
            }
        });
        musics.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                music.collapse(i);
                cardMusic.refreshList(music);
            }
        });
    }

    @OnClick({R.id.googlePanel, R.id.youtubePanel, R.id.wikipediaPanel, R.id.officialPanel, R.id.animedbPanel, R.id.animenewsnetworkPanel})
    public void doClick(View view) {
        String title = activity.isAnime() ? activity.animeRecord.getTitle() : activity.mangaRecord.getTitle();
        String url = "https://";
        switch (view.getId()) {
            case R.id.googlePanel:
                url += "google.nl/?gws_rd=ssl#q=" + TextUtils.htmlEncode(title);
                break;
            case R.id.youtubePanel:
                url += "youtube.com/results?search_query=" + TextUtils.htmlEncode(title);
                break;
            case R.id.wikipediaPanel:
                if (activity.isAnime() && activity.animeRecord.getExternalLinks().getWikipedia() != null)
                    url = activity.animeRecord.getExternalLinks().getWikipedia();
                else
                    url += "en.wikipedia.org/wiki/" + TextUtils.htmlEncode(title);
                break;
            case R.id.officialPanel:
                url = activity.animeRecord.getExternalLinks().getOfficialSite();
                break;
            case R.id.animedbPanel:
                if (activity.isAnime() && activity.animeRecord.getExternalLinks().getAnimeDB() != null)
                    url = activity.animeRecord.getExternalLinks().getAnimeDB();
                else
                    url += "anidb.net/perl-bin/animedb.pl?show=animelist&do.search=Search&adb.search=" + TextUtils.htmlEncode(title);
                break;
            case R.id.animenewsnetworkPanel:
                if (activity.isAnime() && activity.animeRecord.getExternalLinks().getAnimeNewsNetwork() != null)
                    url = activity.animeRecord.getExternalLinks().getAnimeNewsNetwork();
                else
                    url += "animenewsnetwork.com/search?q=" + TextUtils.htmlEncode(title);
                break;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}