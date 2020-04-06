package com.example.barbers.login.register;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.barbers.queues.Constants;
import com.example.barbers.MainActivity;
import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.example.barbers.java.Client;
import com.example.barbers.java.Image;
import com.example.barbers.java.Like;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.regex.Pattern;

//import androidx.fragment.app.FragmentManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUp extends Fragment  {

    //properties
    private EditText etMail;
    private EditText etPass;
    private EditText etAdress;
    private Spinner etType;
    private EditText etFullName;
    private EditText etPhone;
    private ProgressDialog progressDialog;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayList<Image> galleryImagesArr = new ArrayList<>();
    //properties for the class build methods
    private OnSuccessListener<AuthResult> registerListener = new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            //go to main activity
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            if (getType() != null) registerType(getType()); //take the type in the register form and put it on the database
            if (getActivity() != null) getActivity().finish();

        }
    };
    private OnFailureListener mFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            showError(e);
        }
    };

    //empty constructor
    public SignUp() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_signup, container, false);

        /**find view's by id's*/
        //local variables
        TextView btnCreate = v.findViewById(R.id.btn_sign_up);
        TextView sBack = v.findViewById(R.id.go_to_sin);

        //class variables
        etMail = v.findViewById(R.id.mail);
        etPass = v.findViewById(R.id.pswrd);
        etAdress = v.findViewById(R.id.address);
        etType = v.findViewById(R.id.type);
        etFullName = v.findViewById(R.id.fname);
        etPhone = v.findViewById(R.id.phone);

        //initial for the register
        arrayList.add("queues");
        galleryImagesArr.add(new Image("",0,new ArrayList<Like>()));
        //Spinner
        String[] items = new String[]{getString(R.string.barber), getString(R.string.client)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        etType.setAdapter(adapter);

        /**setOnClickListeners*/
        btnCreate.setOnClickListener(b-> register());
        sBack.setOnClickListener(b->{
            getFragmentManager().beginTransaction().remove(SignUp.this).commitAllowingStateLoss();
        });

        return v;
    }

    /**
     * created methods
     * */

    private void register() {
        String email = getEmailforRegister();
        String pass = getPass();
        String fullName = getFullName();
        String phone = getPhone();
        String type = getType();
        String userAddress = getUserAddress();


        if (email == null || pass == null || fullName == null || phone == null || type == null || userAddress == null ) {
            return;
        }

        showProgress();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(registerListener)
                .addOnFailureListener(mFailureListener);

    }

    //Explain how we put barber or client in the database
    private void registerType(String type) {
        if (type.equals(getString(R.string.barber))) {
            Barber barber = new Barber(FirebaseAuth.getInstance().getCurrentUser().getUid(), getFullName(),getPass(), getPhone(), getEmailforRegister(),"",getUserAddress(),"","","",galleryImagesArr,arrayList ,99999999  );
            String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.USERPATH).child(Constants.BARBERPATH).child(uID);
            ref.setValue(barber);
        }
        else {
            Client client = new Client(FirebaseAuth.getInstance().getCurrentUser().getUid(), getFullName(),getPass(), getPhone(), getEmailforRegister());
            String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.USERPATH).child(Constants.CLIENTPATH).child(uID);
            ref.setValue(client);
        }
    }


    private String getEmailforRegister() {
        String email = etMail.getText().toString();
        Pattern emailAddressPattern = Patterns.EMAIL_ADDRESS;

        boolean validEmail = emailAddressPattern.matcher(email).matches();
        if (!validEmail) {
            etMail.setError("Not valid!");
            return null;
        }

        return email;
    }

    private String getUserAddress() {
        String userAddress = etAdress.getText().toString();

        if (userAddress.length() < 4 ) {
            etAdress.setError("Not valid! must contain at least 4 characters");
            return null;
        }

        return userAddress;
    }


    public String getFullName() {
        String fullName = etFullName.getText().toString();

        if (fullName.length() < 4) {
            etFullName.setError("Too short");
            return null;
        }

        return fullName;
    }

    private String getPhone() {
        String phone = etPhone.getText().toString();
        Pattern phonePattern = Patterns.PHONE;

        boolean validPhone = phonePattern.matcher(phone).matches();
        if (!validPhone) {
            etPhone.setError("Not valid!");
            return null;
        }

        return phone;
    }

    private String getType() {
        String type = etType.getSelectedItem().toString();

        if (type.equalsIgnoreCase(getString(R.string.barber)) || type.equalsIgnoreCase(getString(R.string.client))) {
            return type;
        }else {
            return null;
        }
    }


    private String getPass() {

        String pass = etPass.getText().toString();

        if (pass.length() < 6) {
            etPass.setError("Must at least 6");
            return null;
        }
        return pass;
    }

    private void showError(Exception e) {
        new AlertDialog.Builder(getContext()).setTitle("Error").setMessage(e.getLocalizedMessage())
                .setPositiveButton("Dismiss", (dialog, which) -> {

                }).show();

    }


    private void showProgress(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Logging you in");
            progressDialog.setMessage("please wait...");
        }
        progressDialog.show();
    }


}
