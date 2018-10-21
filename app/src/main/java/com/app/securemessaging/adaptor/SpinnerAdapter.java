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
 * this class is used to set the adapter for spinner bitcoins Wallet
 */
public class SpinnerAdapter  extends ArrayAdapter implements IAppConstants {
    LayoutInflater flater;
    private List<WalletDetails> selectFilesAdapters;
    private Context context;

    public SpinnerAdapter(Activity context, int resouceId, int textviewId, List<WalletDetails> list) {
        super(context, resouceId, textviewId, list);
        flater = context.getLayoutInflater();
        selectFilesAdapters = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WalletDetails rowItem = selectFilesAdapters.get(position);

        View rowview = flater.inflate(R.layout.spinner_layout, null, true);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        txtTitle.setText(rowItem.getWALLET_NAME().length() > 15 ? rowItem.getWALLET_NAME().substring(0, 15) : rowItem.getWALLET_NAME());

        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);

        if (rowItem != null && rowItem.getWALLET_LAST_UPDATE_BALANCE() != null && rowItem.getWALLET_LAST_UPDATE_BALANCE().length() > 8) {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        } else {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
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

        WalletDetails rowItem = selectFilesAdapters.get(position);

        View rowview = flater.inflate(R.layout.spinner_layout, null, true);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        txtTitle.setText(rowItem.getWALLET_NAME().length() > 20 ? rowItem.getWALLET_NAME().substring(0, 20) : rowItem.getWALLET_NAME());

        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);

        if (rowItem != null && rowItem.getWALLET_LAST_UPDATE_BALANCE() != null && rowItem.getWALLET_LAST_UPDATE_BALANCE().length() > 8) {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        } else {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        }

        return rowview;
    }
}


