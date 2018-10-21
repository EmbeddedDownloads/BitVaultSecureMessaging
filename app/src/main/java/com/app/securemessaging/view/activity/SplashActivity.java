package com.app.securemessaging.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.database.AppPreferences;
import com.app.securemessaging.util.Utils;

import java.util.ArrayList;

import bitmanagers.EOTWalletManager;
import commons.SDKHelper;
import controller.Preferences;
import iclasses.EotCallbacks.EotWalletCallback;
import model.eotmodel.EotWalletDetail;
import utils.SDKUtils;

import static java.security.AccessController.getContext;

/**
 * Splash Image appears
 */
public class SplashActivity extends AppCompatActivity implements  IAppConstants,EotWalletCallback {

    private boolean isFromNotification = false;
    private String mDataHashTxId = "",mReceiverAddress ="",mTag = "";
    private String mContactReceiverAddress = "";
    private EOTWalletManager mEotWalletManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setIntentValues();
        setContentView(R.layout.activity_splash);
        new Preferences().saveData(this,"", SDKHelper.PREFERENCE_RECEIVE_NAME, SDKHelper.PREFERENCE_RECEIVE_KEY);
        checkPermisisonReadContact();
        mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
        mEotWalletManager.getEotWallet(this);
        TextView mVersioCode = (TextView) findViewById(R.id.version_code);

        try {
            String appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if (appVersion != null) {
                mVersioCode.setText(getResources().getString(R.string.version) + " " + appVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

       setHandlerforDelay();

    }

    /**
     * Method is used to set Handler to delay time to move to next screen
     */
    private void setHandlerforDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mSplashActivity = new Intent(SplashActivity.this, HomeActivity.class);
                if(isFromNotification){
                    mSplashActivity.putExtra(FROM_NOTIFICATION,isFromNotification);
                    mSplashActivity.putExtra(IAppConstants.DATA_TAG,mTag);
                    mSplashActivity.putExtra(IAppConstants.TAG_NOTIFICATION_DATA,mDataHashTxId);
                    mSplashActivity.putExtra(IAppConstants.TAG_NOTIFICATION_RECEIVER,mReceiverAddress);
                }
                else if(mContactReceiverAddress != null && !mContactReceiverAddress.equals("")){
                    mSplashActivity.putExtra(IAppConstants.TAG_RECEIVERADD_CONTACT,mContactReceiverAddress);
                }
                startActivity(mSplashActivity);
                finish();
            }
        }, 3000);
    }

    /**
     * Method is used to set intent values
     */
    private void setIntentValues() {
        if(getIntent().getExtras() != null) {
            if(getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_DATA) != null) {
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

    /**
     * Method is used to get run time permisions
     */
      private void checkPermisisonReadContact(){
     if (ContextCompat.checkSelfPermission(SplashActivity.this,
             Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
         new FetchContacts().execute();
     }
 }

    /**
     * * Class is used to fetch contacts from the device
     * */
    private class FetchContacts extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList mContactsModel = Utils.createContactModelFromCursor(Utils.getContactsList(SplashActivity.this), SplashActivity.this);
            if (mContactsModel != null && mContactsModel.size() > 0) {
                AppPreferences.getInstance(SplashActivity.this).setContact(mContactsModel);
                SDKUtils.showLog("Contacts", mContactsModel.size() + " ");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

    @Override
    public void eotWallet(EotWalletDetail mEotWalletDetail) {

    }
}
