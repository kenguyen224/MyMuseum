package com.hcmus.student.vanke.mymuseum.api;

import com.hcmus.student.vanke.mymuseum.model.MuseumData;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by ke nguyen on 28-Jun-18.
 */

public interface GetMuseumDataService {
    @GET("api/museuminfo")
    Observable<MuseumData> getMuseumData();
}
