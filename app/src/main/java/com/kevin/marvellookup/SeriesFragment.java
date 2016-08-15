package com.kevin.marvellookup;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kevin.marvellookup.adapters.SeriesAdapter;

import java.util.ArrayList;

/**
 * Created by Kevin on 8/13/2016.
 */
public class SeriesFragment extends Fragment {
    ArrayList<SeriesInfo> series;

    private Context context;

    public SeriesFragment() {
    }

    public static SeriesFragment newInstance()
    {
        SeriesFragment seriesFragment = new SeriesFragment();
        return seriesFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        if (context != null)
        {
            Log.d("SERIES FRAG", "onCreate: context != null");
        }
        else
            Log.d("SERIES FRAG", "onCreate: context == null!");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.series_fragment, container, false);

        series = SeriesInfo.createContactsList(16);

        SeriesAdapter adapter = new SeriesAdapter(context,series);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvSeries);

        GridLayoutManager glm = new GridLayoutManager(getContext(),4);
        recyclerView.setLayoutManager(glm);

        //RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(context,DividerItemDecoration.HORIZONTAL_LIST);
        //recyclerView.addItemDecoration(itemDecoration);
        recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(
                5,4
        ));

        if(isAdded())
            recyclerView.setAdapter(adapter);



        return view;
    }

}
