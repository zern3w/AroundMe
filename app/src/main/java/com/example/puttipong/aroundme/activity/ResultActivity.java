package com.example.puttipong.aroundme.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.adapter.SectionPageAdapter;
import com.example.puttipong.aroundme.fragment.Tab1Fragment;
import com.example.puttipong.aroundme.fragment.Tab2Fragment;

public class ResultActivity extends AppCompatActivity {
    private String searchText;
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private Tab1Fragment tab1Fragment;
    private Tab2Fragment tab2Fragment;
    private SectionPageAdapter mSectionPageAdapter;
    final int[] ICONS = new int[]{
            R.drawable.ic_list_white_24dp,
            R.drawable.ic_place_white_24dp
    };

    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initInstance();
    }

    private void initInstance() {
        tab1Fragment = new Tab1Fragment();
        tab2Fragment = new Tab2Fragment();
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mSectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());

        Intent intent = getIntent();
        searchText = intent.getStringExtra("SEARCH_TEXT");
        Log.i(TAG, "initInstance: " + searchText);

        // Set up the ViewPager with the sections adapter.
        setupViewPager(mViewPager);

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        adapter.addFragment(Tab1Fragment.newInstance(searchText), getString(R.string.tab_text_1));
        adapter.addFragment(tab2Fragment, getString(R.string.tab_text_2));
        viewPager.setAdapter(adapter);
    }
}
