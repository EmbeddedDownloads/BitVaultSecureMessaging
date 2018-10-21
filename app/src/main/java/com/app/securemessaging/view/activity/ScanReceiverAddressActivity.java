package com.app.securemessaging.view.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.databinding.ActivityScanReceiverAddressBinding;

import qrcode.QRCodeManager;

public class ScanReceiverAddressActivity extends AppCompatActivity implements IAppConstants{
    private ActivityScanReceiverAddressBinding mActivityScanReceiverAddressBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityScanReceiverAddressBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_receiver_address);
        setListner();
        getIntentData();

    }

    /**
     * Method is used to set listeners
     */
    private void setListner() {

        mActivityScanReceiverAddressBinding.toolbar.walletInfoBack.setOnClickListener(new View.OnClickListener() {
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

        if (getIntent() != null && getIntent().getStringExtra(QR_WALLET_ADDRESS) != null) {
            setData(getIntent().getStringExtra(QR_WALLET_ADDRESS));
        }

    }

    /**
     * Method used to set qr code to image
     */
    private void setData(String selectedReceiverAdd) {
        mActivityScanReceiverAddressBinding.toolbar.title.setText(getResources().getString(R.string.WalletAdd));
        Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(selectedReceiverAdd);
        if (bitmap != null) {
            mActivityScanReceiverAddressBinding.img.setImageBitmap(bitmap);
        }
        mActivityScanReceiverAddressBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);
    }

}
