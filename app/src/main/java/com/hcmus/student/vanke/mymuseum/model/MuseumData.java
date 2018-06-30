package com.hcmus.student.vanke.mymuseum.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ke nguyen on 28-Jun-18.
 */

public class MuseumData {
    @SerializedName("museumdata")
    public List<MuseumDataObject> listMuseumDataObjects;

    public MuseumData() {
    }
}
