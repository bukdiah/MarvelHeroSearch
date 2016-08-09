package com.kevin.marvellookup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.kevin.marvellookup.adapters.HeroAdapter;
import com.kevin.marvellookup.pojo.CharactersData;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kevins on 8/9/16.
 */
public class MainActivity extends AppCompatActivity {

    static String baseURL = "http://gateway.marvel.com:80/v1/public/";
    static String publicAPI = "971211f705ea2abfa6a3e139bdeafeed";
    static String privateAPI = "ce3c2ac5727f98c9093eba74a4224a2d3cf56abe";
    HeroAdapter adapter;
    RecyclerView recyclerView;

    String hash;
    int min = 1;
    int max = 10000;

    int TS;

    static String id = "";
    static String name = "";
    static String description = "";

    Button btnSearch;
    EditText editName;

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

    public static List<HeroInfo> getData()
    {
        List<HeroInfo> data = new ArrayList<>();
        int [] icons = {R.drawable.reedrichards_medium,R.drawable.reedrichards_medium,R.drawable.reedrichards_medium,R.drawable.reedrichards_medium};
        String [] titles = {"Farty","Sharty","Gero","Tabla"};

        for(int i=0; i<titles.length && i<icons.length;i++)
        {
            HeroInfo current = new HeroInfo();
            current.iconId=icons[i];
            current.name = titles[i];
            data.add(current);

        }
        return data;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new HeroAdapter(this,getData());

        recyclerView.setAdapter(adapter);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        editName = (EditText) findViewById(R.id.etSearch);

        Random r = new Random();
        TS = r.nextInt(max - min + 1) + min;

        Log.d("TimeStamp", Integer.toString(TS));

        md5Hash(TS, privateAPI, publicAPI);


        Log.d("HASH", hash);

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseURL)
                .build();

        CharacterService characterService = retrofit.create(CharacterService.class);

        Observable<CharactersData> hulk = characterService.getCharactersData("hulk",TS,publicAPI,hash);

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
    }
}
