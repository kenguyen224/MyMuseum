package com.hcmus.student.vanke.mymuseum.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ke nguyen on 24-Jun-18.
 */

public class MuseumDataObject {
    @SerializedName("major")
    public String major;
    @SerializedName("minor")
    public String minor;
    @SerializedName("name")
    public String name;
    @SerializedName("detail")
    public String info;
    @SerializedName("return code")
    public int retCode;

    public MuseumDataObject(String major, String minor, String name, String info) {
        this.major = major;
        this.minor = minor;
        this.name = name;
        this.info = info;
    }

    public MuseumDataObject() {
    }
}
