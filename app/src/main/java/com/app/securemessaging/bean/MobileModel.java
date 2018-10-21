package com.app.securemessaging.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created Dheeraj Banal root on 15/6/17.
 * version 1.0.0
 * Contains data about mobile number and its type
 */

public class MobileModel implements Parcelable {
    public static final Creator<MobileModel> CREATOR = new Creator<MobileModel>() {
        @Override
        public MobileModel createFromParcel(Parcel in) {
            return new MobileModel(in);
        }

        @Override
        public MobileModel[] newArray(int size) {
            return new MobileModel[size];
        }
    };

    private String number;
    private String numberType;

    public MobileModel() {

    }

    public MobileModel(String number, String numberType) {
        this.number = number;
        this.numberType = numberType;
    }

    private MobileModel(Parcel in) {
        number = in.readString();
        numberType = in.readString();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumberType() {
        return numberType;
    }

    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(number);
        parcel.writeString(numberType);
    }

    public static int emptyCounter(ArrayList<MobileModel> mobileModelList){
        int counter=0;
        for(int i = 0; i< mobileModelList.size(); i++){
            if(mobileModelList.get(i).getNumber()==null|| mobileModelList.get(i).getNumber().length()==0){
                counter++;
            }
        }
        return counter;
    }
}