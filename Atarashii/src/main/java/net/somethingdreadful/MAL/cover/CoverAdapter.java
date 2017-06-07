package net.somethingdreadful.MAL.cover;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

import static net.somethingdreadful.MAL.R.id.coverImage;
import static net.somethingdreadful.MAL.R.id.igf_action1;
import static net.somethingdreadful.MAL.R.id.igf_action2;
import static net.somethingdreadful.MAL.R.id.igf_action3;
import static net.somethingdreadful.MAL.Theme.context;

class CoverAdapter extends BaseAdapter implements SectionIndexer, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private final LayoutInflater inflater;
    private int resource;
    private int action1Id;
    private int action2Id;
    private int action3Id;
    private int coverHeight;
    private boolean list = false;
    private CoverFragment coverFragment;
    private boolean isAnime;
    private CoverFragment.CoverListener listener;
    @Setter @Getter ArrayList<IGFModel.IGFItem> recordList = new ArrayList<>();
    @Setter @Getter private ArrayList<String> fastScrollText;
    GenericDraweeHierarchyBuilder builder;

    CoverAdapter(CoverFragment coverFragment, boolean isAnime, CoverFragment.CoverListener listener, ArrayList<Integer> actionIcons) {
        list = PrefManager.getTraditionalListEnabled();

        if (list) {
            action1Id = actionIcons.get(0);
            action2Id = actionIcons.get(1);
            action3Id = actionIcons.get(2);
            resource = Theme.darkTheme ? R.layout.record_igf_listview_dark : R.layout.record_igf_listview_light;
        } else {
            action1Id = actionIcons.get(3);
            action2Id = actionIcons.get(4);
            action3Id = actionIcons.get(5);
            resource = R.layout.record_igf_grid;
        }

        this.coverFragment = coverFragment;
        this.isAnime = isAnime;
        this.listener = listener;
        this.coverHeight = PrefManager.getCoverHeight();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder = new GenericDraweeHierarchyBuilder(coverFragment.getResources());
    }

    void addRecords(ArrayList<IGFModel.IGFItem> recordList) {
        this.recordList.addAll(recordList);
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        itemHolder viewHolder;

        if (view == null) {
            view = inflater.inflate(resource, viewGroup, false);
            viewHolder = new itemHolder(view, position);

            view.setTag(viewHolder);
        } else {
            viewHolder = (itemHolder) view.getTag();
        }

        final IGFModel.IGFItem record = recordList.get(position);
        final DraweeView cover = viewHolder.cover;
        viewHolder.title.setText(record.getTitle());
        if (list) {
            viewHolder.text1.setText(record.getProgress());
            viewHolder.text2.setText(record.getScore());
            viewHolder.text3.setText(Html.fromHtml(record.getStatus() + "\u2606"));
            if (record.getUserStatusRaw() != null || record.getUserStatusRaw().equals(GenericRecord.STATUS_COMPLETED))
                viewHolder.action3.setVisibility(View.GONE);
            else
                viewHolder.action3.setVisibility(View.VISIBLE);
        } else {
            viewHolder.text1.setText(record.getShortDetails());
        }

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setFirstAvailableImageRequests(record.getImageUrlArray())
                .setOldController(cover.getController())
                .build();
        cover.setController(controller);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        IGFModel.IGFItem record = recordList.get(position);
        switch (view.getId()) {
            case R.id.mainPanel:
                new CoverAction(coverFragment.getActivity(), isAnime).openDetails(record);
                break;
            case R.id.igf_action1:
                listener.onCoverClicked(position, 1, isAnime, record);
                break;
            case R.id.igf_action2:
                listener.onCoverClicked(position, 2, isAnime, record);
                break;
            case R.id.igf_action3:
                listener.onCoverClicked(position, 3, isAnime, record);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        QuickActionsDialog.newInstance(coverFragment.getActivity(), isAnime, coverFragment, recordList.get(position));
        return true;
    }

    @Override
    public Object[] getSections() {
        return fastScrollText.toArray();
    }

    @Override
    public int getPositionForSection(int i) {
        return i;
    }

    @Override
    public int getSectionForPosition(int i) {
        return i;
    }

    /**
     * The viewholder for performance.
     */
    class itemHolder {
        @BindView(R.id.igf_title) TextView title;
        @BindView(R.id.igf_text1) TextView text1;
        @Nullable @BindView(R.id.igf_text2) TextView text2;
        @Nullable @BindView(R.id.igf_text3) TextView text3;
        @BindView(coverImage) DraweeView cover;
        @BindView(R.id.mainPanel) RelativeLayout mainPanel;
        @Nullable @BindView(igf_action1) ImageView action1;
        @Nullable @BindView(igf_action2) ImageView action2;
        @BindView(igf_action3) ImageView action3;

        itemHolder(View view, final int position) {
            ButterKnife.bind(this, view);

            if (!list) {
                view.getLayoutParams().height = coverHeight;
            } else if (action1 != null && action2 != null) {
                action1.setImageResource(action1Id);
                action1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onCoverClicked(position, 1, isAnime, recordList.get(position));
                    }
                });
                action2.setImageResource(action2Id);
                action2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onCoverClicked(position, 2, isAnime, recordList.get(position));
                    }
                });
            }

            action3.setImageResource(action3Id);
            action3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onCoverClicked(position, 3, isAnime, recordList.get(position));
                }
            });

            GenericDraweeHierarchy hierarchy = builder
                    .setPlaceholderImage(R.drawable.cover_loading)
                    .setFailureImage(R.drawable.cover_error)
                    .build();
            cover.setHierarchy(hierarchy);
        }
    }
}