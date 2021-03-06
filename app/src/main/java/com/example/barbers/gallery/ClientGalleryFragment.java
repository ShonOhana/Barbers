package com.example.barbers.gallery;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.barbers.R;
import com.example.barbers.convinience.FirebaseViewHolder;
import com.example.barbers.java.Barber;
import com.example.barbers.java.Client;
import com.example.barbers.java.Image;
import com.example.barbers.java.Like;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClientGalleryFragment extends Fragment {


    private TextView tv_profile_name;
    private TextView barber_address;
    private ImageView ib_barber_profile_picture;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FirebaseRecyclerOptions<Image> options;
    private FirebaseRecyclerAdapter<Image, FirebaseViewHolder> adapter;
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private Bundle args;
    private ArrayList<Like> likers = new ArrayList<>();
    private ArrayList<Image> galleryImg = new ArrayList<>();
    private Button delete;
    private View sargel;
    private Uri currentImg;
    private StorageReference fileRef;
    private String likersText = "";


    private static int PICK_IMAGE_REQUEST = 3;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallerry, container, false);

        /**find view's by id's*/
        //local variables
        DatabaseReference reference;
        ImageView camera = view.findViewById(R.id.camera);
        ImageView ivBack = view.findViewById(R.id.iv_back);
        TextView tvBack = view.findViewById(R.id.tv_back);
        Query query;

        //class variables
        recyclerView = view.findViewById(R.id.recycle_for_gallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        sargel = view.findViewById(R.id.sargel);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        barber_address = view.findViewById(R.id.barber_address);
        barber_address = view.findViewById(R.id.tv_barbershop);
        ib_barber_profile_picture = view.findViewById(R.id.ib_barber_profile_picture);
        tv_profile_name = view.findViewById(R.id.tv_profile_name);


        /**setOnClickListeners*/
        ivBack.setOnClickListener(b->{
            Navigation.findNavController(view).navigate(R.id.action_clientGalleryFragment_to_costumerHomeFragment);
        });
        camera.setOnClickListener(b -> openFileChooser());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            (new Handler()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }, 1000);
        });

        //navigate the gallery kind
        args = getArguments();
        if (args == null) {
            query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
            reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
            getBarberGallery(reference);
        } else {
            query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("BarberId")).child("gallery");
            reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("BarberId")).child("gallery");
            ivBack.setVisibility(View.VISIBLE);
            tvBack.setVisibility(View.VISIBLE);
            camera.setVisibility(View.INVISIBLE);
            getBarberGallery(reference);
        }

        reference.keepSynced(true);
        options = new FirebaseRecyclerOptions.Builder<Image>().setQuery(query, Image.class).build();
        showGallery(reference, fUser);


        return view;
    }


    private void openFileChooser() {
        PICK_IMAGE_REQUEST = 1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST
                && data != null && data.getData() != null) {

            Uri avatarUri = data.getData();


            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child("gallery");
            fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + getFileExtension(avatarUri));

            StorageTask mUploadTask = fileRef.putFile(avatarUri)
                    .addOnSuccessListener(taskSnapshot -> {

                        String gallery_img = fileRef.getDownloadUrl().toString();

                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("barbers").child(fUser.getUid());
                            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        likers = new ArrayList<>();
                                        likers.add(new Like("", ""));
                                        Barber barber = dataSnapshot.getValue(Barber.class);
                                        if (barber != null) {
                                            if (barber.getGallery() != null) {
                                                galleryImg = barber.getGallery();

                                                if (galleryImg.get(0).getUri().equals("")) {
                                                    galleryImg.set(0, new Image(uri.toString(), 0, likers));
                                                } else
                                                    galleryImg.add(new Image(uri.toString(), 0, likers));
                                                barber.setGallery(galleryImg);

                                                dbRef.setValue(barber);
                                            }
//

                                        }
                                    }
                                }


                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        })

                                .addOnFailureListener(this::showError);
                    })
                    .addOnFailureListener(uploadFailureListener);
        }
    }


    private void showError(Exception e) {
        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.error))
                .setMessage(e.getLocalizedMessage()).
                setPositiveButton((getString(R.string.dismiss)), (dialog, which) -> {
        }).show();
    }


    private OnFailureListener uploadFailureListener = this::showError;

    private String getFileExtension(Uri uri) {
        if (getActivity() != null) {
            ContentResolver contentResolver = getActivity().getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        }
        return "";
    }

    private void showGallery(DatabaseReference ref, FirebaseUser clientUser) {

        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child("users").child("clients").child(clientUser.getUid());

        adapter = new FirebaseRecyclerAdapter<Image, FirebaseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FirebaseViewHolder holder, int position, @NonNull Image model) {

                Image thisImg = galleryImg.get(position);

                ImageView img = holder.itemView.findViewById(R.id.iv_gallery_item);
                ImageView like = holder.itemView.findViewById(R.id.like_Button);
                TextView the_likers = holder.itemView.findViewById(R.id.tv_likers);
                delete = holder.itemView.findViewById(R.id.b_delete);

                args = getArguments();
                if (args.getString("BarberId") != null) {
                    delete.setVisibility(View.INVISIBLE);
                    sargel.setVisibility(View.INVISIBLE);

                }




                int likes = thisImg.getLikes();

                if (thisImg.getLikers() != null) {
                    likers = thisImg.getLikers();
                    System.out.println(likers + " LIKERS");
                    System.out.println(thisImg + "  THISIMG");

                    thisImg.setLikers(likers);


                    if (likers.size() > 1) {
                        likersText = likers.get(model.getLikers().size() - 1).getName() + " and " + (model.getLikes() - 1) + " others";
                        System.out.println(likers.get(likers.size() - 1).getName());
                    } else {
                        if (likers.get(0).getFuserID().equals("")) likersText = "";
                        else likersText = likers.get(0).getName();
                    }


                    System.out.println(likersText);


                    if (!model.getUri().equals(""))
                        Picasso.get().load(model.getUri()).into(img);

                    for (int i = 0; i < model.getLikers().size(); i++) {
                        if (thisImg.getLikers().get(i).getFuserID().equals(clientUser.getUid())) {
                            like.setImageResource(R.drawable.barber_img);
                        }
                    }


                    if (args != null) {
                        like.setOnClickListener(b -> {
                            Image likeImg = model;
                            likers = likeImg.getLikers();


                            clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    int idxToRemove = -1;


                                    if (dataSnapshot.exists()) {
                                        Client client = dataSnapshot.getValue(Client.class);
                                        for (int j = 0; j < likers.size(); j++) {
                                            if (model.getLikers().get(j).getFuserID().equals(client.getClientID())) {
                                                idxToRemove = j;
                                            }
                                        }
                                        if (idxToRemove != -1) {
                                            if (likers.size() == 1) {
                                                likers.set(0, new Like("", ""));
                                                likeImg.setLikes(0);
                                                likersText = "";
                                            } else {
                                                likers.remove(idxToRemove);
                                                likeImg.setLikes(likers.size());
                                            }

                                        } else {
                                            if (likers.get(0).getFuserID().equals("")) {
                                                likers.set(0, new Like(client.getClientID(), client.getFullName()));
                                                likersText = likers.get(model.getLikers().size() - 1).getName();
                                            } else {
                                                likers.add(new Like(client.getClientID(), client.getFullName()));
                                                likersText = likers.get(model.getLikers().size() - 1).getName() + " and " + (model.getLikes() - 1) + " others";
                                            }

                                            likeImg.setLikes(likers.size());
                                        }

                                        likeImg.setLikers(likers);

                                        galleryImg.set(position, likeImg);

                                        ref.setValue(galleryImg);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        });
                        the_likers.setText(likersText);
                        the_likers.setTextColor(Color.BLUE);

                    }


                }
//                }

            }

            @NonNull
            @Override
            public FirebaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FirebaseViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.fragment_gallery_item, parent, false));
            }
        };


        recyclerView.setAdapter(adapter);
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

    private void getBarberGallery(DatabaseReference ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Image img = ds.getValue(Image.class);

                    galleryImg.add(img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}