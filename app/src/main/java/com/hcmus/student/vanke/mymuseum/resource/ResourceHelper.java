package com.hcmus.student.vanke.mymuseum.resource;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hcmus.student.vanke.mymuseum.model.MuseumData;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ke nguyen on 28-Jun-18.
 */

public class ResourceHelper {

    public static MuseumData getMuseumDataFromAsset(Context context, SharedPreferences pref) throws IOException, JSONException {
        MuseumData museumData;
        String json = pref.getString("museumdata", "");
        if (json.isEmpty()) {
            InputStream is = context.getAssets().open("museumdata.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            json = new String(buffer, "UTF-8");
            is.close();
        }

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        Gson gson = new Gson();
        museumData = gson.fromJson(jsonElement, MuseumData.class);

        return museumData;
    }
}
