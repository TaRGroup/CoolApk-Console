package com.targroup.coolapkconsole.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.activities.DetailActivity;
import com.targroup.coolapkconsole.model.AppItem;

import java.util.List;

/**
 * Created by Trumeet on 2017/4/2.
 * @author Trumeet
 */

public class AppListAdapter extends ArrayAdapter<AppItem> {
    private List<AppItem> mList;
    private Context mContext;
    public AppListAdapter (Context context, List<AppItem> list) {
        super(context, 0, list);
        mContext = context;
        mList = list;
    }
    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView,
                 @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_app, parent, false);
        }
        final AppItem item = mList.get(position);
        CardView card = (CardView)convertView.findViewById(R.id.item_card);
        ImageView icon = (ImageView)convertView.findViewById(R.id.item_icon);
        TextView title = (TextView)convertView.findViewById(R.id.item_title);
        TextView subtitle = (TextView)convertView.findViewById(R.id.item_subtitle);
        TextView context = (TextView)convertView.findViewById(R.id.item_context);
        Glide.with(mContext)
                .load(item.getIcon())
                .into(icon);
        title.setText(item.getName());
        subtitle.setText(item.getStatus());
        context.setText(mContext.getString(R.string.apk_item_context, item.getDownloads()));
        // Card's clicking listener
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(mContext, DetailActivity.class)
                        .putExtra(DetailActivity.EXTRA_APP_ITEM, item));
            }
        });
        return convertView;
    }
}