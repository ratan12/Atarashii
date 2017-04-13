package net.somethingdreadful.MAL.cover;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.DraweeView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import net.somethingdreadful.MAL.AppLog;
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

class CoverAdapter extends RecyclerView.Adapter<CoverAdapter.itemHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private int resource;
    private Drawable action1Drawable;
    private Drawable action2Drawable;
    private Drawable action3Drawable;
    private int coverHeight;
    private boolean list = false;
    private Activity activity;
    private boolean isAnime;
    private CoverFragment.CoverListener listener;
    @Setter @Getter ArrayList<IGFModel.IGFItem> recordList = new ArrayList<>();
    @Setter @Getter private ArrayList<String> fastScrollText;

    CoverAdapter(Activity activity, boolean isAnime, int coverHeight, CoverFragment.CoverListener listener, ArrayList<Integer> actionIcons) {
        list = PrefManager.getTraditionalListEnabled();
        Resources res = activity.getResources();

        if (list) {
            action1Drawable = VectorDrawableCompat.create(res, actionIcons.get(0), null);
            action2Drawable = VectorDrawableCompat.create(res, actionIcons.get(1), null);
            action3Drawable = VectorDrawableCompat.create(res, actionIcons.get(2), null);
            resource = Theme.darkTheme ? R.layout.record_igf_listview_dark : R.layout.record_igf_listview_light;
        } else {
            action1Drawable = VectorDrawableCompat.create(res, actionIcons.get(3), null);
            action2Drawable = VectorDrawableCompat.create(res, actionIcons.get(4), null);
            action3Drawable = VectorDrawableCompat.create(res, actionIcons.get(5), null);
            resource = R.layout.record_igf_grid;
        }

        this.activity = activity;
        this.isAnime = isAnime;
        this.listener = listener;
        this.coverHeight = coverHeight;
    }

    void addRecords(ArrayList<IGFModel.IGFItem> recordList) {
        this.recordList.addAll(recordList);
    }

    @Override
    public itemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final itemHolder itemView = new itemHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));

        // Set the clicklisteners
        itemView.mainPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CoverAction(activity, isAnime).openDetails(recordList.get(itemView.getAdapterPosition()));
            }
        });
        itemView.action1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = itemView.getAdapterPosition();
                listener.onCoverClicked(position, 1, isAnime, recordList.get(position));
            }
        });
        itemView.action2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = itemView.getAdapterPosition();
                listener.onCoverClicked(position, 2, isAnime, recordList.get(position));
            }
        });
        itemView.action3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = itemView.getAdapterPosition();
                listener.onCoverClicked(position, 3, isAnime, recordList.get(position));
            }
        });
        return itemView;
    }

    @Override
    public void onBindViewHolder(itemHolder holder, final int position) {
        try {
            final IGFModel.IGFItem record = recordList.get(position);
            holder.title.setText(record.getTitle());
            if (list) {
                holder.text1.setText(record.getProgress());
                holder.text2.setText(record.getScore());
                holder.text3.setText(Html.fromHtml(record.getStatus() + "\u2606"));
                if (record.getUserStatusRaw() != null || record.getUserStatusRaw().equals(GenericRecord.STATUS_COMPLETED))
                    holder.action3.setVisibility(View.GONE);
                else
                    holder.action3.setVisibility(View.VISIBLE);
            } else {
                holder.text1.setText(record.getShortDetails());
            }

            holder.cover.setController(Fresco.newDraweeControllerBuilder()
                    .setFirstAvailableImageRequests(record.getImageUrlArray())
                    .setOldController(holder.cover.getController())
                    .build());
        } catch (Exception e) {
            AppLog.logTaskCrash("ScheduleActivity", e.getMessage(), e);
        }
    }

    @Override
    public void onViewDetachedFromWindow(itemHolder holder) {
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return fastScrollText.size() != 0 ? fastScrollText.get(position) : "";
    }

    /**
     * The viewholder for performance.
     */
    class itemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.igf_title) TextView title;
        @BindView(R.id.igf_text1) TextView text1;
        @BindView(R.id.igf_text2) TextView text2;
        @BindView(R.id.igf_text3) TextView text3;
        @BindView(coverImage) DraweeView cover;
        @BindView(R.id.mainPanel) RelativeLayout mainPanel;
        @BindView(igf_action1) ImageView action1;
        @BindView(igf_action2) ImageView action2;
        @BindView(igf_action3) ImageView action3;

        itemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            action1.setImageDrawable(action1Drawable);
            action2.setImageDrawable(action2Drawable);
            action3.setImageDrawable(action3Drawable);
            if (!PrefManager.getTraditionalListEnabled())
                itemView.getLayoutParams().height = coverHeight;
        }
    }
}