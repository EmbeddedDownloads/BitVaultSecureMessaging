package com.app.securemessaging.view.activity;

import android.content.Intent;
import android.os.Bundle;

import com.app.securemessaging.appinterface.IAppConstants;
import com.embedded.wallet.BitVaultActivity;

import iclasses.UserAuthenticationCallback;

/**
 * this is authentication activity is loading very first time and after transaticon confirm
 */
public class FingerprintAuthenticationActivity extends BitVaultActivity implements UserAuthenticationCallback, IAppConstants {

    private boolean isFromNotification = false;
    private String mDataHashTxId = "",mReceiverAddress ="",mTag = "";
    private String mContactReceiverAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getExtras() != null) {
            if(getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_DATA) != null){
                isFromNotification = getIntent().getExtras().getBoolean(FROM_NOTIFICATION);
                mDataHashTxId = getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_DATA);
                mReceiverAddress = getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_RECEIVER);
                mTag = getIntent().getExtras().getString(IAppConstants.DATA_TAG);
            }
           else if(getIntent().getExtras().getString(IAppConstants.TAG_RECEIVERADD_CONTACT) != null){
               mContactReceiverAddress = getIntent().getExtras().getString(IAppConstants.TAG_RECEIVERADD_CONTACT);
           }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        validateUser(this, this);
    }

    @Override
    public void onAuthenticationSuccess() {
        Intent splashIntent = new Intent(FingerprintAuthenticationActivity.this, SplashActivity.class);
        if(isFromNotification){
            splashIntent.putExtra(FROM_NOTIFICATION,isFromNotification);
            splashIntent.putExtra(IAppConstants.DATA_TAG,mTag);
            splashIntent.putExtra(IAppConstants.TAG_NOTIFICATION_DATA,mDataHashTxId);
            splashIntent.putExtra(IAppConstants.TAG_NOTIFICATION_RECEIVER,mReceiverAddress);
        }
        else if(mContactReceiverAddress != null && !mContactReceiverAddress.equals("")){
            splashIntent.putExtra(IAppConstants.TAG_RECEIVERADD_CONTACT,mContactReceiverAddress);
        }
        startActivity(splashIntent);
        finish();
    }
}


