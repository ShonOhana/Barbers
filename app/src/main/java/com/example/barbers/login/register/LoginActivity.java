package com.example.barbers.login.register;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barbers.R;

public class LoginActivity extends AppCompatActivity {

    private TextView signIn;
    private TextView signUp;
    private LinearLayout circle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.signupforfree);
        circle = findViewById(R.id.circle);

        signIn.setOnClickListener(v->{
            circle.setVisibility(View.INVISIBLE);
            signIn.setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.cl_entry,new LoginFragment()).commit();

        });

        signUp.setOnClickListener(v->{
            getSupportFragmentManager().beginTransaction().replace(R.id.cl_entry,new SignUp()).commit();
        });


    }


}
