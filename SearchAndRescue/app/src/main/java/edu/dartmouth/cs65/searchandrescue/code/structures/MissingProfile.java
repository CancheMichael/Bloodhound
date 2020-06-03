/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.structures;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

//Stores information regarding missing person
@IgnoreExtraProperties
public class MissingProfile {
    private String name, height, age, gender, eyeColor, hairColor, comment;
    private String photo;
    private ArrayList<LatLng> mLocationList; // Location list


    public MissingProfile(){};

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public ArrayList<LatLng> getmLocationList() {
        return mLocationList;
    }

    public String getmLocationListAsString() {
        StringBuilder sb = new StringBuilder();
        if(mLocationList == null)
            return null;
        for (LatLng s : mLocationList)
        {
            sb.append(s.latitude + "," + s.longitude);
            sb.append(" ");
        }
        return sb.toString();
    }

    //Convert String to LatLng, used when retrieving LatLng from Firebase
    public ArrayList<LatLng> createLatLngFromString(String s) {
        if(s == null)
            return null;
        ArrayList<LatLng> newLatLng = new ArrayList<>();
        for(String l: s.split(" ")) {
            String[] latlng = l.split(",");
            double latitude = Double.parseDouble(latlng[0]);
            double longitude = Double.parseDouble(latlng[1]);
            LatLng location = new LatLng(latitude, longitude);
            newLatLng.add(location);
        }
        return newLatLng;
    }

    public void setmLocationList(ArrayList<LatLng> mLocationList) {
        this.mLocationList = mLocationList;
    }
}
