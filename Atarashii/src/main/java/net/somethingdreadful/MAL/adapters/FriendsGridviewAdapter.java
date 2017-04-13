package net.somethingdreadful.MAL.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collection;

public class FriendsGridviewAdapter<T> extends ArrayAdapter<T> {
    private final Context context;
    private ArrayList<Profile> list;

    public FriendsGridviewAdapter(Context context, ArrayList<Profile> list) {
        super(context, R.layout.record_friends_gridview);
        this.context = context;
        this.list = list;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final Profile record = (list.get(position));
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.record_friends_gridview, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) view.findViewById(R.id.userName);
            viewHolder.lastOnline = (TextView) view.findViewById(R.id.lastonline);
            viewHolder.lastOnlineLabel = (TextView) view.findViewById(R.id.lastonlineLabel);
            viewHolder.avatar = (SimpleDraweeView) view.findViewById(R.id.profileImg);

            if (Theme.darkTheme) {
                viewHolder.username.setTextColor(ContextCompat.getColor(context, R.color.white));
                viewHolder.lastOnline.setTextColor(ContextCompat.getColor(context, R.color.text_dark));
                viewHolder.lastOnlineLabel.setTextColor(ContextCompat.getColor(context, R.color.text_dark));
                Theme.setBackground(context, view);
            }

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        try {
            String username = record.getUsername();
            viewHolder.username.setText(WordUtils.capitalize(username));
            if (Profile.isDeveloper(username))
                viewHolder.username.setTextColor(ContextCompat.getColor(context, R.color.primary)); //Developer
            else
                viewHolder.username.setTextColor(ContextCompat.getColor(context, Theme.darkTheme ? R.color.text_dark : R.color.bg_dark_card));

            if (record.getDetails() != null && record.getDetails().getLastOnline() != null && AccountService.isMAL()) {
                String last_online = record.getDetails().getLastOnline();
                last_online = DateTools.parseDate(last_online, true);
                viewHolder.lastOnline.setText(last_online.equals("") ? record.getDetails().getLastOnline() : last_online);
            } else {
                viewHolder.lastOnline.setText(context.getString(R.string.unknown));
            }
            viewHolder.avatar.setImageURI(record.getImageUrl());
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "FriendsActivity.ListViewAdapter(): " + e.getMessage());
            AppLog.logException(e);
        }
        return view;
    }

    public void supportAddAll(Collection<? extends T> collection) {
        this.clear();
        list = (ArrayList<Profile>) collection;
        for (T record : collection) {
            this.add(record);
        }
    }

    static class ViewHolder {
        TextView username;
        TextView lastOnline;
        TextView lastOnlineLabel;
        SimpleDraweeView avatar;
    }
}