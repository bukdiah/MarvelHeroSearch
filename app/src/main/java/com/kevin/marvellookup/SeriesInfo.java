package com.kevin.marvellookup;

import java.util.ArrayList;

/**
 * Created by Kevin on 8/14/2016.
 */
public class SeriesInfo {
    private String name;
    private String imageURL;

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    int iconId;

    public SeriesInfo (String name, String imageURL)
    {
        this.name = name;
        this.imageURL = imageURL;
    }

    public SeriesInfo (String name, int icon)
    {
        this.name = name;
        this.iconId = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    private static int lastContactId = 0;

    public static ArrayList<SeriesInfo> createContactsList(int numContacts) {
        ArrayList<SeriesInfo> contacts = new ArrayList<SeriesInfo>();

        for (int i = 1; i <= numContacts; i++) {
            contacts.add(new SeriesInfo("Person " + ++lastContactId, R.drawable.reedrichards_medium));
        }

        return contacts;
    }
}
