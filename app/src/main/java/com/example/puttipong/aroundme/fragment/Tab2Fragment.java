package com.example.puttipong.aroundme.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.puttipong.aroundme.R;

public class Tab2Fragment extends Fragment {
    public Tab2Fragment() {
        super();
    }

    public static Tab2Fragment newInstance(Bundle bundle) {
        Tab2Fragment fragment = new Tab2Fragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);
        initInstances(rootView);
        return rootView;
    }

    private void initInstances(View rootView) {

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
