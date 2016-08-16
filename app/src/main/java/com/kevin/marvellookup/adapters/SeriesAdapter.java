package com.kevin.marvellookup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevin.marvellookup.R;
import com.kevin.marvellookup.ComicsInfo;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 8/14/2016.
 */
public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.MyViewHolder>{
    List<ComicsInfo> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public SeriesAdapter(Context context, List<ComicsInfo> data) {
        this.data = data;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.series_row,parent,false);

        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ComicsInfo current = data.get(position);

        holder.title.setText(current.getName());
        //holder.icon.setImageResource(current.getIconId());

        //Load image from URL into ImageView
        Picasso
                .with(context)
                //.load(current.getIconId())
                .load(current.getImageURL())
                .into(holder.cover);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView cover;
        public TextView title;

        public MyViewHolder(final View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.tvSeriesTitle);
            cover = (ImageView) itemView.findViewById(R.id.ivCover);
            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(clickListener != null)
                        clickListener.onItemClick(itemView,getLayoutPosition());
                }
            });*/


        }
    }
}
