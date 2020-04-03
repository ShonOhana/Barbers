package com.example.barbers;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.barbers.java.Barber;
import com.example.barbers.java.Client;
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
public class queuesTestFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference ref;
    FirebaseUser fUser;
    ArrayList<String> queues;
    boolean isBooked = false;
    String fixedDate;
    Bundle args ;
    String UserName;
    DatabaseReference userRef;
    String hour;
    Button btn_back;
    int booked =0;


    public queuesTestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        args = getArguments();
        View v = inflater.inflate(R.layout.fragment_queues_test, container, false);
        btn_back = v.findViewById(R.id.btn_back);
        swipeRefreshLayout = v.findViewById(R.id.swipe);
        btn_back.setOnClickListener(b->{
            Navigation.findNavController(v).navigate(R.id.action_queuesTestFragment2_to_queuesFragment,args);
        });

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        args = getArguments();
        userRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Client client = dataSnapshot.getValue(Client.class);
                    UserName = client.getFullName();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (args!=null) {
            ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("barberId"));
            String date = args.getString("date");
            String dayB = args.getString("day");


            if (date != null) {
                fixedDate = date.substring(1 + date.indexOf("{"), (date.indexOf("}")));
            }


            for (int k = 0; k < Constants.BUTTONS.length; k++) {


                Button button = v.findViewById(Constants.BUTTONS[k]);


                button.setOnClickListener(b -> {

                    BookBtn(date, dayB, button);

                });

            }
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            (new Handler()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                //todo:find out what we need to refresh
            },2000);
        });
        return v;
    }

    private void CancelBook(Button btn, Bundle args) {

        userRef = FirebaseDatabase.getInstance().getReference().child("users/clients").child(fUser.getUid());
        String IDs = btn.getResources().getResourceName(btn.getId());

        String hour = "";
        if (IDs.length()>37){
            hour = IDs.substring(IDs.indexOf("n_")+2,IDs.indexOf("n_")+4)+":"+IDs.substring(IDs.indexOf("n_")+4);
        }
        else hour = "0"+IDs.substring(IDs.indexOf("n_")+2,IDs.indexOf("n_")+3)+":"+IDs.substring(IDs.indexOf("n_")+3);
        btn.setText(hour);
        ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("barberId"));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int indexToDel = 0;
                    ArrayList<String> ques = new ArrayList<>();
                    Barber barber = dataSnapshot.getValue(Barber.class);

                    if (barber != null) {
                        ques = barber.getQueues();

                        for (int i = 0; i < queues.size(); i++) {
                            if (ques.get(i).substring(0, ques.get(i).indexOf("0 ") + 1).equals(fixedDate + ": " + args.getString("day") + " - " + btn.getText())) {
                                indexToDel = i;
                            }

                        }
                        ques.remove(indexToDel);
                        booked=0;
                        barber = new Barber(barber.getBarberID(), barber.getFullName(), barber.getPassword(), barber.getPhone(), barber.getEmail(), barber.getUsername(), barber.getAddress(), barber.getBarbershop(), barber.getDescription(), barber.getImg(),barber.getGallery(), ques, barber.getPriority());

                    }
                    ref.setValue(barber);


                }

            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){

            }

        });



        btn.setBackgroundResource(R.drawable.queues_btn_not_clicked);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.date_buttons);

        args = getArguments();
        if (args!= null) {
            String date = getArguments().getString("date");
            String dayB = getArguments().getString("day");
            title.setText(dayB + " "+ fixedDate);
            fUser = FirebaseAuth.getInstance().getCurrentUser();
            ref = FirebaseDatabase.getInstance().getReference().child("users").child("barbers").child(args.getString("barberId"));
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Barber b = dataSnapshot.getValue(Barber.class);
                    if (b.getQueues() != null) {
                        queues = b.getQueues();
                        for (int k = 0; k < Constants.BUTTONS.length; k++) {
                            Button button = view.findViewById(Constants.BUTTONS[k]);
                            for (int i = 0; i < queues.size(); i++) {
                                if (queues.get(i).substring(0, queues.get(i).indexOf("0 ") + 1).equals(fixedDate + ": " + dayB + " - " + button.getText())) {
                                    String name = queues.get(i).substring(queues.get(i).indexOf("0 ") + 3).trim();
                                    button.setText(name);
                                    button.setBackgroundResource(R.drawable.queues_btn_clicked);
                                    button.setOnClickListener(v -> {

                                        if (button.getText().toString().equalsIgnoreCase(UserName)){
                                            if (getContext()!=null){
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("are you sure you want to cancel this appointment?").setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        CancelBook(button, args);
                                                    }
                                                }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                            }

                                        }
                                        else Toast.makeText(getContext(), "appointment is already booked", Toast.LENGTH_SHORT).show();

                                    });
                                }
                            }
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }

    private void BookBtn(String date, String dayB, Button button) {
        if (booked == 1) {
            Toast.makeText(getContext(), "cant book more than one appointment a day \n\n cancel your booking or book another day", Toast.LENGTH_LONG).show();

        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Do you want to book an appointment on \n" + dayB + " " + date.toString().substring(1 + date.toString().indexOf("{"), (date.toString().indexOf("}"))) + " at " + button.getText()).
                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Barber barber = dataSnapshot.getValue(Barber.class);
                                    queues = barber.getQueues();
                                    if (queues == null) {
                                        queues = new ArrayList<>();
                                        queues.add("queues");
                                        barber = new Barber(barber.getBarberID(), barber.getFullName(), barber.getPassword(), barber.getPhone(), barber.getEmail(), barber.getUsername(), barber.getAddress(), barber.getBarbershop(), barber.getDescription(), barber.getImg(),barber.getGallery(), queues, barber.getPriority());
                                    }

                                    for (int i = 0; i < queues.size(); i++) {
                                        if (queues.get(i).substring(0, queues.get(i).indexOf("0 ") + 1).equals(fixedDate + ": " + dayB + " - " + button.getText())) {

                                            isBooked = true;
                                        }
                                    }

                                    if (isBooked) {


                                        Toast.makeText(getContext(), "the appointment is occupied", Toast.LENGTH_SHORT).show();
                                    } else {
                                        queues.add(fixedDate + ": " + dayB + " - " + button.getText() + " - " + args.getString("userName"));


                                        Barber brbQ = new Barber(barber.getBarberID(), barber.getFullName(), barber.getPassword(), barber.getPhone(), barber.getEmail(), barber.getUsername(), barber.getAddress(), barber.getBarbershop(), barber.getDescription(), barber.getImg(),barber.getGallery(), queues, barber.getPriority());

                                        ref.setValue(brbQ);
                                        booked = 1;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }


}
