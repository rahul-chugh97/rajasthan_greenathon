package com.specico.solarpesa.models;

/**
 * Created by rajkumar on 20/02/18.
 */

public class Society {
    public Society(String name, String city) {
        this.name = name;
        this.city = city;
    }
    public Society()
    {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;
    public String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSociety(){
        return name+", "+city;
    }


}
