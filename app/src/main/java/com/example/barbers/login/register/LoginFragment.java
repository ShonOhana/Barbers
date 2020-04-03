package com.example.barbers.login.register;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.barbers.MainActivity;
import com.example.barbers.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends DialogFragment {

    private EditText etMail;
    private EditText etPass;
    //property we put in a success listener for login.
    private OnSuccessListener<AuthResult> mSuccessListener = new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            if (progressDialog.isShowing()){ //after the progress bar us showing then dismiss
                progressDialog.dismiss();
            }

            //go to main activity
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
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

    public LoginFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button btnLogin = v.findViewById(R.id.sin1);
        etMail = v.findViewById(R.id.Email);
        etPass = v.findViewById(R.id.pswrd);
        TextView sBack = v.findViewById(R.id.go_to_sup);

        FragmentManager fm = getFragmentManager();
        btnLogin.setOnClickListener(b->{login();});
        sBack.setOnClickListener(b->{
            if (fm!=null) fm.beginTransaction().replace(R.id.cl_login, new SignUp()).commit();
        });



        return v;
    }

    //METHODS
    private ProgressDialog progressDialog; // property for the show progress method
    // that method create new progress bar in use.
    private void showProgress(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Logging you in");
            progressDialog.setMessage("please wait...");
        }
        progressDialog.show();
    }

    private void login(){
        String email = getEmail();
        String pass = getPass();

        if (email == null || email.isEmpty() || pass.isEmpty()){
            return;
        }
            showProgress();
        // make the user put email and password (must)
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(mSuccessListener)
                    .addOnFailureListener(mFailureListener);
    }

    private String getEmail(){
        String email = etMail.getText().toString();

        Pattern emailAddressPattern = Patterns.EMAIL_ADDRESS;
        boolean validEmail = emailAddressPattern.matcher(email).matches();
        if (!validEmail){
            etMail.setError("Please enter email");
            return "";
        }

        return email;
    }

    private String getPass(){

        String pass = etPass.getText().toString();

        if (pass.length()<6){
            etPass.setError("Must at least 6");
        }
        return pass;
    }

    private void showError(Exception e) {
        new AlertDialog.Builder(getContext()).setTitle("Error").setMessage(e.getLocalizedMessage()) //message for auth problem
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }


    //need to check if the user exist in the system / full name is enought
    private String getUserName() {
        String userName = etMail.getText().toString();

        if (userName.length() < 4 ) {
            etMail.setError("Not valid! must contain at least 4 characters");
            return null;
        }

        return userName;
    }

}
