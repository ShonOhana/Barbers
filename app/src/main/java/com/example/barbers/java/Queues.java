package com.example.barbers.java;

import android.os.Build;

import java.time.LocalDate;

public class Queues implements Comparable<Queues> {

    private LocalDate Date;
    private String Day;
    private String Hour;
    private String name;

    public Queues(LocalDate date, String day, String hour, String name) {
        Date = date;
        Day = day;
        Hour = hour;
        this.name = name;
    }

    public Queues() {
    }

    public LocalDate getDate() {
        return Date;
    }

    public void setDate(LocalDate date) {
        Date = date;
    }

    public String getDay() {
        return Day;
    }

    public void setDay(String day) {
        Day = day;
    }

    public String getHour() {
        return Hour;
    }

    public void setHour(String hour) {
        Hour = hour;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return
                Date + ": " +
                        Day + " - " +
                        " - " + Hour + '\'' +
                        " - " + name ;
    }


    @Override
    public int compareTo(Queues q) {
        if (Date==q.getDate()) {
            if (Hour.equals(q.getHour())) return 0;
            else if (Hour.hashCode()<q.getHour().hashCode()) return 1;
            else return -1;
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Date.compareTo(q.getDate());
            }
        }

        return 0;
    }
}
