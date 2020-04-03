package com.example.barbers.barbers;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleForBarberFragment extends Fragment {



    TextView barbers_queues;
    ArrayList<String> queues;
    StringBuilder queuesList;
    DatabaseReference ref;
    FirebaseUser fUser;

    public ScheduleForBarberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v =  inflater.inflate(R.layout.fragment_schedule_for_barber, container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid());

        queuesList= new StringBuilder();
        barbers_queues = v.findViewById(R.id.barbers_queues);

        Button back = v.findViewById(R.id.btn_back);
        back.setOnClickListener(b->{
            Navigation.findNavController(v).navigate(R.id.action_scheduleForBarberFragment_to_barberHomeFragment);
        });


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Barber barber = dataSnapshot.getValue(Barber.class);
                    if (barber.getQueues()!=null) {
                        queues = barber.getQueues();

                        System.out.println(queues + "  queues" );

                        for (int i = 0; i < queues.size(); i++) {
                            queuesList.append(queues.get(i)).append("\n\n");

                            System.out.println(queuesList + "    QUEUESS");
                        }

                        barbers_queues.setText(queuesList.toString());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return v;
    }
}
