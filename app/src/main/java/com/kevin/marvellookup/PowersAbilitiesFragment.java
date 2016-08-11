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
public class PowersAbilitiesFragment extends Fragment {

    public static final String POWERS = "powers";
    public static final String ABILITIES = "abilities";

    private String powers;
    private String abilities;

    public static PowersAbilitiesFragment newtInstance(String powers, String abilities)
    {
        PowersAbilitiesFragment powersAbilitiesFragment = new PowersAbilitiesFragment();

        Bundle args = new Bundle();
        args.putString(POWERS,powers);
        args.putString(ABILITIES,abilities);

        powersAbilitiesFragment.setArguments(args);

        return powersAbilitiesFragment;
    }

    public PowersAbilitiesFragment() {
        //empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        powers = getArguments().getString(POWERS);
        abilities = getArguments().getString(ABILITIES);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.powers_abilities_fragment,container,false);;

        TextView tvPowers = (TextView) view.findViewById(R.id.tvPowers);
        TextView tvAbilities = (TextView) view.findViewById(R.id.tvAbilities);

        tvPowers.setText(Html.fromHtml(powers).toString());
        tvAbilities.setText(Html.fromHtml(abilities).toString());
        return view;
    }
}
