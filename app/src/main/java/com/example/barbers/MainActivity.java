package com.example.barbers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.barbers.java.Image;
import com.example.barbers.java.Like;
import com.example.barbers.login.register.LoginActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {





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












    //if we dont have a user ->go Login
    private FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    };

    // that say that if we are logged in go to mainActivity, else go to LoginActivity!!!!
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




    }


}



