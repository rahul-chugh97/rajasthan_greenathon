package com.specico.solarpesa.models;

import android.content.Context;

import com.google.firebase.database.IgnoreExtraProperties;
import com.specico.solarpesa.Utils.Constants;
import com.specico.solarpesa.Utils.SharedPrefUtil;

/**
 * Created by RAJ on 2/18/2018.
 */

@IgnoreExtraProperties
public class User {

    public String phone;
    public String password;
    public String name;
    public String photoUri;
    public String email;
    public String address;
    public String district;
    public String state;

    public User(String phone, String password, String name, String photoUri, String email, String address, String district, String state) {
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.photoUri = photoUri;
        this.email = email;
        this.address = address;
        this.district = district;
        this.state = state;
    }

    public User(String phone, String password, String name, String email, String address, String district, String state) {
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.email = email;
        this.address = address;
        this.district = district;
        this.state = state;
    }

    public User(String phone, String name, String email, String address, String district, String state) {
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.address = address;
        this.district = district;
        this.state = state;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public void saveUserToSharedPref(Context context) {
        SharedPrefUtil.setStringPreference(context, Constants.USER_NAME,name);
        SharedPrefUtil.setStringPreference(context, Constants.USER_EMAIL,email);
        SharedPrefUtil.setStringPreference(context, Constants.USER_PHONE,phone);
        if(null != photoUri)
            SharedPrefUtil.setStringPreference(context, Constants.USER_PHOTO,photoUri);
        SharedPrefUtil.setStringPreference(context, Constants.USER_ADDRESS,address);
        SharedPrefUtil.setStringPreference(context, Constants.USER_DISTRICT,district);
        SharedPrefUtil.setStringPreference(context, Constants.USER_STATE,state);

    }

}