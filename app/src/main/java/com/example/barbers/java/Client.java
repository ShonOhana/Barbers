package com.example.barbers.java;

public class Client {
    //PROPERTIES
    private String clientID;
    private String fullName;
    private String password;
    private String phone;
    private String email;


    //CTORS
    public Client() {
    }

    public Client(String clientID, String fullName, String password, String phone, String email) {
        this.clientID = clientID;
        this.fullName = fullName;
        this.password = password;
        this.phone = phone;
        this.email = email;

    }

    //GETTERS
    public String getClientID() {
        return clientID;
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

    @Override
    public String toString() {
        return "Barber{" +
                "barberID='" + clientID + '\'' +
                ", fullName='" + fullName + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
