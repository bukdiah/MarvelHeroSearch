package com.kevin.marvellookup;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

    public static BioFragment newInstance(String bio)
    {
        BioFragment bioFragment = new BioFragment();

        Bundle args = new Bundle();
        args.putString(BIO,bio);
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
        View view = inflater.inflate(R.layout.bio_fragment, container, false);

        TextView t = (TextView) view.findViewById(R.id.tvBio);

        t.setText(Html.fromHtml(bio).toString());
        return view;
    }
}
