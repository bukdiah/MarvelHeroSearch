package com.kevin.marvellookup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevin.marvellookup.HeroInfo;
import com.kevin.marvellookup.R;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 8/9/2016.
 */
public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.MyViewHolder> {
    List<HeroInfo> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public HeroAdapter(Context context, List<HeroInfo> data)
    {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_layout_copy,parent,false);

        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HeroInfo current = data.get(position);

        holder.name.setText(current.getName());
        holder.name.setMaxEms(current.getCharId());
        //holder.icon.setImageResource(current.getIconId());

        //Load image from URL into ImageView
        Picasso
                .with(context)
                .load(current.getImageURL())
                .into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static ClickListener clickListener;

    public interface ClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setClickListener(ClickListener clickListener)
    {
        this.clickListener = clickListener;
    }

    public static class MyViewHolder extends ViewHolder{
        public ImageView icon;
        public TextView name;

        public MyViewHolder(final View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tvHero);
            icon = (ImageView) itemView.findViewById(R.id.ivHero);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(clickListener != null)
                        clickListener.onItemClick(itemView,getLayoutPosition());
                }
            });


        }
    }
}
