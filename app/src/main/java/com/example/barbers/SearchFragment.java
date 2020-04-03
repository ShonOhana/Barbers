package com.example.barbers;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbers.clients.ClientHomeAdapter;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {


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
    TextView tv_not_found;
    Button back_to_costumer;
    private ImageView waze;
    private ImageView whatsapp;


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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ValueEventListener checkIfExists = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){

                    tv_not_found.setVisibility(View.VISIBLE);
                    String text = getArguments().getString("search")+ " was not found \n go back to search \n and try again";
                    tv_not_found.setText(text);
                    back_to_costumer.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = v.findViewById(R.id.recycler_view_client_home);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        barberArrayList = new ArrayList<>();
        adapterQ = new ClientHomeAdapter(barberArrayList,inflater);
        fUser = FirebaseAuth.getInstance().getCurrentUser();


        tv_not_found = v.findViewById(R.id.tv_not_found);
        back_to_costumer = v.findViewById(R.id.btn_back_to_costumer);
        Bundle args = getArguments();

        if(args != null){
            if (args.getString("search") != null){
                query= FirebaseDatabase.getInstance().getReference().child("users").child("barbers").orderByChild("fullName").equalTo(args.getString("search"));
                query.addListenerForSingleValueEvent(checkIfExists);
                back_to_costumer.setOnClickListener(b->{
                    Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_costumerHomeFragment);
                });
            }else if (args.getString("id") != null){
                query= FirebaseDatabase.getInstance().getReference().child("users").child("barbers").orderByChild("barberID").startAt(args.getString("id"));
                Button back = v.findViewById(R.id.back_to_Barber);
                back.setOnClickListener(b->{
                    Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_barberHomeFragment);
                });
            }
        }


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

    }

    private void readFromDB() {

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Client client = dataSnapshot.getValue(Client.class);

//                    tv_profile_name.setText(client.getFullName());



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

//



    @Override
    public void onStart() {
        super.onStart();

        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
//        adapter.stopListening();
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
                    TextView info = holder.itemView.findViewById(R.id.tv_info);

                    Button btn_schedule = holder.itemView.findViewById(R.id.b_schedule);
                    ImageView ib_barber_profile = holder.itemView.findViewById(R.id.ib_barber_profile_picture);


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
                    if (!model.getImg().equals("")){

                        Uri currentImg = Uri.parse(model.getImg());
                        Picasso.get().load(currentImg).into(ib_barber_profile);

                    }else {
                        ib_barber_profile.setImageResource(R.drawable.barber_img);
                    }
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
                                    Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_queuesFragment, args);
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

//

    }



}
