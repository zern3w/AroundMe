package com.example.puttipong.aroundme.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.dao.Results;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private Context context;
    private List<Results> resultsList;

    private static final String TAG = "VillageAdapter";

    public PlaceAdapter(List<Results> resultsList, Context context) {
        this.resultsList = resultsList;
        this.context = context;
    }

    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_place_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        Results results = resultsList.get(position);

        Typeface robotoR = Typeface.createFromAsset(viewHolder.tvName.getContext().getAssets(),
                "fonts/Roboto-Light.ttf");
        viewHolder.tvName.setTypeface(robotoR);
        viewHolder.tvName.setText(results.getName());

        Typeface roboto = Typeface.createFromAsset(viewHolder.tvName.getContext().getAssets(),
                "fonts/Roboto-Regular.ttf");
        viewHolder.tvAddress.setText(results.getVicinity());
        viewHolder.tvAddress.setTypeface(roboto);

        try {
            Picasso.with(this.context)
                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                            + results.getPhotos().get(0).getPhotoReference()
                            + "&key=AIzaSyCZ1BCe4Q7YL1nCa_ovtet4Bjn52tT20T8")
                    .fit()
                    .into(viewHolder.imgPlace);
        } catch (Exception e){
            Picasso.with(this.context)
                    .load(R.drawable.nophoto)
                    .fit()
                    .into(viewHolder.imgPlace);
        }
    }

    @Override
    public int getItemCount() {
        return resultsList == null ? 0
                : resultsList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPlace;
        private TextView tvAddress, tvName;

        public ViewHolder(View view) {
            super(view);

            tvName = (TextView) view.findViewById(R.id.tvName);
            tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            imgPlace = (ImageView) view.findViewById(R.id.imgPlace);
        }

    }
}