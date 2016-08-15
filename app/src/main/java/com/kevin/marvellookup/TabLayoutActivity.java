package com.kevin.marvellookup;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TabLayoutActivity extends AppCompatActivity {

    private String bio;
    private String powers;
    private String abilities;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);
        //Retrieve Bundle sent from man activity
        Bundle b = getIntent().getExtras();

        if(b != null)
        {
            Log.d("TabLayout Bundle", "bio: "+b.getString("bio"));
            Log.d("TabLayout Bundle", "powers: "+b.getString("powers"));
            Log.d("TabLayout Bundle", "abilities: "+b.getString("abilities"));

            bio = b.getString(BioFragment.BIO);
            powers = b.getString(PowersAbilitiesFragment.POWERS);
            abilities = b.getString(PowersAbilitiesFragment.ABILITIES);

            //System.out.println("BIO: "+bio);
        }
        else
        {
            Log.d("TabLayout Bundle", "NULL ");
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //adapter.addFragment(new BioFragment(), "Biography");
        adapter.addFragment(BioFragment.newInstance(bio),"Biography");
        //adapter.addFragment(new PowersAbilitiesFragment(), "Attributes/Powers");
        adapter.addFragment(PowersAbilitiesFragment.newtInstance(powers,abilities), "Powers/Abilities");
        adapter.addFragment(new SeriesFragment(), "Series");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
