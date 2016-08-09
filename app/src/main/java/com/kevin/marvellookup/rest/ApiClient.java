package com.kevin.marvellookup.rest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kevin on 8/9/2016.
 */
public class ApiClient {
    public static String baseURL = "http://gateway.marvel.com:80/v1/public/";
    public static String publicAPI = "971211f705ea2abfa6a3e139bdeafeed";
    public static String privateAPI = "ce3c2ac5727f98c9093eba74a4224a2d3cf56abe";

    public static String hash;
    public static int min = 1;
    public static int max = 10000;

    public static int TS;

    private static Retrofit retrofit = null;

    public static Retrofit getClient()
    {
        if(retrofit == null)
        {
            Retrofit retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseURL)
                    .build();
        }

        return retrofit;
    }

    public static String getBaseURL() {
        return baseURL;
    }

    public static void setBaseURL(String baseURL) {
        ApiClient.baseURL = baseURL;
    }

    public static String getPublicAPI() {
        return publicAPI;
    }

    public static void setPublicAPI(String publicAPI) {
        ApiClient.publicAPI = publicAPI;
    }

    public static String getPrivateAPI() {
        return privateAPI;
    }

    public static void setPrivateAPI(String privateAPI) {
        ApiClient.privateAPI = privateAPI;
    }

    public static String getHash() {
        return hash;
    }

    public static void setHash(String hash) {
        ApiClient.hash = hash;
    }

    public static int getTS() {
        return TS;
    }

    public static void setTS(int TS) {
        ApiClient.TS = TS;
    }

    public static String md5Hash() {

        Random r = new Random();
        TS = r.nextInt(max - min + 1) + min;

        String combo = TS + privateAPI + publicAPI;
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

        return hash;
    }
}
