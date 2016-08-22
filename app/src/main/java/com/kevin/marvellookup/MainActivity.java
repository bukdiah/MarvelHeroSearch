package com.kevin.marvellookup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.marvellookup.adapters.HeroAdapter;
import com.kevin.marvellookup.pojo.CharactersData;
import com.kevin.marvellookup.pojo.ComicsPOJO.ComicsData;
import com.kevin.marvellookup.pojo.Result;
import com.kevin.marvellookup.pojo.Url;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by kevins on 8/9/16.
 */
public class MainActivity extends AppCompatActivity {

    private static String baseURL = "http://gateway.marvel.com:80/v1/public/";
    private static String publicAPI = "971211f705ea2abfa6a3e139bdeafeed";
    private static String privateAPI = "ce3c2ac5727f98c9093eba74a4224a2d3cf56abe";

    private Context context;
    private HeroAdapter adapter;
    private RecyclerView recyclerView;

    private String hash;
    private int min = 1;
    private int max = 10000;

    private int TS;

    Button btnSearch;
    EditText editName;
    Retrofit retrofit;

    Intent i;
    Bundle bundle;

    private CharacterService characterService, characterDetail, characterComics;
    private Observable<CharactersData> searchResults, detailSearch;
    private Observable<ComicsData> comicSearch;
    private Observable<HashMap<String, String>> fetchFromWiki;
    private CompositeSubscription mCompositeSubscription;

    private ProgressDialog mProgressDialog;

    private int charId;

    private boolean connection = false;
    private TextView emptyView;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCompositeSubscription.unsubscribe();

    }

    //Function produces a md5Hash needed as an argument for HTTP Request
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

        mCompositeSubscription = new CompositeSubscription();

        i = new Intent(this, TabLayoutActivity.class);
        bundle = new Bundle();

        context = this;

        //Check if Wifi or Mobile is enabled
        connection = checkNetwork();

        Log.d("Connection", "value =  "+connection);

        if (connection == false)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this);
            builder.setTitle("Marvel Search");
            builder.setMessage("This app needs WiFi enabled. Do you want to turn it on?");
            builder.setCancelable(false);
            builder.setPositiveButton("Enable Connectivity",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                final DialogInterface dialogInterface,
                                final int i) {
                            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                            wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
                            connection = checkNetwork();

                        }
                    });
            builder.setNegativeButton("Quit Application", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.create().show();
        }


        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setVisibility(View.GONE);

        emptyView = (TextView) findViewById(R.id.empty_view);
        emptyView.setVisibility(View.VISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        //adapter = new HeroAdapter(this,getData());

        //adapter.setClickListener(this);
        //recyclerView.setAdapter(adapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        editName = (EditText) findViewById(R.id.etSearch);

        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseURL)
                .build();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(context,"Please Wait","Searching for hero");
                showDialog();

                if(connection){
                    searchHero();
                }

                else
                {
                    return;
                }
            }
        });

    }

    private void createDialog(Context context, String title, String message)
    {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
    }

    private void showDialog()
    {
        mProgressDialog.show();
    }

    private void dismissDialog()
    {
        mProgressDialog.dismiss();
    }

    /*
    Does a Marvel API call to retrieve a list of characters whose name Starts With
    the String entered in the EditText
     */
    private void searchHero() {


        if(connection == false)
        {
            Toast.makeText(this,"Enable WiFI to search!", Toast.LENGTH_SHORT);
            return;
        }

        if (connection == true)
        {
            Log.d("Connection", "searchHero() if condition true. connection  =  "+connection);

            String nameStartsWith = editName.getText().toString();

            characterService = retrofit.create(CharacterService.class);

            if (nameStartsWith != null) {
                Random r = new Random();
                TS = r.nextInt(max - min + 1) + min;

                Log.d("TimeStamp", Integer.toString(TS));

                md5Hash(TS, privateAPI, publicAPI);

                Log.d("HASH", hash);

                searchResults = characterService.getSearchResults(nameStartsWith, TS, publicAPI, hash);

                mCompositeSubscription.add(
                        searchResults.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .retry()
                                .subscribe(new Subscriber<CharactersData>() {
                                    @Override
                                    public void onCompleted() {
                                        //mProgressDialog.dismiss();
                                        dismissDialog();
                                        Log.d("COMPLETE", "COMPLETE!");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (e instanceof HttpException) {
                                            HttpException response = (HttpException) e;
                                            int code = response.code();

                                            Log.e("Response code", Integer.toString(code));
                                        }
                                    }

                                    @Override
                                    public void onNext(CharactersData charactersData) {
                                        //List of Result objects
                                        List<Result> data = charactersData.getData().getResults();
                                        List<HeroInfo> heroes = new ArrayList<>();

                                        //Create HeroInfo objects outta all of these and put inside a List<HeroInfo>
                                        for (Result temp : data) {
                                            String name = temp.getName();
                                            String imageURL = temp.getThumbnail().getPath() + "/standard_large.jpg";
                                            //int iconId = temp.getId();
                                            int charId = temp.getId();

                                            HeroInfo current = new HeroInfo();
                                            current.name = name;
                                            current.imageURL = imageURL;
                                            current.charId = charId;
                                            heroes.add(current);
                                        }

                                        if(heroes.isEmpty())
                                        {
                                            recyclerView.setVisibility(View.GONE);
                                            emptyView.setVisibility(View.VISIBLE);
                                            emptyView.setText("No results found :(");
                                        }
                                        else
                                        {
                                            recyclerView.setVisibility(View.VISIBLE);
                                            emptyView.setVisibility(View.GONE);
                                        }
                                        adapter = new HeroAdapter(context, heroes);

                                        adapter.setClickListener(new HeroAdapter.ClickListener() {
                                            @Override
                                            public void onItemClick(View itemView, int position) {
                                                TextView tvName = (TextView) itemView.findViewById(R.id.tvHero);
                                                String name = tvName.getText().toString();

                                                int Id = tvName.getMaxEms();
                                                Log.d("Mainactivity", "onItemClick: YES");
                                                Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                    // If we're running on Honeycomb or newer, then we can use the Theme's
                                                    // selectableItemBackground to ensure that the View has a pressed state
                                                    TypedValue outValue = new TypedValue();
                                                    context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                                                    itemView.setBackgroundResource(outValue.resourceId);
                                                }

                                                /*
                                                mProgressDialog = new ProgressDialog(context);
                                                mProgressDialog.setTitle("Please wait");
                                                mProgressDialog.setMessage("Getting character details...");
                                                mProgressDialog.setCancelable(false);
                                                mProgressDialog.setIndeterminate(true);
                                                mProgressDialog.show();*/
                                                createDialog(context,"Please Wait","Getting character details...");
                                                showDialog();
                                                //Pass String name into a method that calls RxAndroid for another query
                                                searchHeroDetail(name,Id);
                                                //adapterClick(name, Id);

                                                //i.putExtras(bundle);
                                                //startActivity(i);

                                            }
                                        });
                                        //recyclerView.setAdapter(adapter);
                                        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
                                        alphaInAnimationAdapter.setDuration(1000);
                                        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
                                        recyclerView.setAdapter(alphaInAnimationAdapter);

                                    }
                                })
                );

            }
        }


    }

    /*
    Called when User clicks an Item in the RecyclerView.
    Performs a detailed search of the User clicked character
     */
    private void adapterClick(String name, int id) {
        Log.d("adapterclick", "adapterClick called");

        characterDetail = retrofit.create(CharacterService.class);

        if (name != null) {
            Log.d("adapterClick", "INSIDE IF STATEMENT");
            Log.d("adapterclick", "name value = " + name);

            Random r = new Random();
            TS = r.nextInt(max - min + 1) + min;

            Log.d("TimeStamp", Integer.toString(TS));

            md5Hash(TS, privateAPI, publicAPI);

            Log.d("HASH", hash);

            charId = id;

            detailSearch = characterDetail.getCharactersData(name, TS, publicAPI, hash);

            mCompositeSubscription.add(
                    detailSearch.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<CharactersData>() {
                                @Override
                                public void onCompleted() {
                                    mProgressDialog.dismiss();
                                    Log.d("AdapterClick", "onCompleted: DONE");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (e instanceof HttpException) {
                                        HttpException response = (HttpException) e;
                                        int code = response.code();

                                        Log.e("Response code", Integer.toString(code));
                                    }
                                }

                                @Override
                                public void onNext(CharactersData charactersData) {

                                    Log.d("adapterclick", " detailSearch onNext: CALLED");
                                    //Intent i = new Intent(context,TabLayoutActivity.class);

                                    List<Result> data = charactersData.getData().getResults();
                                    List<Url> URLs;

                                    for (Result temp : data) {
                                        String desc = temp.getDescription();
                                        URLs = temp.getUrls();

                                        Log.d("adapterclick", "ELSE clause of deatileSearch");
                                        for (Url u : URLs) {
                                            if (u.getType().equals("wiki")) {
                                                final String wiki = u.getUrl();
                                                Log.d("adapterclick", "else clause onNext: URL " + wiki);
                                                Observable<HashMap<String, String>> fetchFromWiki = Observable.create(new Observable.OnSubscribe<HashMap<String, String>>() {
                                                    @Override
                                                    public void call(Subscriber<? super HashMap<String, String>> subscriber) {
                                                        try {
                                                            HashMap<String, String> dataMap = searchDetail(wiki);
                                                            subscriber.onNext(dataMap); //Emit the hashmap
                                                            subscriber.onCompleted();
                                                        } catch (Exception e) {
                                                            subscriber.onError(e);
                                                        }
                                                    }
                                                });

                                                fetchFromWiki.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Subscriber<HashMap<String, String>>() {
                                                            @Override
                                                            public void onCompleted() {
                                                                Log.d("FetchFromWiki", "onCompleted: starting comic search");
                                                                characterComics = retrofit.create(CharacterService.class);
                                                                Log.d("FetchFromWiki", "onCompleted: charId = " + charId);

                                                                comicSearch = characterComics.getComics(charId, TS, publicAPI, hash);

                                                                comicSearch.subscribeOn(Schedulers.newThread())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(new Subscriber<ComicsData>() {
                                                                            List<ComicsInfo> comics = new ArrayList<>();

                                                                            @Override
                                                                            public void onCompleted() {
                                                                                mProgressDialog.dismiss();

                                                                                for (ComicsInfo c : comics) {
                                                                                    Log.d("comicsSearch Array ", c.getName() + " " + c.getImageURL());
                                                                                }
                                                                                i.putExtras(bundle);
                                                                                startActivity(i);

                                                                            }

                                                                            @Override
                                                                            public void onError(Throwable e) {
                                                                                if (e instanceof HttpException) {
                                                                                    HttpException response = (HttpException) e;
                                                                                    int code = response.code();

                                                                                    Log.e("Response code", Integer.toString(code));
                                                                                }

                                                                                if (e instanceof SocketTimeoutException) {
                                                                                    Log.e("COMICS", "onError: ", e);
                                                                                    comicSearch.retry();
                                                                                }
                                                                                comicSearch.retry();


                                                                            }

                                                                            @Override
                                                                            public void onNext(ComicsData comicsData) {
                                                                                Log.d("comicSearch", "onNext: inside");
                                                                                List<com.kevin.marvellookup.pojo.ComicsPOJO.Result> data = comicsData.getData().getResults();

                                                                                for (com.kevin.marvellookup.pojo.ComicsPOJO.Result temp : data) {
                                                                                    String title = temp.getTitle();

                                                                                    String thumbnailURL = temp.getThumbnail().getPath().concat("/portrait_incredible.jpg");

                                                                                    ComicsInfo comic = new ComicsInfo(title, thumbnailURL);
                                                                                    comics.add(comic);
                                                                                }
                                                                                bundle.putParcelableArrayList(ComicsFragment.COMICS, (ArrayList<ComicsInfo>) comics);

                                                                                for (ComicsInfo c : comics) {
                                                                                    Log.d("comicsSearch Array", "onNext() " + c.getName() + " " + c.getImageURL());
                                                                                }
                                                                            }
                                                                        });

                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                Log.e("FetchFromWiki", "onError: ", e);
                                                            }

                                                            @Override
                                                            public void onNext(HashMap<String, String> map) {
                                                                Log.d("fetchFromWiki", "call: POOP ");

                                                                bundle.putString("bio", map.get("bio"));
                                                                bundle.putString("powers", map.get("powers"));
                                                                bundle.putString("abilities", map.get("abilities"));
                                                            }
                                                        });
                                                /*
                                                fetchFromWiki
                                                        .subscribeOn(Schedulers.newThread())
                                                        .observeOn(AndroidSchedulers.mainThread())

                                                        .subscribe(new Action1<HashMap<String, String>>() {
                                                            @Override
                                                            public void call(HashMap<String, String> map) {
                                                                Log.d("fetchFromWiki", "call: POOP ");

                                                                bundle.putString("bio",map.get("bio"));
                                                                bundle.putString("powers",map.get("powers"));
                                                                bundle.putString("abilities",map.get("abilities"));

                                                                //i.putExtras(bundle);
                                                                //startActivity(i);
                                                            }
                                                        });*/
                                            }
                                        }

                                    }
                                }
                            })
            );


            //Observable to retrieve comics of specified charId
            //characterComics = retrofit.create(CharacterService.class);

            //comicSearch = characterComics.getComics(charId,TS,publicAPI,hash);

            /*
            detailSearch.doOnNext(new Action1<CharactersData>() {
                @Override
                public void call(CharactersData charactersData) {
                    Log.d("adapterclick", " detailSearch onNext: CALLED");
                    //Intent i = new Intent(context,TabLayoutActivity.class);

                    List<Result> data = charactersData.getData().getResults();
                    List<Url> URLs;

                    for(Result temp : data)
                    {
                        URLs = temp.getUrls();

                        for(Url u: URLs)
                        {
                            if(u.getType().equals("wiki"))
                            {
                                final String wiki = u.getUrl();

                                Observable<HashMap<String,String>> fetchFromWiki = Observable.create(new Observable.OnSubscribe<HashMap<String, String>>() {
                                    @Override
                                    public void call(Subscriber<? super HashMap<String, String>> subscriber) {
                                        try{
                                            HashMap<String,String> dataMap = searchDetail(wiki);
                                            subscriber.onNext(dataMap); //Emit the hashmap
                                            subscriber.onCompleted();
                                        }catch(Exception e)
                                        {
                                            subscriber.onError(e);
                                        }
                                    }
                                });

                                fetchFromWiki
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())

                                        .subscribe(new Action1<HashMap<String, String>>() {
                                            @Override
                                            public void call(HashMap<String, String> map) {
                                                Log.d("fetchFromWiki", "call: MAP");
                                                Log.d("fetchFromWiki","BIO: "+map.get("bio"));
                                                Log.d("fetchFromWiki","POWERS: "+map.get("powers"));
                                                Log.d("fetchFromWiki","ABILITIES: "+map.get("abilities"));
                                                //detailMap.put("bio",map.get("bio"));
                                                //detailMap.put("powers",map.get("powers"));
                                               // detailMap.put("abilities",map.get("abilities"));

                                                bundle.putString("bio",map.get("bio"));
                                                bundle.putString("powers",map.get("powers"));
                                                bundle.putString("abilities",map.get("abilities"));
                                            }
                                        });

                            }
                        }
                    }


                }
            });

            comicSearch.doOnNext(new Action1<ComicsData>() {
                @Override
                public void call(ComicsData comicsData) {
                    List<com.kevin.marvellookup.pojo.ComicsPOJO.Result> data = comicsData.getData().getResults();
                    List<ComicsInfo> comics = new ArrayList<>();

                    for(com.kevin.marvellookup.pojo.ComicsPOJO.Result temp: data)
                    {
                        String title = temp.getTitle();

                        String thumbnailURL = temp.getThumbnail().getPath().concat("/portrait_incredible.jpg");

                        ComicsInfo comic = new ComicsInfo(title,thumbnailURL);
                        comics.add(comic);
                    }
                    bundle.putParcelableArrayList(ComicsFragment.COMICS, (ArrayList<ComicsInfo>)comics);

                    System.out.println("Comics ARRAY LIST");

                    for(ComicsInfo c: comics)
                    {
                        System.out.println("TITLE: "+c.getName());
                        System.out.println("URL: "+c.getImageURL());
                    }
                }
            });
            detailSearch.subscribeOn(Schedulers.newThread());
            comicSearch.subscribeOn(Schedulers.newThread());
            
            Observable.zip(detailSearch, comicSearch, new Func2<CharactersData, ComicsData, Object>() {
                @Override
                public Object call(CharactersData charactersData, ComicsData comicsData) {
                    return null;
                }
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        Log.d("BUNDLE", String.format("%s %s (%s)", key,
                                value.toString(), value.getClass().getName()));
                    }
                }
            });*/
/*
            comicSearch.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<ComicsData>() {
                        List<ComicsInfo> comics = new ArrayList<>();
                        @Override
                        public void onCompleted() {


                        }

                        @Override
                        public void onError(Throwable e) {


                        }

                        @Override
                        public void onNext(ComicsData comicsData) {
                            List<com.kevin.marvellookup.pojo.ComicsPOJO.Result> data = comicsData.getData().getResults();
                            //List<ComicsInfo> comics = new ArrayList<>();

                            for(com.kevin.marvellookup.pojo.ComicsPOJO.Result temp: data)
                            {
                                String title = temp.getTitle();

                                String thumbnailURL = temp.getThumbnail().getPath().concat("/portrait_incredible.jpg");

                                ComicsInfo comic = new ComicsInfo(title,thumbnailURL);
                                comics.add(comic);
                            }
                            bundle.putParcelableArrayList(ComicsFragment.COMICS, (ArrayList<ComicsInfo>)comics);

                            //i.putExtras(bundle);
                            //startActivity(i);
                        }
                    });*/


        }
    }

    private void searchHeroDetail(String name, int id)
    {
        createObservables(name,id);
    }

    private void createObservables(String name, int id)
    {
        characterDetail = retrofit.create(CharacterService.class);
        characterComics = retrofit.create(CharacterService.class);

        //Returns  Observable<CharactersData>
        detailSearch = characterDetail.getCharactersData(name,TS, publicAPI,hash);
        //Return Observable<ComicsData>
        comicSearch = characterComics.getComics(id,TS,publicAPI,hash);

        mCompositeSubscription.add(
                detailSearch
                        .flatMap(new Func1<CharactersData, Observable<HashMap<String,String>>>() {
                            @Override
                            public Observable<HashMap<String, String>> call(CharactersData charactersData) {
                                List<Result> data = charactersData.getData().getResults();
                                List<Url> URLs;

                                for (Result temp: data)
                                {
                                    URLs = temp.getUrls();

                                    for (Url u: URLs)
                                    {
                                        if(u.getType().equals("wiki"))
                                        {
                                            final String wiki = u.getUrl();

                                            fetchFromWiki = Observable.create(new Observable.OnSubscribe<HashMap<String, String>>() {
                                                @Override
                                                public void call(Subscriber<? super HashMap<String, String>> subscriber) {
                                                    try {
                                                        HashMap<String, String> dataMap = searchDetail(wiki);
                                                        subscriber.onNext(dataMap); //Emit the hashmap
                                                        subscriber.onCompleted();
                                                    } catch (Exception e) {
                                                        subscriber.onError(e);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                                return fetchFromWiki;
                            }
                        })
                        .flatMap(new Func1<HashMap<String, String>, Observable<ComicsData>>() {
                            @Override
                            public Observable<ComicsData> call(HashMap<String, String> map) {
                                bundle.putSerializable(BioFragment.BIO, map.get("bio"));
                                bundle.putSerializable(PowersAbilitiesFragment.POWERS, map.get("powers"));
                                bundle.putSerializable(PowersAbilitiesFragment.ABILITIES, map.get("abilities"));

                                return comicSearch;
                            }
                        })
                        .retry()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ComicsData>() {
                            List<ComicsInfo> comics = new ArrayList<>();
                            @Override
                            public void onCompleted() {
                                dismissDialog();
                                i.putExtras(bundle);
                                startActivity(i);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e instanceof HttpException) {
                                    HttpException response = (HttpException) e;
                                    int code = response.code();

                                    Log.e("Response code", Integer.toString(code));
                                }

                                if (e instanceof SocketTimeoutException) {
                                    Log.e("COMICS", "onError: ", e);
                                }

                            }

                            @Override
                            public void onNext(ComicsData comicsData) {
                                Log.d("comicSearch", "onNext: inside");
                                List<com.kevin.marvellookup.pojo.ComicsPOJO.Result> data = comicsData.getData().getResults();
                                for (com.kevin.marvellookup.pojo.ComicsPOJO.Result temp: data)
                                {
                                    String title = temp.getTitle();
                                    String thumbnailURL = temp.getThumbnail().getPath().concat("/portrait_incredible.jpg");

                                    ComicsInfo comic = new ComicsInfo(title,thumbnailURL);
                                    comics.add(comic);
                                }

                                bundle.putParcelableArrayList(ComicsFragment.COMICS, (ArrayList<ComicsInfo>) comics);

                                for (ComicsInfo c : comics) {
                                    Log.d("comicsSearch Array", "onNext() " + c.getName() + " " + c.getImageURL());
                                }
                            }
                        })
        );

    }

    //Performs a detailed search of selected Marvel Hero
    //Uses JSoup to scrape information to the wiki site passed in as a param
    public HashMap<String, String> searchDetail(String wikiUrl) {
        Log.d("adapterclick", "searchDetail: CALLED");
        String bio = null;
        String powers = null;
        String abilities = null;

        HashMap<String, String> map = new HashMap<>();
        try {
            Document doc = Jsoup.connect(wikiUrl).get();

            Elements biobody = doc.select("div#biobody");

            for (Element e : biobody) {
                bio = e.html();
            }

            Elements char_powers_content = doc.select("div#char-powers-content");

            for (Element e : char_powers_content) {
                powers = e.html();
            }

            Elements abilitiesElements = doc.select("div#char-abilities-content");

            for (Element e : abilitiesElements) {
                abilities = e.html();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        map.put("bio", bio);
        map.put("powers", powers);
        map.put("abilities", abilities);

        Log.d("adapterclick", "searchDetail: CALLED");
        return map;
    }

    //Check if Internet Network is active
    private boolean checkNetwork() {
        boolean wifiDataAvailable = false;
        boolean mobileDataAvailable = false;

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                //Connected to wifi
                //Toast.makeText(this, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                wifiDataAvailable = true;
            }

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                //Toast.makeText(this, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                mobileDataAvailable = true;
            }
        }

        return wifiDataAvailable || mobileDataAvailable;

    }
}
