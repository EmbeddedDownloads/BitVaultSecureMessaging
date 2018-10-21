package com.app.securemessaging.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Bean class for storing public key and address
 */

public class PublicKeyModel implements Parcelable {

    private String publicKey;

    public PublicKeyModel(String publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKeyModel() {
    }

    protected PublicKeyModel(Parcel in) {
        publicKey = in.readString();
    }

    public static final Creator<PublicKeyModel> CREATOR = new Creator<PublicKeyModel>() {
        @Override
        public PublicKeyModel createFromParcel(Parcel in) {
            return new PublicKeyModel(in);
        }

        @Override
        public PublicKeyModel[] newArray(int size) {
            return new PublicKeyModel[size];
        }
    };

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(publicKey);
    }

    public static int emptyCounter(ArrayList<PublicKeyModel> publicKeyModelList){
        int counter=0;
        for(int i = 0; i< publicKeyModelList.size(); i++){
            if(publicKeyModelList.get(i).getPublicKey()==null|| publicKeyModelList.get(i).getPublicKey().length()==0)
            {
                counter++;
            }
        }
        return counter;
    }
}
