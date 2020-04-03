package com.example.barbers;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FirebaseViewHolder extends RecyclerView.ViewHolder {


    public FirebaseViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.findViewById(R.id.tv_profile_name);
        itemView.findViewById(R.id.barber_address);
        itemView.findViewById(R.id.tv_barbershop);
        itemView.findViewById(R.id.tv_barbershop);


    }


}
