package com.example.barbers.clients;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbers.R;
import com.example.barbers.java.Barber;

import java.util.List;

public class ClientHomeAdapter extends RecyclerView.Adapter<ClientHomeAdapter.ClientHomeViewHolder>{

    //properties
    List<Barber> clientssOnline;
    LayoutInflater inflater; //takes xml ->Views

    public ClientHomeAdapter(List<Barber> barbersOnline, LayoutInflater inflater) {
        this.clientssOnline = barbersOnline;
        this.inflater = inflater;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    @NonNull
    @Override
    public ClientHomeAdapter.ClientHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.fragment_barber_home, parent, false);
        return new ClientHomeAdapter.ClientHomeViewHolder(v);
    }

    //set the text in our xml file with our barbers actions
    @Override
    public void onBindViewHolder(@NonNull ClientHomeAdapter.ClientHomeViewHolder holder, int position) {
        Barber client = clientssOnline.get(position);

        holder.profileName.setText(client.getFullName());


    }

    @Override
    public int getItemCount() {
        return clientssOnline.size();
    }

    public static class ClientHomeViewHolder extends RecyclerView.ViewHolder {

        TextView profileName;

        public ClientHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.tv_profile_name);

        }
    }


}
