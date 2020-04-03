package com.example.barbers;


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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class GallerryFragment extends Fragment {

    private Uri resultUri;
    private Uri imageUri;
    private final int CODE_IMG_GALLERY = 1;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ImageView img;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FirebaseRecyclerOptions<Image> options;
    private ArrayList<Image> galleryImg = new ArrayList<>();
    private FirebaseRecyclerAdapter<Image, FirebaseViewHolder> adapter;
    private DatabaseReference reference;
    private FirebaseUser fUser;
    private Query query;
    private boolean isClient;
    private ImageView galleryItem;
    private ArrayList<Like> likers;
    private Bundle args;
    private Uri currentImg;
    private StorageReference fileRef;
    private static int PICK_IMAGE_REQUEST = 3;
    private DatabaseReference ref;
    private TextView tv_profile_name;
    private TextView barber_address;
    private ImageView ib_barber_profile_picture;
    private FirebaseStorage mSorage = FirebaseStorage.getInstance();
    private TextView barbershopName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallerry, container, false);


        barbershopName = view.findViewById(R.id.tv_barbershop);
        recyclerView = view.findViewById(R.id.recycle_for_gallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers");
        galleryItem = view.findViewById(R.id.iv_gallery_item);
        ImageView camera = view.findViewById(R.id.camera);
        barber_address = view.findViewById(R.id.barber_address);
        ib_barber_profile_picture = view.findViewById(R.id.ib_barber_profile_picture);
        tv_profile_name = view.findViewById(R.id.tv_profile_name);
        Button editProfile = view.findViewById(R.id.btn_edit_details);
        Button goToDescription = view.findViewById(R.id.b_to_description);
        Button schedule = view.findViewById(R.id.btn_schedule_barbers);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        camera.setOnClickListener(b -> startActivityForResult(new Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*"), CODE_IMG_GALLERY));


        fUser = FirebaseAuth.getInstance().getCurrentUser();
        args = getArguments();
        if (args == null) {
            query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
            reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
            gerBarberGallery(reference);
        } else {
            query = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("BarberId")).child("gallery");
            reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("BarberId")).child("gallery");
            isClient = true;
            gerBarberGallery(reference);
        }
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid()).child("gallery");
        reference.keepSynced(true);
        options = new FirebaseRecyclerOptions.Builder<Image>().setQuery(query, Image.class).build();


        Button ghostMode = view.findViewById(R.id.ghost);
        ghostMode.setOnClickListener(b->{
            Bundle args = new Bundle();
            args.putString("id",fUser.getUid());
            Navigation.findNavController(view).
                    navigate(R.id.action_gallerryFragment_to_searchFragment, args);
        });

        showGallery(reference);

        goToDescription.setOnClickListener(b -> {
            NavController nv = Navigation.findNavController(view);
            nv.navigate(R.id.action_gallerryFragment_to_barberHomeFragment);
        });
        schedule.setOnClickListener(b -> {
            Navigation.findNavController(view).navigate(R.id.action_gallerryFragment_to_scheduleForBarberFragment);
        });

        readFromDB("name");
        readFromDB("address");
        readFromDB("image");
        readFromDB("barbershop");

        editProfile.findViewById(R.id.btn_edit_details).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_gallerryFragment_to_detailUpdate);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            (new Handler()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }, 4000);
        });
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CODE_IMG_GALLERY && data != null) {

            imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getContext(),this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();



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
                        .addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {



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



    private void openFileChooser() {
        PICK_IMAGE_REQUEST = 1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }




    private void showError(Exception e) {
        new AlertDialog.Builder(getContext()).setTitle("An Error Occurred").setMessage(e.getLocalizedMessage()).setPositiveButton("Dismiss", (dialog, which) -> {
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

    public void showGallery(DatabaseReference ref) {
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());

        adapter = new FirebaseRecyclerAdapter<Image, FirebaseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FirebaseViewHolder holder, int position, @NonNull Image model) {


                if (reference != null) {

                    img = holder.itemView.findViewById(R.id.iv_gallery_item);
                    ImageView like = holder.itemView.findViewById(R.id.like_Button);
                    TextView the_likers = holder.itemView.findViewById(R.id.tv_likers);
                    Button deleteImage = holder.itemView.findViewById(R.id.b_delete);
                    like.setVisibility(View.INVISIBLE);
                    the_likers.setVisibility(View.INVISIBLE);
                    deleteImage.setOnClickListener(b -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Are you sure you want to delete this photo?").
                                setPositiveButton("Yes", (dialog, which) -> deleteImage(position))
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();

                    });


                    if (!model.getUri().equals(""))
                        Picasso.get().load(model.getUri()).into(img);

                }

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

    public void readFromDB(String key) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid());
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
                            System.out.println(currentImg);
                            Picasso.get().load(currentImg).into(ib_barber_profile_picture);
                            break;
                        case "barbershop":
                            assert barber != null;
                            if (barber.getBarbershop().equals("")) {
                                String message = "Please enter your barber shop name.";
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

    private void deleteImage(int position){
        StorageReference delRef = mSorage.getReferenceFromUrl(galleryImg.get(position).getUri());
        delRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }


        });


        DatabaseReference test = FirebaseDatabase.getInstance().getReference().
                child("users").child("barbers").child(fUser.getUid()).
                child("gallery").child(Integer.toString(position));

        test.removeValue();
    }
}



