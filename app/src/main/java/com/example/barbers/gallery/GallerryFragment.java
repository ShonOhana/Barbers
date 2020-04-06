package com.example.barbers.gallery;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.barbers.convinience.FirebaseViewHolder;
import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.example.barbers.java.Image;
import com.example.barbers.java.Like;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class GallerryFragment extends Fragment {

    //properties
    private final int CODE_IMG_GALLERY = 1;
    private ImageView img;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FirebaseRecyclerOptions<Image> options;
    private ArrayList<Image> galleryImg = new ArrayList<>();
    private FirebaseRecyclerAdapter<Image, FirebaseViewHolder> adapter;
    private FirebaseUser fUser;
    private ArrayList<Like> likers;
    private Uri currentImg;
    private StorageReference fileRef;
    private TextView tv_profile_name;
    private TextView barber_address;
    private ImageView ib_barber_profile_picture;
    private FirebaseStorage mSorage = FirebaseStorage.getInstance();
    private TextView barbershopName;
    private ArrayList<Image> deletedGallery = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallerry, container, false);

        /**find view's by id's*/
        //local variables
        ImageView camera = view.findViewById(R.id.camera);
        Button editProfile = view.findViewById(R.id.btn_edit_details);
        Button goToDescription = view.findViewById(R.id.b_to_description);
        Button schedule = view.findViewById(R.id.btn_schedule_barbers);
        Button logout = view.findViewById(R.id.btn_logout);
        DatabaseReference reference;
        Query query;
        Button ghostMode = view.findViewById(R.id.ghost);

        //class variables
        barbershopName = view.findViewById(R.id.tv_barbershop);
        recyclerView = view.findViewById(R.id.recycle_for_gallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        barber_address = view.findViewById(R.id.barber_address);
        ib_barber_profile_picture = view.findViewById(R.id.ib_barber_profile_picture);
        tv_profile_name = view.findViewById(R.id.tv_profile_name);
        swipeRefreshLayout = view.findViewById(R.id.swipe);


        /**setOnClickListeners*/
        //TODO: fix that I need to press twice for the gallery
        camera.setOnClickListener(b -> startActivityForResult(new Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*"), CODE_IMG_GALLERY));

        logout.setOnClickListener(b -> {
            logOut();
        });
        ghostMode.setOnClickListener(b -> {
            Bundle args = new Bundle();
            args.putString("id", fUser.getUid());
            Navigation.findNavController(view).
                    navigate(R.id.action_gallerryFragment_to_searchFragment, args);
        });
        goToDescription.setOnClickListener(b -> {
            NavController nv = Navigation.findNavController(view);
            nv.navigate(R.id.action_gallerryFragment_to_barberHomeFragment);
        });
        schedule.setOnClickListener(b -> {
            Navigation.findNavController(view).navigate(R.id.action_gallerryFragment_to_scheduleForBarberFragment);
        });
        editProfile.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_gallerryFragment_to_detailUpdate);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            (new Handler()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }, 1000);
        });


        //navigation: gallery gor barber / client bu args
        Bundle args1 = getArguments();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (args1 == null) {
            query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
            reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
            gerBarberGallery(reference);
        } else {
            query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args1.getString("BarberId")).child("gallery").orderByChild("likes");
            reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args1.getString("BarberId")).child("gallery");
            gerBarberGallery(reference);
        }

        //firebase relationship
        reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
        reference.keepSynced(true);
        deletedGallery = galleryImg;
        options = new FirebaseRecyclerOptions.Builder<Image>().setQuery(query, Image.class).build();


        /**class methods calls*/
        showGallery(query);
        readFromDB("name");
        readFromDB("address");
        readFromDB("image");
        readFromDB("barbershop");


        return view;
    }


    /**
     * created methods
     * */

    private String getFileExtension(Uri uri) {
        if (getActivity() != null) {
            ContentResolver contentResolver = getActivity().getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        }
        return "";
    }

    private void showGallery(Query query) {
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());

        adapter = new FirebaseRecyclerAdapter<Image, FirebaseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FirebaseViewHolder holder, int position, @NonNull Image model) {


                img = holder.itemView.findViewById(R.id.iv_gallery_item);
                ImageView like = holder.itemView.findViewById(R.id.like_Button);
                TextView the_likers = holder.itemView.findViewById(R.id.tv_likers);
                Button deleteImage = holder.itemView.findViewById(R.id.b_delete);
                like.setVisibility(View.INVISIBLE);
                the_likers.setVisibility(View.INVISIBLE);
                deleteImage.setOnClickListener(b -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.q_delete_photo).
                            setPositiveButton("Yes", (dialog, which) -> deleteImage(position))
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();

                });


                if (!model.getUri().equals(""))
                    Picasso.get().load(model.getUri()).into(img);


            }

            @NonNull
            @Override
            public FirebaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FirebaseViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.fragment_gallery_item, parent, false));
            }
        };


        recyclerView.setAdapter(adapter);
    }

    //put the images gallery from database to a list
    private void gerBarberGallery(DatabaseReference ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Image image = ds.getValue(Image.class);

                    galleryImg.add(image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readFromDB(String key) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Barber barber = dataSnapshot.getValue(Barber.class);
                    switch (key) {
                        case "name":
                            tv_profile_name.setText(barber.getFullName());
                            break;
                        case "address":
                            if (barber.getAddress().equals("")) {
                                barber_address.setText("enter address");
                            } else barber_address.setText(barber.getAddress());
                            break;
                        case "image":
                                currentImg = Uri.parse(barber.getImg());
                                Picasso.get().load(currentImg).into(ib_barber_profile_picture);
                            if (barber.getImg().equals(""))
                                Picasso.get().load(R.drawable.barber_img).into(ib_barber_profile_picture);
                            break;
                        case "barbershop":
                            assert barber != null;
                            if (barber.getBarbershop().equals("")) {
                                String message = getString(R.string.enter_shop_name);
                                barbershopName.setText(message);
                            } else barbershopName.setText(barber.getBarbershop());
                            break;
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void deleteImage(int position) {
        if (!galleryImg.get(0).getUri().equals("") ) {


            DatabaseReference test = FirebaseDatabase.getInstance().getReference().
                    child("users").child("barbers").child(fUser.getUid()).
                    child("gallery");


            if (position>= deletedGallery.size()) {
                deletedGallery = galleryImg;
            }

            deletedGallery.remove(position);

            test.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    test.setValue(deletedGallery);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.logoutt).setPositiveButton(R.string.yes, (dialog, which) -> FirebaseAuth.getInstance().signOut()).setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    /**
     * class build methods
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CODE_IMG_GALLERY && data != null) {

            data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();


                if (!currentImg.toString().equals("")) {
                    StorageReference delRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentImg.toString());
                    delRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }


                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child("gallery");
                fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                        child(System.currentTimeMillis() + "." + getFileExtension(resultUri));
                StorageTask mUploadTask = fileRef.putFile(resultUri)
                        .addOnSuccessListener(taskSnapshot -> {


                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {


                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("barbers").child(fUser.getUid());
                                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            likers = new ArrayList<>();
                                            likers.add(new Like("", ""));
                                            Barber barber = dataSnapshot.getValue(Barber.class);
                                            assert barber != null;
                                            if (barber.getGallery() != null) {
                                                galleryImg = barber.getGallery();

                                                if (galleryImg.get(0).getUri().equals(""))
                                                    galleryImg.set(0, new Image(uri.toString(), 0, likers));
                                                else
                                                    galleryImg.add(new Image(uri.toString(), 0, likers));
                                                barber.setGallery(galleryImg);

                                                System.out.println("Gallery image size " + galleryImg.size());
                                                dbRef.setValue(barber);
                                            } else {
                                                ArrayList<Image> galleryImg = new ArrayList<>();
                                                galleryImg.add(new Image("", 0, likers));
                                                barber.setGallery(galleryImg);
                                                dbRef.setValue(barber);
                                            }

                                        }
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            });

                        });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


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
}



