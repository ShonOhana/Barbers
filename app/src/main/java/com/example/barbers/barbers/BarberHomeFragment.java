package com.example.barbers.barbers;


import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.example.barbers.queues.Constants;
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
    private final int CODE_IMG_GALLERY;
    private DatabaseReference ref;
    private TextView tv_profile_name;
    private TextView barber_address;
    private TextView barbershopName;
    private FirebaseUser fUser;
    private ImageView ib_barber_profile_picture;
    private Uri avatarUri;
    private Uri currentImg;
    private StorageTask mUploadTask;
    private StorageReference fileRef;
    private TextView description;
    private EditText et_description;
    private FloatingActionButton barbershop_description_fab;

    //empty constructor
    public BarberHomeFragment() {
        CODE_IMG_GALLERY = 1;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_barber_home, container, false);

        /**find view's by id's*/
        //local variables
        Button schedule = root.findViewById(R.id.btn_schedule_barbers);
        Button logout = root.findViewById(R.id.btn_logout);
        Button editProfile = root.findViewById(R.id.btn_edit_details);
        Button ghostMode = root.findViewById(R.id.ghost);
        Button goToGall = root.findViewById(R.id.b_to_gallery);

        //class variables
        barber_address = root.findViewById(R.id.barber_address);
        ib_barber_profile_picture = root.findViewById(R.id.ib_barber_profile_picture);
        description = root.findViewById(R.id.description);
        et_description = root.findViewById(R.id.et_description);
        barbershopName = root.findViewById(R.id.tv_barbershop);
        tv_profile_name = root.findViewById(R.id.tv_profile_name);
        barbershop_description_fab = root.findViewById(R.id.brbshop_description_fab);

        //FireBase relationship
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child(Constants.USERPATH).child(Constants.BARBERPATH).child(fUser.getUid());

        /**setOnClickListeners*/
        //logout
        logout.setOnClickListener(b ->{
            logOut();
        });
        //go to barber gallery fragment
        goToGall.setOnClickListener(b->{
            Navigation.findNavController(root).navigate(R.id.action_barberHomeFragment_to_gallerryFragment);
        });
        //go to the search fragment when the current barber is first (client mode)
        ghostMode.setOnClickListener(b->{
            Bundle args = new Bundle();
            args.putString("id",fUser.getUid());
            args.putString("idName",tv_profile_name.getText().toString());
            Navigation.findNavController(root).
                    navigate(R.id.action_barberHomeFragment_to_searchFragment, args);
        });
        //change barber description
        description.setOnClickListener(v -> change_description(description));
        barbershop_description_fab.setOnClickListener(v -> change_description(barbershop_description_fab));
        //see barbers queues
        schedule.setOnClickListener(b->{
            Navigation.findNavController(root).navigate(R.id.action_barberHomeFragment_to_scheduleForBarberFragment);
        });
        //go to edit profile fragment
        editProfile.setOnClickListener(v -> {
            NavController nv = Navigation.findNavController(root);
            nv.navigate(R.id.action_barberHomeFragment_to_detailUpdate);
        });
        //change barbers profile picture
        ib_barber_profile_picture.setOnClickListener(v -> openFileChooser());

        /**class methods calls*/
        readFromDB("name");
        readFromDB("address");
        readFromDB("description");
        readFromDB("barbershop");
        readFromDB("image");

        return root;
    }

    /**
     * created methods
     * */

    //open the gallery in the phone
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CODE_IMG_GALLERY);

    }

    //show error
    private void showError(Exception e) {
        new AlertDialog.Builder(getContext()).setTitle((getString(R.string.error)))
                .setMessage(e.getLocalizedMessage()).setPositiveButton((getString(R.string.dismiss)),
                (dialog, which) -> {
                }).show();
    }

    //read the data from firebase database
    private void readFromDB(String key) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
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
                                barber_address.setText(R.string.enter_address);
                            }
                            barber_address.setText(barber.getAddress());
                            break;
                        case "image":
                            currentImg = Uri.parse(barber.getImg());
                            Picasso.get().load(currentImg).into(ib_barber_profile_picture);
                            if (barber.getImg().equals(""))
                                Picasso.get().load(R.drawable.barber_img).into(ib_barber_profile_picture);
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
                                String message = (getString(R.string.enter_shop_name));
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

    //change the barbershop description
    private void change_description(View v) {
        String shopdescription = "";


        if (v.getResources().getResourceName(v.getId()).equals(description.getResources().getResourceName(description.getId()))){
            et_description.setVisibility(View.VISIBLE);
            description.setVisibility(View.INVISIBLE);

            barbershop_description_fab.setImageResource(R.drawable.ic_beenhere_black_24dp);
            barbershop_description_fab.setVisibility(View.VISIBLE);


        } else {
            shopdescription = et_description.getText().toString();
            String finaldes = shopdescription;
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Barber brb = dataSnapshot.getValue(Barber.class);
                    assert brb != null;
                    Barber change_shop = new Barber(fUser.getUid(), brb.getFullName(), brb.getPassword(), brb.getPhone(), brb.getEmail(), brb.getUsername(), brb.getAddress(), brb.getBarbershop(), finaldes, brb.getImg(),brb.getGallery(), brb.getQueues(),brb.getPriority());
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

    //logout
    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.logoutt).setPositiveButton(R.string.yes, (dialog, which)
                -> FirebaseAuth.getInstance().signOut()).setNegativeButton(R.string.no,
                (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    //get the photo type
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    /**
     * class build methods
     * */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                //properties
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

                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
                fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + getFileExtension(resultUri));
                mUploadTask = fileRef.putFile(resultUri)
                        .addOnSuccessListener(uploadSuccessListener)
                        .addOnFailureListener(uploadFailureListener);

            }
        }
    }

    //properties for the class build methods
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
    private OnFailureListener uploadFailureListener = this::showError;


}

