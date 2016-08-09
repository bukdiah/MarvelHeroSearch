package com.kevin.marvellookup;


import com.kevin.marvellookup.pojo.CharactersData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by kevins on 8/9/16.
 */
public interface CharacterService {
    @GET("characters")
    Observable<CharactersData> getCharactersData(@Query("name") String name, @Query("apikey") String apikey);
   // @GET("characters?name={name}&apikey={api_key}")
    //Observable<CharactersData> getCharactersData(@Path("name") String name, @Path("api_key") String api_key);

}
