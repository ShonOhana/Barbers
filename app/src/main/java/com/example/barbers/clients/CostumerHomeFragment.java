package com.example.barbers.clients;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.barbers.convinience.FirebaseViewHolder;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class CostumerHomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<Barber> barberArrayList;
    private FirebaseRecyclerOptions<Barber> options;
    private FirebaseRecyclerAdapter<Barber, FirebaseViewHolder> adapter;
    private FirebaseUser fUser;
    private Bundle args;
    private TextView tv_profile_name;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_costumer_home, container, false);

        /**find view's by id's*/
        //local variables
        Button search = v.findViewById(R.id.fab_search);
        EditText etSearch = v.findViewById(R.id.et_search);
        TextView logout = v.findViewById(R.id.btn_logout);
        Bundle args = new Bundle();

        //class variables
        swipeRefreshLayout = v.findViewById(R.id.swipe);
        recyclerView = v.findViewById(R.id.recycler_view_client_home);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        barberArrayList = new ArrayList<>();
        tv_profile_name = v.findViewById(R.id.tv_profile_name);
//        ClientHomeAdapter adapterQ = new ClientHomeAdapter(barberArrayList, inflater);

        //firebase relationship
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers");
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference.keepSynced(true);
        options = new FirebaseRecyclerOptions.Builder<Barber>().setQuery(query, Barber.class).build();


        /**setOnClickListeners*/
        logout.setOnClickListener(b -> {
            logOut();
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search.setOnClickListener(b -> {
            if (!etSearch.getText().equals("") ) {
                search.setEnabled(true);
                args.putString("search", etSearch.getText().toString());
                Navigation.findNavController(v).navigate(R.id.action_costumerHomeFragment_to_searchFragment, args);
                InputMethodManager inputMethodManager = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
                assert inputMethodManager != null;
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(Objects.requireNonNull(getActivity()).getCurrentFocus()).getWindowToken(), 0);
            }

        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            (new Handler()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }, 1000);
        });



        /**class methods calls*/
        showAllBarbers(v, reference);
        readFromDB();

        return v;

    }

    /**
     * class build methods
     * */
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /**
     * created methods
     * */

    //the adapter of all the barbers from the database
    private void showAllBarbers(View v, DatabaseReference reference) {
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());

        adapter = new FirebaseRecyclerAdapter<Barber, FirebaseViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull FirebaseViewHolder holder, int position, @NonNull Barber model) {

                if (reference != null) {

                    /**find view's by id's*/
                    TextView profileName = holder.itemView.findViewById(R.id.tv_profile_name);
                    TextView barberAdress = holder.itemView.findViewById(R.id.barber_address);
                    TextView barberShopTitle = holder.itemView.findViewById(R.id.tv_barbershop);
                    ImageView description = holder.itemView.findViewById(R.id.description);
                    TextView info = holder.itemView.findViewById(R.id.tv_info);
                    TextView gallery = holder.itemView.findViewById(R.id.tv_gallery);
                    ImageView VAGallery = holder.itemView.findViewById(R.id.va_gallery);
                    Button btn_schedule = holder.itemView.findViewById(R.id.b_schedule);
                    ImageView ib_barber_profile = holder.itemView.findViewById(R.id.ib_barber_profile_picture);
                    ImageView whatsapp = holder.itemView.findViewById(R.id.whatsapp);
                    ImageView waze = holder.itemView.findViewById(R.id.waze);
                    ImageView photo1 = holder.itemView.findViewById(R.id.photo1);
                    ImageView photo2 = holder.itemView.findViewById(R.id.photo2);
                    ImageView photo3 = holder.itemView.findViewById(R.id.photo3);
                    ImageView photo4 = holder.itemView.findViewById(R.id.photo4);
                    //initialize
                    args = new Bundle();

                    int gallerySize = model.getGallery().size();

                        switch (gallerySize) {
                            case 0:
                                break;
                            case 1:
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo1);
                                break;
                            case 2:
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo1);
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-2).getUri())).into(photo2);
                                break;
                            case 3:
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo1);
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-2).getUri())).into(photo2);
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-3).getUri())).into(photo3);
                                break;
                            default:
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-1).getUri())).into(photo1);
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-2).getUri())).into(photo2);
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-3).getUri())).into(photo3);
                                Picasso.get().load(Uri.parse(model.getGallery().get(gallerySize-4).getUri())).into(photo4);
                                break;
                        }

                    /**setOnClickListeners*/
                    gallery.setOnClickListener(b -> {
                        args.putString("BarberId", model.getBarberID());
                        Navigation.findNavController(v).navigate(R.id.action_costumerHomeFragment_to_clientGalleryFragment, args);
                    });
                    VAGallery.setOnClickListener(b -> {
                        args.putString("BarberId", model.getBarberID());
                        Navigation.findNavController(v).navigate(R.id.action_costumerHomeFragment_to_clientGalleryFragment, args);
                    });
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
                    barberAdress.setOnClickListener(b -> {
                        try {
                            String url = "https://waze.com/ul?q=" + model.getAddress();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                            startActivity(intent);
                        }
                    });
                    profileName.setOnClickListener(b -> {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + model.getPhone()));
                        startActivity(intent);
                    });
                    barberShopTitle.setOnClickListener(b -> {
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, model.getBarbershop());
                        startActivity(intent);
                    });
                    description.setOnClickListener(b -> {
                        desInfo(model);
                    });
                    info.setOnClickListener(b -> {
                        desInfo(model);
                    });
                    btn_schedule.setOnClickListener(b -> {
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


                    //initialize the barbers details from database
                    profileName.setText(model.getFullName() + " - " + model.getPhone());
                    barberAdress.setText(model.getAddress());
                    barberShopTitle.setText(model.getBarbershop());
                    if (!model.getImg().equals("")) {

                        Uri currentImg = Uri.parse(model.getImg());
                        Picasso.get().load(currentImg).into(ib_barber_profile);

                    } else {
                        ib_barber_profile.setImageResource(R.drawable.barber_img);
                    }

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

    private void desInfo(Barber model){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String barberShopForTitle;

        if (model.getBarbershop().equals(""))
            barberShopForTitle = model.getFullName() + " " + "Barber shop";
        else barberShopForTitle = model.getBarbershop();

        String descriptionMSG;

        if (model.getDescription().equals(""))
            descriptionMSG = getString(R.string.locatedIn) + model.getAddress() + " " + getString(R.string.happy);
        else descriptionMSG = model.getDescription();

        builder.setTitle(barberShopForTitle).setMessage(descriptionMSG).setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.logoutt).setPositiveButton(R.string.yes, (dialog, which) -> FirebaseAuth.getInstance().signOut()).setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}




