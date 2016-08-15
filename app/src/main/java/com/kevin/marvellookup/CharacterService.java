package com.kevin.marvellookup;


import com.kevin.marvellookup.pojo.CharactersData;
import com.kevin.marvellookup.pojo.ComicsPOJO.ComicsData;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by kevins on 8/9/16.
 */
public interface CharacterService {
    @GET("characters")
    Observable<CharactersData> getCharactersData(@Query("name") String name, @Query("ts") int ts, @Query("apikey") String apikey, @Query("hash") String hash);

    @GET("characters")
    Observable<CharactersData> getSearchResults(@Query("nameStartsWith") String name,@Query("ts") int ts, @Query("apikey") String apikey, @Query("hash") String hash);

    @GET("characters/{charId}/comics")
    Observable<ComicsData> getComics(@Path("charId") int charId, @Query("ts") int ts, @Query("apikey") String apikey, @Query("hash") String hash);



}
