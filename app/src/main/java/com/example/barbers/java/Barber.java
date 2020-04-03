package com.example.barbers.java;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Barber {
    private String barberID;
    private String fullName;
    private String password;
    private String phone;
    private String email;
    private String username;
    private String address;
    private String barbershop;
    private String description;
    private String img;
    private ArrayList<Image> gallery;
    private ArrayList<String> queues;
    private int priority;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String uri) {
        this.img = uri;
    }

    public Barber() {
    }

    public Barber(String barberID, String fullName, String password, String phone, String email, String username, String address,String barbershop, String description, String img, ArrayList<Image> gallery, ArrayList<String> queues, int priority) {
        this.barberID = barberID;
        this.fullName = fullName;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.address = address;
        this.description = description;
        this.barbershop = barbershop;
        this.queues = queues;
        this.img = img;
        this.queues = queues;
        this.gallery = gallery;
        this.priority = priority;
    }

    public Barber(String barberID, String password, String email) {
        this.barberID = barberID;
        this.password = password;
        this.email = email;
    }

    public Barber(String barberID, String fullName, String password, String phone, String email) {
        this.barberID = barberID;
        this.fullName = fullName;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }


    public void setGallery(ArrayList<Image> gallery) {
        this.gallery = gallery;
    }

    public ArrayList<Image> getGallery() {
        return gallery;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getBarbershop() {
        return barbershop;
    }

    public void setBarbershop(String barbershop) {
        this.barbershop = barbershop;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getQueues() {
        return queues;
    }

    public void setQueues(ArrayList<String> queues) {
        this.queues = queues;
    }

    public void setBarberID(String barberID) {
        this.barberID = barberID;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBarberID() {
        return barberID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }


    public ArrayList<Barber> readAllBarbers(DatabaseReference ref){
        ArrayList<Barber> barberArrayList = new ArrayList<>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Barber barber = ds.getValue(Barber.class);

                    barberArrayList.add(barber);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return  barberArrayList;
    }


    public Barber getBarber(String barberID, DatabaseReference ref){
        Barber barber = new Barber();

        ArrayList<Barber> barberArrayList = readAllBarbers(ref);
        for (Barber barber1 : barberArrayList) {
            if (barber1.getBarberID().equals(barberID)){
                return barber1;
            }
        }


        return barber;
    }

    @Override
    public String toString() {
        return "Barber{" +
                "barberID='" + barberID + '\'' +
                ", fullName='" + fullName + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
