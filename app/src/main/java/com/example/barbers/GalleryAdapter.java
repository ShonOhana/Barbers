package com.example.barbers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    List<GalleryItemFragment>  galleryItemFragmentList;
    Context context;

    public GalleryAdapter(List<GalleryItemFragment> galleryItemFragmentList, Context context) {
        this.galleryItemFragmentList = galleryItemFragmentList;
        this.context = context;
    }

    @NonNull
    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GalleryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_gallery_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.GalleryViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return galleryItemFragmentList.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        ImageView like;
        TextView the_likers;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_gallery_item);
            like = itemView.findViewById(R.id.like_Button);
            the_likers = itemView.findViewById(R.id.tv_likers);

        }
    }
}
