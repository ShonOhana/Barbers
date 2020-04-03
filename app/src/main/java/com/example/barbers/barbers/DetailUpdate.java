package com.example.barbers.barbers;


import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.barbers.R;
import com.example.barbers.java.Barber;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailUpdate extends Fragment {

    private DatabaseReference ref;
    private Button btn_update_name;
    private EditText fname_update;
    private Button btn_update_phone;
    private EditText phone_update;
    private Button btn_update_mail;
    private EditText mail_update;
    private Button btn_update_type;
    private EditText address_update;
    private Button btn_update_barbershop;
    private EditText barbershop_update;
    private Button btn_update_pass;
    private EditText pass_update;


    public DetailUpdate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.detail_update_fragment, container, false);
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid());
        System.out.println(fUser.getUid() + "  user id");
        btn_update_name = v.findViewById(R.id.btn_update_name);
        fname_update = v.findViewById(R.id.fname_update);
        btn_update_phone = v.findViewById(R.id.btn_update_phone);
        phone_update = v.findViewById(R.id.phone_update);
        btn_update_mail = v.findViewById(R.id.btn_update_email);
        mail_update = v.findViewById(R.id.mail_update);
        btn_update_type = v.findViewById(R.id.btn_update_address);
        address_update = v.findViewById(R.id.type_update);
        barbershop_update = v.findViewById(R.id.et_barbershop_name);
        btn_update_barbershop = v.findViewById(R.id.btn_update_barbershop);
        btn_update_pass = v.findViewById(R.id.btn_update_password);
        pass_update = v.findViewById(R.id.pswrd);


        btn_update_name.setOnClickListener(b -> {
            String fullName = fname_update.getText().toString();
            if (fullName.length() < 4) fname_update.setError(getString(R.string.tooShort));
            else updateDetail(fUser,btn_update_name);
        });
        btn_update_phone.setOnClickListener(b -> {
            String phone = phone_update.getText().toString();
            Pattern phonePattern = Patterns.PHONE;
            boolean validPhone = phonePattern.matcher(phone).matches();
            if (!validPhone) phone_update.setError(getString(R.string.notValid));
            else updateDetail(fUser,btn_update_phone);
        });
        btn_update_mail.setOnClickListener(b -> {
            String email = mail_update.getText().toString();
            Pattern emailAddressPattern = Patterns.EMAIL_ADDRESS;
            boolean validEmail = emailAddressPattern.matcher(email).matches();
            if (!validEmail) mail_update.setError(getString(R.string.notValid));
            else updateDetail(fUser,btn_update_mail);
        });
        btn_update_type.setOnClickListener(b -> {
            String userAddress = address_update.getText().toString();
            if (userAddress.length() < 4)
                address_update.setError(R.string.notValid + getString(R.string.length4));
            else updateDetail(fUser,btn_update_type);
        });
        btn_update_barbershop.setOnClickListener(b -> {

            updateDetail(fUser,btn_update_barbershop);
        });
        btn_update_pass.setOnClickListener(b -> {
            String pass = pass_update.getText().toString();
            if (pass.length() < 6) pass_update.setError(getString(R.string.length6));
            else updateDetail(fUser,btn_update_pass);
        });


        v.findViewById(R.id.back_to_Barber).setOnClickListener(b -> {
            NavController nv = Navigation.findNavController(v);
            nv.navigate(R.id.action_detailUpdate_to_barberHomeFragment);
        });


        return v;
    }

    private void updateDetail(FirebaseUser fUser, Button b) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Barber brb = dataSnapshot.getValue(Barber.class);
                String changeId = b.getResources().getResourceName(b.getId());
                String key = changeId.substring(changeId.indexOf("e_") + 2);
                switch (key) {
                    case "name":
                        assert brb != null;
                        brb.setFullName(fname_update.getText().toString());
                        ref.setValue(brb);
                        Toast.makeText(getContext(), getString(R.string.the) + getString(R.string.name) + " " + getString(R.string.wasChanged), Toast.LENGTH_SHORT).show();
                        break;
                    case "phone":
                        assert brb != null;
                        brb.setPhone(phone_update.getText().toString());
                        ref.setValue(brb);
                        Toast.makeText(getContext(), getString(R.string.the) + getString(R.string.phoneNumber) + " " + getString(R.string.wasChanged), Toast.LENGTH_SHORT).show();
                        break;
                    case "email":
                        assert brb != null;
                        brb.setEmail(mail_update.getText().toString());
                        ref.setValue(brb);
                        Toast.makeText(getContext(), getString(R.string.the) + getString(R.string.emailKey) + " " + getString(R.string.wasChanged), Toast.LENGTH_SHORT).show();
                        break;
                    case "address":
                        assert brb != null;
                        brb.setAddress(phone_update.getText().toString());
                        ref.setValue(brb);
                        Toast.makeText(getContext(), getString(R.string.the) + getString(R.string.addressKey) + " " + getString(R.string.wasChanged), Toast.LENGTH_SHORT).show();
                        break;
                    case "barbershop":
                        assert brb != null;
                        brb.setBarbershop(barbershop_update.getText().toString());
                        ref.setValue(brb);
                        Toast.makeText(getContext(), getString(R.string.the) + getString(R.string.barberShopKey) + " " + getString(R.string.wasChanged), Toast.LENGTH_SHORT).show();
                        break;
                    case "password":
                        assert brb != null;
                        brb.setPassword(pass_update.getText().toString());
                        ref.setValue(brb);
                        Toast.makeText(getContext(), getString(R.string.the) + getString(R.string.password) + " " + getString(R.string.wasChanged), Toast.LENGTH_SHORT).show();
                        break;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void dialogAlet(FirebaseUser fUser, Button b, String set) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.changeDialog) + " " + set + "?").setPositiveButton(R.string.yes, (dialog, which) -> updateDetail(fUser, b)).setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
