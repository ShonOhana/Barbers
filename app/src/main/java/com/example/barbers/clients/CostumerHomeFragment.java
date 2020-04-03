package com.example.barbers.clients;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.barbers.FirebaseViewHolder;
import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.example.barbers.java.Client;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CostumerHomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ArrayList<Barber> barberArrayList;
    private FirebaseRecyclerOptions<Barber> options;
    private FirebaseRecyclerAdapter<Barber, FirebaseViewHolder> adapter;
    private ClientHomeAdapter adapterQ;
    private DatabaseReference reference;
    ValueEventListener valueEventListener;
    FirebaseUser fUser;
    Bundle args;
    TextView tv_profile_name;
    private ImageView waze;
    private ImageView whatsapp;


    private Uri avatarUri;
    private Uri currentImg;

    Query query ;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                barberArrayList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Barber barber = snapshot.getValue(Barber.class);
                        barberArrayList.add(barber);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.notifyAll();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        View v = inflater.inflate(R.layout.fragment_costumer_home, container, false);

        swipeRefreshLayout = v.findViewById(R.id.swipe);


        v.findViewById(R.id.btn_logout).setOnClickListener(b ->{
            logOut();
        });



        recyclerView = v.findViewById(R.id.recycler_view_client_home);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        barberArrayList = new ArrayList<>();
        adapterQ = new ClientHomeAdapter(barberArrayList,inflater);
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageView search = v.findViewById(R.id.fab_search);
        EditText etSearch = v.findViewById(R.id.et_search);
        Bundle args = new Bundle();

        search.setOnClickListener(b->{
            if (etSearch.getText().equals("")){
                search.setEnabled(false);
            }else {
                args.putString("search", etSearch.getText().toString());
                Navigation.findNavController(v).navigate(R.id.action_costumerHomeFragment_to_searchFragment, args);
            }
        });
        query= FirebaseDatabase.getInstance().getReference().child("users").child("barbers");
        reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers");
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference.keepSynced(true);
        options = new FirebaseRecyclerOptions.Builder<Barber>().setQuery(query, Barber.class).build();
        showAllBarbers(v, reference);



        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_profile_name = view.findViewById(R.id.tv_profile_name);
        readFromDB();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            (new Handler()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            },2000);
        });
    }

    private void readFromDB() {

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Client client = dataSnapshot.getValue(Client.class);

                    tv_profile_name.setText(client.getFullName());



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void showAllBarbers(View v, DatabaseReference reference) {
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());

        adapter = new FirebaseRecyclerAdapter<Barber, FirebaseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FirebaseViewHolder holder, int position, @NonNull Barber model) {

                if (reference != null) {

                    TextView profileName = holder.itemView.findViewById(R.id.tv_profile_name);
                    TextView barberAdress = holder.itemView.findViewById(R.id.barber_address);
                    TextView barberShopTitle = holder.itemView.findViewById(R.id.tv_barbershop);
                    ImageView description = holder.itemView.findViewById(R.id.description);
                    TextView gallery = holder.itemView.findViewById(R.id.tv_gallery);
                    Button btn_schedule = holder.itemView.findViewById(R.id.b_schedule);

                    ImageView photo1 = holder.itemView.findViewById(R.id.photo1);
                    ImageView photo2 = holder.itemView.findViewById(R.id.photo2);
                    ImageView photo3 = holder.itemView.findViewById(R.id.photo3);
                    ImageView photo4 = holder.itemView.findViewById(R.id.photo4);


//                    int gallerySize = model.getGallery().size();
//
//                    System.out.println(gallerySize);
//                    System.out.println(model.getFullName());
//                    switch (gallerySize){
//                        case 0:
//                            break;
//                        case 1:
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo1);
//                            break;
//                        case 2:
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-2).getUri())).into(photo1);
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo2);
//                            break;
//                        case 3:
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-3).getUri())).into(photo1);
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-2).getUri())).into(photo2);
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo3);
//                            break;
//                        default:
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-4).getUri())).into(photo1);
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-3).getUri())).into(photo2);
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-2).getUri())).into(photo3);
//                            Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo4);
//                            break;
//                    }



                    args = new Bundle();
                    gallery.setOnClickListener(b->{
                        args.putString("BarberId",model.getBarberID());
                        Navigation.findNavController(v).navigate(R.id.action_costumerHomeFragment_to_clientGalleryFragment,args);
                    });

                    whatsapp = holder.itemView.findViewById(R.id.whatsapp);
                    waze = holder.itemView.findViewById(R.id.waze);
                    whatsapp.setOnClickListener(b -> {

                        Uri uri = Uri.parse("smsto:" + model.getPhone());
                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                        intent.setPackage("com.whatsapp");
                        if (uri != null) startActivity(Intent.createChooser(intent, ""));


                    });
                    waze.setOnClickListener(b -> {
                        try {
                            String url = "https://waze.com/ul?q=" + model.getAddress();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                            startActivity(intent);
                        }
                    });

                    barberAdress.setOnClickListener(b->{
                        try {
                            String url = "https://waze.com/ul?q=" + model.getAddress();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                            startActivity(intent);
                        }
                    });


                    profileName.setText(model.getFullName());
                    barberAdress.setText(model.getAddress());
                    barberShopTitle.setText(model.getBarbershop());
                    description.setOnClickListener(b->{
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        String barberShopForTitle;

                        if(model.getBarbershop().equals("")) barberShopForTitle = model.getFullName() + " " + "Barber shop";
                        else barberShopForTitle = model.getBarbershop();

                        String descriptionMSG;

                        if(model.getDescription().equals("")) descriptionMSG = getString(R.string.locatedIn) + model.getAddress() + " " + getString(R.string.happy);
                        else descriptionMSG = model.getDescription();

                        builder.setTitle(barberShopForTitle).setMessage(descriptionMSG).setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    });

                    btn_schedule.setOnClickListener(b-> {
                        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());
                        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Client client = dataSnapshot.getValue(Client.class);
                                    args = new Bundle();
                                    args.putString("userName", client.getFullName());
                                    args.putString("barberId", model.getBarberID());
                                    Navigation.findNavController(v).navigate(R.id.action_costumerHomeFragment_to_queuesFragment, args);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    });

                }
            }

            @NonNull
            @Override
            public FirebaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FirebaseViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.fragment_barber_for_clients_recyle, parent, false));
            }
        };


        recyclerView.setAdapter(adapter);
    }


    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.logoutt).setPositiveButton(R.string.yes, (dialog, which) -> FirebaseAuth.getInstance().signOut()).setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}




