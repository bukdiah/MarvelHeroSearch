package com.kevin.marvellookup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Kevin on 8/7/2016.
 */
public class BioFragment extends Fragment {

    public static final String BIO = "bio";

    private String bio;

    public BioFragment() {

    }

    public static BioFragment newInstance(String bio) {
        BioFragment bioFragment = new BioFragment();

        Bundle args = new Bundle();
        args.putString(BIO, bio);
        bioFragment.setArguments(args);

        return bioFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        bio = getArguments().getString(BIO);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.bio_fragment, container, false);
        View view = inflater.inflate(R.layout.bio2, container, false);

        TextView t = (TextView) view.findViewById(R.id.tvBio);

        if (bio != null && !bio.isEmpty() && !bio.trim().isEmpty())
            t.setText(Html.fromHtml(bio).toString().concat("\n\n\n\n"));
        return view;
    }
}