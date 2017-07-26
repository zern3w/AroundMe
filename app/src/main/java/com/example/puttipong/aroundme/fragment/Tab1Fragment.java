package com.example.puttipong.aroundme.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.puttipong.aroundme.R;

public class Tab1Fragment extends Fragment {

    private String searchText;

    public Tab1Fragment() {
        super();
    }

    public static Tab1Fragment newInstance(String searchText) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("SEARCH_TEXT", searchText);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
            searchText = getArguments().getString("SEARCH_TEXT", "");
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);
        initInstances(rootView);
        return rootView;
    }

    private void initInstances(View rootView) {

        Toast.makeText(getContext(),
                searchText,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
    }

    /*
     * Restore Instance State Here
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();


        if (savedInstanceState != null) {
            // Restore Instance State here
        }
    }


}
