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

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 8/9/2016.
 */
public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.MyViewHolder> {
    List<HeroInfo> data = Collections.emptyList();
    private LayoutInflater inflater;

    public HeroAdapter(Context context, List<HeroInfo> data)
    {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_layout,parent,false);

        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HeroInfo current = data.get(position);

        holder.name.setText(current.getName());
        holder.icon.setImageResource(current.getIconId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends ViewHolder{
        ImageView icon;
        TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tvHero);
            icon = (ImageView) itemView.findViewById(R.id.ivHero);

        }
    }
}
