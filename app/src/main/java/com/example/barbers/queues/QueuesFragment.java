package com.example.barbers.queues;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.barbers.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.time.LocalDate;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class QueuesFragment extends Fragment {

    //property
    private MaterialCalendarView mtv;

    //empty constructor
    public QueuesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_queues, container, false);
        /**find view's by id's*/
        //local variables
        Button back = v.findViewById(R.id.btn_back);

        //class variables
        mtv = v.findViewById(R.id.the_Calender);

        //firebase relationship
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.USERPATH).child(Constants.BARBERPATH).child(fUser.getUid());


        /**setOnClickListeners*/
        back.setOnClickListener(b->{
            Navigation.findNavController(v).navigate(R.id.action_queuesFragment_to_costumerHomeFragment);
        });

        /**class methods calls*/
        setCalendar(mtv);
        bookOnCalander(v);

        return v;
    }

    /**
     * created methods
     * */

    private void bookOnCalander(View v){
        Bundle args = getArguments();
        mtv.setOnDateChangedListener((widget, date, selected) -> {
            args.putString(Constants.CalanderConstants.DATE, date.toString());
            args.putString(Constants.CalanderConstants.USERNAME,args.getString(Constants.CalanderConstants.USERNAME));
            args.putString(Constants.CalanderConstants.BARBERID,args.getString(Constants.CalanderConstants.BARBERID));
            String datetoParse="";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (date.getDay()<10) datetoParse = date.getYear() + "-" +"0"+ (1 + date.getMonth()) + "-" + "0"+date.getDay();
                else datetoParse = date.getYear() + "-" +"0"+ (1 + date.getMonth()) + "-" +date.getDay();
                LocalDate d = LocalDate.parse(datetoParse);
                args.putString("day", d.getDayOfWeek().name());
                Navigation.findNavController(v).navigate(R.id.action_queuesFragment_to_queuesTestFragment2, args);
            }
        });
    }

    private void setCalendar(MaterialCalendarView mtv) {
        mtv.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2020, 2, 1))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
    }



}