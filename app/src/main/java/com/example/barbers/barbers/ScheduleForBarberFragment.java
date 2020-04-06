package com.example.barbers.barbers;


import android.os.Build;
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
import com.example.barbers.java.Queues;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleForBarberFragment extends Fragment {

    //properties
    private TextView barbers_queues;
    private ArrayList<String> queues;
    private StringBuilder queuesList;
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<Queues> arrangeQ = new ArrayList<>();
    private ArrayList<String> currentQ = new ArrayList<>();

    //empty constructor
    public ScheduleForBarberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_schedule_for_barber, container, false);

        /**find view's by id's*/
        //local variables
        Button back = v.findViewById(R.id.btn_back);

        //class variables
        queuesList = new StringBuilder();
        barbers_queues = v.findViewById(R.id.barbers_queues);


        /**setOnClickListeners*/
        back.setOnClickListener(b -> {
            Navigation.findNavController(v).navigate(R.id.action_scheduleForBarberFragment_to_barberHomeFragment);
        });

        //FireBase relationship
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(fUser.getUid());

        getAllQueues(ref);


        return v;
    }


    private void getAllQueues(DatabaseReference ref) {

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Barber barber = dataSnapshot.getValue(Barber.class);
                    if (barber != null) {
                        if (barber.getQueues().size() > 0) {
                            if (barber.getQueues().get(0).equals("queues")){
                                queuesList.append("No queues yet");
                            }else {
                                if (queuesList.equals(new StringBuilder("No queues yet"))) {
                                    queuesList.delete(0, queuesList.length() - 1);
                                } else {
                                    currentQ = barber.getQueues();
                                    for (int i = 0; i < barber.getQueues().size(); i++) {
                                        //get date by subsring from que on DB
                                        String date = currentQ.get(i).substring(0, currentQ.get(i).indexOf(":"));
                                        StringBuilder stDate = new StringBuilder(date);
                                        //make month of que 2 digits
                                        stDate.insert(5, 0);
                                        System.out.println(date + " Date");
                                        //if stDate length is 9 it means the day is 1 digit
                                        if (stDate.length() == 9)
                                            stDate.insert(stDate.length() - 1, 0);
                                        System.out.println(stDate);
                                        String day = null;
                                        //check build version so we can apply localDate
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            day = LocalDate.parse(stDate).getDayOfWeek().toString();
                                        }
                                        // get hour by substring from que on DB
                                        String hour = currentQ.get(i).substring(currentQ.get(i).indexOf("Y - ") + 4, currentQ.get(i).indexOf("0 ") + 1);
                                        // get name by substring from que on DB
                                        String name = currentQ.get(i).substring(currentQ.get(i).indexOf("0 - ") + 4);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                            arrangeQ.add(new Queues(LocalDate.parse(stDate), day, hour, name));
                                        }


                                    }
                                    //sort ques
                                    Collections.sort(arrangeQ);
                                }
                            }
                        }
                        for (Queues que : arrangeQ) {
                            queuesList.append(que.getDate()).append(": ").append(que.getDay()).
                                    append(" - ").append(que.getHour()).
                                    append(" - ").append(que.getName()).append("\n\n");

                        }

                        barbers_queues.setText(queuesList);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
