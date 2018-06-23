package com.hcmus.student.vanke.mymuseum;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vanke on 16/06/2018.
 */

public class IBeaconData {
    @SerializedName("uuid")
    String uuid  = null;
    @SerializedName("major")
    String major  = null;
    @SerializedName("minor")
    String minor = null;

    IBeaconData ( String id1, String id2, String id3) {
        uuid = id1;
        major = id2;
        minor = id3;
    }

    public IBeaconData() {
    }

    public IBeaconData clone() {
        IBeaconData ret = new IBeaconData();

        ret.uuid = uuid;
        ret.major = major;
        ret.minor = minor;

        return ret;
    }
}
