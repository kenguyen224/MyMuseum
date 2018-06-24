package com.hcmus.student.vanke.mymuseum.api;

import com.hcmus.student.vanke.mymuseum.model.MuseumDataObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by kengu on 24-Jun-18.
 */

public interface GetMuseumInfoService {
    @GET("api/museuminfo")
    Call<MuseumDataObject> getMuseumInfo(@Query("major") String major, @Query("minor") String minor);
}
