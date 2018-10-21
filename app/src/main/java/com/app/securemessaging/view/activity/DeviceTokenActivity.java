package com.app.securemessaging.view.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.databinding.ActivityDeviceTokenBinding;
import com.app.securemessaging.util.Utils;

import database.DatabaseHandler;
import model.PushDataModel;
import qrcode.QRCodeManager;

public class DeviceTokenActivity extends AppCompatActivity  implements View.OnClickListener, IAppConstants {
    private ActivityDeviceTokenBinding mActivityDeviceTokenBinding;
    private DatabaseHandler mDatabaseHandler;
    private PushDataModel mPushDataModel = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityDeviceTokenBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_token);
        mActivityDeviceTokenBinding.toolbar.title.setText(getResources().getString(R.string.device_token_title));
        mActivityDeviceTokenBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);
        setListner();
        setQrImage();

    }

    /**
     * Method used to generate and set QR Image
     */
    private void setQrImage() {
        mDatabaseHandler = new DatabaseHandler(DeviceTokenActivity.this);
        if(mDatabaseHandler != null){
            mPushDataModel = new PushDataModel();
            mPushDataModel = mDatabaseHandler.getPushData();
            if(mPushDataModel != null && mPushDataModel.getDeviceToken() != null &&
                    !mPushDataModel.getDeviceToken().equalsIgnoreCase("")){
                String token = mPushDataModel.getDeviceToken();
                Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(token);
                if (bitmap != null) {
                    mActivityDeviceTokenBinding.qrcode.setImageBitmap(bitmap);
                    SetViewVisibility(true);
                }
                else{
                    SetViewVisibility(false);
                }
            }
            else{
                SetViewVisibility(false);
            }
        }
        else{
            SetViewVisibility(false);

        }

    }

    /**
     * Method used to set view Visibility
     */
    private void SetViewVisibility(boolean isToken)
    {
        if(!isToken){
            mActivityDeviceTokenBinding.buttomTxt.setVisibility(View.GONE);
            mActivityDeviceTokenBinding.qrcode.setVisibility(View.GONE);
            mActivityDeviceTokenBinding.txtNoDeviceToken.setVisibility(View.VISIBLE);
            mActivityDeviceTokenBinding.retryButton.setVisibility(View.VISIBLE);
            mActivityDeviceTokenBinding.txtNoDeviceToken.setText(getResources().getString(R.string.wrong_token));
            Utils.showSnakbar(mActivityDeviceTokenBinding.rlMain, getResources().getString(R.string.wrong_token), Snackbar.LENGTH_SHORT);
        }

        else{
            mActivityDeviceTokenBinding.qrcode.setVisibility(View.VISIBLE);
            mActivityDeviceTokenBinding.buttomTxt.setVisibility(View.VISIBLE);
            mActivityDeviceTokenBinding.txtNoDeviceToken.setVisibility(View.GONE);
            mActivityDeviceTokenBinding.retryButton.setVisibility(View.GONE);

        }
    }


    /**
     * Initialize click listener
     */

    private void setListner() {

        mActivityDeviceTokenBinding.toolbar.walletInfoBack.setOnClickListener(this);
        mActivityDeviceTokenBinding.retryButton.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.retry_button:
                setQrImage();
                break;

        }
    }
}
