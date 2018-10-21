package com.app.securemessaging.view.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.databinding.TransactionQrBinding;

import java.util.ArrayList;

import qrcode.QRCodeManager;

/**
 * Class used to get the transaction id on full screen
 */

public class TransactionQr extends AppCompatActivity implements IAppConstants {

    private TransactionQrBinding mSecureMessagingBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSecureMessagingBinding = DataBindingUtil.setContentView(this, R.layout.transaction_qr);

        getIntentData();

        setListner();
    }

    /**
     * Method is used to set listeners
     */
    private void setListner() {

        mSecureMessagingBinding.toolbar.walletInfoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    /**
     * Method is used to get data from the intent
     */
    private void getIntentData() {

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null &&
                getIntent().getExtras().getString(B2A_SESSIONKEY) != null) {
            setData(getIntent().getExtras().getString(TXID),getIntent().getExtras().getString(B2A_SESSIONKEY));
        }
        else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null){
            setData(getIntent().getExtras().getString(TXID));
        }
    }

    /**
     * Method used to set values in case of B2A
     */
    private void setData(String txId,String sessionKey) {
        mSecureMessagingBinding.toolbar.title.setText(getResources().getString(R.string.B2Asession_key));
        Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(txId + ":" + sessionKey);
        if (bitmap != null) {
            mSecureMessagingBinding.img.setImageBitmap(bitmap);
        }
        mSecureMessagingBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);
      //  mArrImageBean = new ArrayList<>();
    }

    /**
     * Method used to set values in normal message case
     */
    private void setData(String txId) {
        mSecureMessagingBinding.toolbar.title.setText(getResources().getString(R.string.txid));
        Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(txId);
        if (bitmap != null) {
            mSecureMessagingBinding.img.setImageBitmap(bitmap);
        }
        mSecureMessagingBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * To pause the running task.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
