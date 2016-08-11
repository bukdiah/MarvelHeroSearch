package com.kevin.marvellookup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.marvellookup.adapters.HeroAdapter;
import com.kevin.marvellookup.pojo.CharactersData;
import com.kevin.marvellookup.pojo.Result;
import com.kevin.marvellookup.pojo.Url;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by kevins on 8/9/16.
 */
public class MainActivity extends AppCompatActivity {

    static String baseURL = "http://gateway.marvel.com:80/v1/public/";
    static String publicAPI = "971211f705ea2abfa6a3e139bdeafeed";
    static String privateAPI = "ce3c2ac5727f98c9093eba74a4224a2d3cf56abe";
    HashMap<String,String> detailMap = new HashMap<>();

    //Intent i = new Intent(context,TabLayoutActivity.class);
    //Bundle bundle = new Bundle();

    Context context;
    HeroAdapter adapter;
    RecyclerView recyclerView;

    String hash;
    int min = 1;
    int max = 10000;

    int TS;

    ProgressDialog progress;

    static String id = "";
    static String name = "";
    static String description = "";

    Button btnSearch;
    EditText editName;
    Retrofit retrofit;

    Intent i;
    Bundle bundle;

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

        i = new Intent(this,TabLayoutActivity.class);
        bundle = new Bundle();

        context = this;

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        //adapter = new HeroAdapter(this,getData());

        //adapter.setClickListener(this);
        //recyclerView.setAdapter(adapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST);
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
                searchHero();
                //adapter.setClickListener(this);
            }
        });

    }

    private void searchHero() {

        String nameStartsWith = editName.getText().toString();

        CharacterService characterService = retrofit.create(CharacterService.class);

        if(nameStartsWith!=null) {
            Random r = new Random();
            TS = r.nextInt(max - min + 1) + min;

            Log.d("TimeStamp", Integer.toString(TS));

            md5Hash(TS, privateAPI, publicAPI);

            Log.d("HASH", hash);

            Observable<CharactersData> searchResults = characterService.getSearchResults(nameStartsWith,TS,publicAPI,hash);

            searchResults.subscribeOn(Schedulers.newThread())
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
                            //List of Result objects
                            List<Result> data = charactersData.getData().getResults();
                            List<HeroInfo> heroes = new ArrayList<>();

                            //Create HeroInfo objects outta all of these and put inside a List<HeroInfo>
                            for (Result temp : data)
                            {
                                String name = temp.getName();
                                String imageURL = temp.getThumbnail().getPath()+"/standard_large.jpg";
                                int iconId = temp.getId();

                                HeroInfo current = new HeroInfo();
                                current.name = name;
                                current.imageURL = imageURL;
                                current.iconId = iconId;
                                heroes.add(current);
                            }
                            adapter = new HeroAdapter(context,heroes);
                            adapter.setClickListener(new HeroAdapter.ClickListener() {
                                @Override
                                public void onItemClick(View itemView, int position) {
                                    TextView tvName = (TextView) itemView.findViewById(R.id.tvHero);
                                    String name = tvName.getText().toString();
                                    Log.d("Mainactivity", "onItemClick: YES");
                                    Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();

                                    //Pass String name into a method that calls RxAndroid for another query
                                    adapterClick(name);

                                    //Intent i = new Intent(context,TabLayoutActivity.class);
                                    //Bundle bundle = new Bundle();

                                    //String bio = detailMap.get("bio");
                                    //String powers = detailMap.get("powers");
                                    //String abilities = detailMap.get("abilities");

                                    //Log.d("MAIN", "onItemClick: bio "+bio);
                                    //Log.d("MAIN", "onItemClick: powers "+powers);
                                    //Log.d("MAIN", "onItemClick: abilties "+abilities);
                                    //i.putExtras(bundle);
                                    //startActivity(i);

                                }
                            });
                            recyclerView.setAdapter(adapter);

                        }
                    });
        }
    }

    private void adapterClick(String name)
    {
        Log.d("adapterclick","adapterClick called");

        CharacterService characterDetail = retrofit.create(CharacterService.class);

        if(name != null)
        {
            Log.d("adapterClick", "INSIDE IF STATEMENT");
            Log.d("adapterclick", "name value = "+name);

            Random r = new Random();
            TS = r.nextInt(max - min + 1) + min;

            Log.d("TimeStamp", Integer.toString(TS));

            md5Hash(TS, privateAPI, publicAPI);

            Log.d("HASH", hash);

            Observable<CharactersData> detailSearch = characterDetail.getCharactersData(name,TS,publicAPI,hash);

            detailSearch.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<CharactersData>() {
                            @Override
                            public void onCompleted() {
                                Log.d("AdapterClick", "onCompleted: DONE");
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
                                Log.d("adapterclick", " detailSearch onNext: CALLED");
                                //Intent i = new Intent(context,TabLayoutActivity.class);

                                List<Result> data = charactersData.getData().getResults();
                                List<Url> URLs;

                                for(Result temp : data)
                                {
                                    String desc = temp.getDescription();
                                    URLs = temp.getUrls();

                                    if(desc != null && !desc.isEmpty() && !desc.trim().isEmpty())
                                    {
                                        Log.d("adapterclick", "desc = "+desc);
                                        //bundle.putString("bio",desc);
                                    }
                                    else
                                    {
                                        Log.d("adapterclick", "ELSE clause of deatileSearch");
                                        for(Url u: URLs)
                                        {
                                            if(u.getType().equals("wiki"))
                                            {
                                                final String wiki = u.getUrl();
                                                Log.d("adapterclick", "else clause onNext: URL "+wiki);
                                                Observable<HashMap<String,String>> fetchFromWiki = Observable.create(new Observable.OnSubscribe<HashMap<String, String>>() {
                                                    @Override
                                                    public void call(Subscriber<? super HashMap<String, String>> subscriber) {
                                                        try{
                                                            HashMap<String,String> dataMap = searchDetail(wiki);

                                                            for (String key: dataMap.keySet())
                                                            {
                                                                System.out.println("------------------------------------------------");
                                                                System.out.println("Iterating or looping map using java5 foreach loop");
                                                                System.out.println("key: " + key + " value: " + dataMap.get(key));
                                                            }
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
                                                                Log.d("fetchFromWiki", "call: POOP ");

                                                                bundle.putString("bio",map.get("bio"));
                                                                bundle.putString("powers",map.get("powers"));
                                                                bundle.putString("abilities",map.get("abilities"));

                                                                i.putExtras(bundle);
                                                                startActivity(i);
                                                                //detailMap.putAll(map);

                                                                //String bio = map.get("bio");
                                                                //detailMap.put("bio",bio);

                                                                //Log.d("fetchFromWiki", "bio: "+bio);

                                                                /*
                                                                String bio,powers,abilities;

                                                                for (String key: map.keySet())
                                                                {
                                                                    System.out.println("------------------------------------------------");
                                                                    System.out.println("Inside fetchfromWiki subscribe");
                                                                    System.out.println("key: " + key + " value: " + map.get(key));
                                                                }

                                                                bio = map.get("bio");
                                                                powers = map.get("powers");
                                                                abilities = map.get("abilities");
                                                                */
                                                                //bundle.putString("bio",bio);
                                                                //bundle.putString("powers",powers);
                                                                //bundle.putString("abilities",abilities);
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            }
                        });

        }
    }

    public HashMap<String,String> searchDetail(String wikiUrl)
    {
        Log.d("adapterclick", "searchDetail: CALLED");
        String bio = null;
        String powers = null;
        String abilities = null;

        HashMap<String,String> map = new HashMap<>();
        try {
            Document doc = Jsoup.connect(wikiUrl).get();

            Elements biobody = doc.select("div#biobody");

            for (Element e : biobody) {
                bio = e.html();
            }

            Elements char_powers_content = doc.select("div#char-powers-content");

            for(Element e: char_powers_content)
            {
                powers = e.html();
            }

            Elements abilitiesElements = doc.select("div#char-abilities-content");

            for (Element e: abilitiesElements)
            {
                abilities = e.html();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("searchDetail", "bio: "+bio);
        Log.d("searchDetail", "powers: "+powers);
        Log.d("searchDetail", "abilities: "+abilities);

        map.put("bio",bio);
        map.put("powers",powers);
        map.put("abilities",abilities);

        Log.d("adapterclick", "searchDetail: CALLED");
        return map;
    }

}
