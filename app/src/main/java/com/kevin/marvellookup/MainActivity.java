package com.kevin.marvellookup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kevin.marvellookup.pojo.CharactersData;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by kevins on 8/9/16.
 */
public class MainActivity extends AppCompatActivity {

    //static String baseURL = "http://gateway.marvel.com:80/v1/public/characters?name=";
    static String baseURL = "http://gateway.marvel.com:80/v1/public/";
    static String publicAPI = "971211f705ea2abfa6a3e139bdeafeed";
    static String privateAPI = "ce3c2ac5727f98c9093eba74a4224a2d3cf56abe";
    String url;

    String hash;
    int min = 1;
    int max = 10000;

    int TS;

    static String id = "";
    static String name = "";
    static String description = "";

    Button btnSearch;
    EditText editName;
    TextView textID, textName, textDesc;

    public void md5Hash(int TS, String privateKey, String publicKey) {
        String combo = TS + privateKey + publicKey;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(combo.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuffer sb = new StringBuffer();

            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            hash = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseURL)
                .build();

        CharacterService characterService = retrofit.create(CharacterService.class);

        Observable<CharactersData> hulk = characterService.getCharactersData("hulk",publicAPI);

        hulk.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharactersData>() {
                    @Override
                    public void onCompleted() {
                        Log.d("COMPLETE","COMPLETE!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof HttpException)
                        {
                            HttpException response = (HttpException) e;
                            int code = response.code();

                            Log.e("Response code",Integer.toString(code));
                        }
                    }

                    @Override
                    public void onNext(CharactersData charactersData) {
                        Log.d("CHARACTER", charactersData.getData().getResults().get(0).getDescription());

                    }
                });
        /*

        hulk.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharactersData>() {
                    @Override
                    public void call(CharactersData charactersData) {
                        Log.e("CHARACTER", charactersData.getData().getResults().get(0).getDescription());
                    }
                });*/
    }
}
