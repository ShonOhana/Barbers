package com.example.barbers.login.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.barbers.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.barbers.R.id.nav_host_fragment;


public class BlankFragment extends Fragment {

    //constructor
    public BlankFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        /**class methods calls*/
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) navigateToUserType();

        return view;
    }

    /**created methods*/
    //navigate this loading page to the barber page / client page
    private void navigateToUserType (){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals("barbers")) {
                        try {

                            if (child.child(uid).getValue() != null) {//
                                Navigation.findNavController(getActivity(), nav_host_fragment).navigate(R.id.action_blankFragment_to_barberHomeFragment);

                            } else {
                                Bundle args = new Bundle();

                                Navigation.findNavController(getActivity(), nav_host_fragment).navigate(R.id.action_blankFragment_to_costumerHomeFragment);
                            }
                        }catch (NullPointerException e){
                            System.out.println("activity is null");
                        } catch (IllegalArgumentException e){
                            System.out.println("illigal argument");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.getMessage();
            }
        });


    }
}
