package com.kevin.marvellookup;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Kevin on 8/14/2016.
 */
public class ComicsInfo implements Parcelable {
    private String name;
    private String imageURL;

    protected ComicsInfo(Parcel in) {
        name = in.readString();
        imageURL = in.readString();
        iconId = in.readInt();
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    int iconId;

    public ComicsInfo(String name, String imageURL)
    {
        this.name = name;
        this.imageURL = imageURL;
    }

    public ComicsInfo(String name, int icon)
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

    public static ArrayList<ComicsInfo> createContactsList(int numContacts) {
        ArrayList<ComicsInfo> contacts = new ArrayList<ComicsInfo>();

        for (int i = 1; i <= numContacts; i++) {
            contacts.add(new ComicsInfo("Person " + ++lastContactId, R.drawable.reedrichards_medium));
        }

        return contacts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(imageURL);
        parcel.writeInt(iconId);
    }

    public static final Parcelable.Creator<ComicsInfo> CREATOR = new Creator<ComicsInfo>() {
        @Override
        public ComicsInfo createFromParcel(Parcel in) {
            return new ComicsInfo(in);
        }

        @Override
        public ComicsInfo[] newArray(int size) {
            return new ComicsInfo[size];
        }
    };
}
