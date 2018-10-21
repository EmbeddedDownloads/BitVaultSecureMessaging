package com.app.securemessaging.adaptor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.util.Utils;

import java.util.List;

import model.WalletDetails;

/**
 * this class is used to set the adapter for all wallet
 */

public class SpinnerAdapterAll extends ArrayAdapter implements IAppConstants {
    LayoutInflater flater;
    private List<WalletDetails> selectFilesAdapters;
    private Context mContext;
    private double mEotcoinsBal = 0.0;

    public SpinnerAdapterAll(Activity context, int resouceId, int textviewId, List<WalletDetails> list,double eotcoins) {
        super(context, resouceId, textviewId, list);
        this.flater = context.getLayoutInflater();
        this.selectFilesAdapters = list;
        this.mEotcoinsBal = eotcoins;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return selectFilesAdapters.size() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowview = flater.inflate(R.layout.spinner_layout, null, true);
        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);

        if(position == 0){
            txtTitle.setText(mContext.getString(R.string.wallet_eotS));
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(mEotcoinsBal) + " " + mContext.getResources().getString(R.string.wallet_eot));
        }else {
            WalletDetails rowItem = selectFilesAdapters.get(position - 1);
            txtTitle.setText(rowItem.getWALLET_NAME().length() > 15 ? rowItem.getWALLET_NAME().substring(0, 15) : rowItem.getWALLET_NAME());

            if (rowItem != null && rowItem.getWALLET_LAST_UPDATE_BALANCE() != null && rowItem.getWALLET_LAST_UPDATE_BALANCE().length() > 8) {
                mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
            } else {
                mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
            }
        }

        return rowview;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    /**
     * Method to get custom view
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        View rowview = flater.inflate(R.layout.spinner_layout, null, true);
        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);

        if(position == 0){
            txtTitle.setText(mContext.getString(R.string.wallet_eotS));
            mwalletBalance.setText(" " + Utils.convertDecimalFormatPattern(mEotcoinsBal) + mContext.getResources().getString(R.string.wallet_eot));
        }else {
            WalletDetails rowItem = selectFilesAdapters.get(position - 1);
            txtTitle.setText(rowItem.getWALLET_NAME().length() > 15 ? rowItem.getWALLET_NAME().substring(0, 15) : rowItem.getWALLET_NAME());

            if (rowItem != null && rowItem.getWALLET_LAST_UPDATE_BALANCE() != null && rowItem.getWALLET_LAST_UPDATE_BALANCE().length() > 8) {
                mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
            } else {
                mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
            }
        }


        return rowview;
    }
}


