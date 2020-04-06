package com.example.barbers.gallery;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbers.queues.Constants;
import com.example.barbers.convinience.FirebaseViewHolder;
import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryItemFragment extends Fragment {

    private FirebaseRecyclerOptions<Barber> options;
    private FirebaseRecyclerAdapter<Barber, FirebaseViewHolder> adapter;

    public GalleryItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_gallery_item, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.recycle_for_gallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USERPATH).child(Constants.BARBERPATH);
        reference.keepSynced(true);
        options = new FirebaseRecyclerOptions.Builder<Barber>().setQuery(reference, Barber.class).build(); //that line build all the barbers we have in our database ref "barbers"

        adapter = new FirebaseRecyclerAdapter<Barber, FirebaseViewHolder>(options) { // that inflate our fireBase view holder to the recycle by the barber model
            @Override
            protected void onBindViewHolder(@NonNull FirebaseViewHolder holder, int position, @NonNull Barber model) {

                ImageView gallery = holder.itemView.findViewById(R.id.iv_gallery_item);
                Picasso.get().load(model.getImg()).into(gallery);

            }

            @NonNull
            @Override
            public FirebaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //inflate the database to the xml file
                return new FirebaseViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.fragment_barber_for_clients_recyle, parent, false));
            }
        };


        recyclerView.setAdapter(adapter);

        return v;
    }

}
