package com.example.barbers.barbers;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.barbers.Constants;
import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */

public class BarberHomeFragment extends Fragment {

    //properties
    private Uri resultUri;
    private Uri imageUri;
    private final int CODE_IMG_GALLERY = 1;
    private Button editProfile;
    private Button goToGall;
    private DatabaseReference ref;
    private TextView tv_profile_name;
    private TextView barber_address;
    private TextView barbershopName;
    private FirebaseUser fUser;
    private ImageView ib_barber_profile_picture;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri avatarUri;
    private Uri currentImg;
    private StorageTask mUploadTask;
    private StorageReference fileRef;
    private ImageView camera;
    private TextView description;
    private EditText et_description;
    private FloatingActionButton barbershop_description_fab;
    private Button ghostMode;

    //on create view we init the recycler to the container with our adapter.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_barber_home, container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child(Constants.USERPATH).child(Constants.BARBERPATH).child(fUser.getUid());
        Button schedule = root.findViewById(R.id.btn_schedule_barbers);
        barber_address = root.findViewById(R.id.barber_address);
        ib_barber_profile_picture = root.findViewById(R.id.ib_barber_profile_picture);
        editProfile = root.findViewById(R.id.btn_edit_details);
        description = root.findViewById(R.id.description);
        et_description = root.findViewById(R.id.et_description);
        barbershopName = root.findViewById(R.id.tv_barbershop);
        tv_profile_name = root.findViewById(R.id.tv_profile_name);
        root.findViewById(R.id.btn_logout).setOnClickListener(b ->{
            logOut();
        });

        barbershop_description_fab = root.findViewById(R.id.brbshop_description_fab);
        goToGall = root.findViewById(R.id.b_to_gallery);
        goToGall.setOnClickListener(b->{
            Navigation.findNavController(root).navigate(R.id.action_barberHomeFragment_to_gallerryFragment);
        });


        ghostMode = root.findViewById(R.id.ghost);
        ghostMode.setOnClickListener(b->{
            Bundle args = new Bundle();
            args.putString("id",fUser.getUid());
            Navigation.findNavController(root).
                    navigate(R.id.action_barberHomeFragment_to_searchFragment, args);
        });


        //change barber description
        description.setOnClickListener(v -> change_description(description));
        barbershop_description_fab.setOnClickListener(v -> change_description(barbershop_description_fab));



        schedule.setOnClickListener(b->{
            Navigation.findNavController(root).navigate(R.id.action_barberHomeFragment_to_scheduleForBarberFragment);
        });



        editProfile.findViewById(R.id.btn_edit_details).setOnClickListener(v -> {
            NavController nv = Navigation.findNavController(root);
            nv.navigate(R.id.action_barberHomeFragment_to_detailUpdate);
        });

        ib_barber_profile_picture.setOnClickListener(v -> openFileChooser());

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readFromDB("name");
        readFromDB("address");
        readFromDB("description");
        readFromDB("image");
        readFromDB("barbershop");

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CODE_IMG_GALLERY && data != null) {

            imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);

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

                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
                fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + getFileExtension(resultUri));
                mUploadTask = fileRef.putFile(resultUri)
                        .addOnSuccessListener(uploadSuccessListener)
                        .addOnFailureListener(uploadFailureListener);

            }
        }
    }


    private OnSuccessListener uploadSuccessListener = (OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {


        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("barbers");
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getKey().equals(fUser.getUid())) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("img", uri.toString());
                            ds.getRef().updateChildren(hashMap);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Picasso.get().load(avatarUri).into(ib_barber_profile_picture);
        })

                .addOnFailureListener(this::showError);
    };

    private void showError(Exception e) {
        new AlertDialog.Builder(getContext()).setTitle("An Error Occurred").setMessage(e.getLocalizedMessage()).setPositiveButton("Dismiss", (dialog, which) -> {
        }).show();
    }


    private OnFailureListener uploadFailureListener = this::showError;

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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
                            assert barber != null;
                            if (barber.getAddress().equals("")) {
                                barber_address.setText("enter address");
                            }
                            barber_address.setText(barber.getAddress());
                            break;
                        case "image":
                            currentImg = Uri.parse(barber.getImg());
                            System.out.println(currentImg);
                            Picasso.get().load(currentImg).into(ib_barber_profile_picture);
                            break;
                        case "description":
                            assert barber != null;
                            if (barber.getDescription().equals("")) {
                                String message = barber.getFullName() + " " + getString(R.string.barberDescription);
                                description.setText(message);
                            } else description.setText(barber.getDescription());
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


    private void change_description(View v) {
        String shopdescription = "";


        if (v.getResources().getResourceName(v.getId()).equals(description.getResources().getResourceName(description.getId()))){
//        if (countdes == 0) {
            et_description.setVisibility(View.VISIBLE);
//            et_description.setTextColor(Color.WHITE);
            description.setVisibility(View.INVISIBLE);


            barbershop_description_fab.setImageResource(R.drawable.ic_beenhere_black_24dp);
            barbershop_description_fab.setVisibility(View.VISIBLE);


        } else {
            shopdescription = et_description.getText().toString();
            String finaldes = shopdescription;
            System.out.println(finaldes + "the shop");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Barber brb = dataSnapshot.getValue(Barber.class);
                    assert brb != null;
                    Barber change_shop = new Barber(fUser.getUid(), brb.getFullName(), brb.getPassword(), brb.getPhone(), brb.getEmail(), brb.getUsername(), brb.getAddress(), brb.getBarbershop(), finaldes, brb.getImg(),brb.getGallery(), brb.getQueues(),brb.getPriority());
                    System.out.println("shop was changed");
                    ref.setValue(change_shop);
                    description.setText(finaldes);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            et_description.setVisibility(View.INVISIBLE);
            description.setVisibility(View.VISIBLE);
            description.setText(finaldes);
            barbershop_description_fab.setVisibility(View.INVISIBLE);
        }
    }
    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.logoutt).setPositiveButton(R.string.yes, (dialog, which) -> FirebaseAuth.getInstance().signOut()).setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }


}

