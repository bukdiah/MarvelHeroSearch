package com.kevin.marvellookup;

/**
 * Created by Kevin on 8/9/2016.
 */
public class HeroInfo {
    public int getCharId() {
        return charId;
    }

    public void setCharId(int charId) {
        this.charId = charId;
    }

    int charId;
    String name;
    String imageURL;


    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
