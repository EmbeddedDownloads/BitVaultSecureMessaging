package com.app.securemessaging.view.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.AutoCompleteAdapter;
import com.app.securemessaging.adaptor.ShowImageAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.database.AppPreferences;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.CreateMessageBitattachBinding;
import com.app.securemessaging.util.Utils;

import java.io.File;
import java.util.ArrayList;

import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import database.DatabaseHandler;
import model.PushDataModel;
import model.WalletDetails;
import qrcode.ScanQRCode;
import utils.SDKUtils;

/**
 * This class is used to create message screen related to bit-attach application
 */

public class CreateMessageWithBitAttach extends AppCompatActivity implements View.OnClickListener, IAppConstants {

    private final int REQUEST_SCAN_PRIVATE_KEY = 500,REQUEST_SCAN_SESSIONKEY = 600;
    private CreateMessageBitattachBinding mCreateMessageBitAttach;
    private String mScannedResultSessionKey = "";
    private WalletDetails mSelectedWalletDetails;
    private boolean isSavedInDb = false;
    private long rowId = 0;
    private static final int MY_PERMISSIONS_CONTACT = 101;
    private ArrayList<ContactsModel> mContactsModel;
    private AutoCompleteAdapter mAdapter;
    private double mBitAttachSize = 0;
    private DatabaseHandler mDatabaseHandler;
    private PushDataModel mPushDataModel = null;
    private String mWalletType = "";
    private String mEotWalletAddress = "";
    private double mEotCoinsTotal = 0.0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCreateMessageBitAttach = DataBindingUtil.setContentView(this, R.layout.create_message_bitattach);

        setListner();

        getDataFromIntent();

        setData();

    }

    /**
     * Method used to get data from intent
     */
    private void getDataFromIntent() {

        if (getIntent() != null && getIntent().getStringExtra(FROM) != null) {
            mWalletType = getIntent().getStringExtra(WALLET_TYPE);
            getData(getIntent().getStringExtra(FROM));
        }
    }
    /**
     * get data from intent
     *
     * @param stringExtra
     */
    private void getData(String stringExtra) {

        switch (stringExtra) {

            case WALLET:
                setWalletData();
                break;

            case SENT:
                setWalletData();
                if (getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                    MessageBeanHelper mMessagebeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);
                    mScannedResultSessionKey = mMessagebeanHelper.getMessage();
                    mCreateMessageBitAttach.receiverAddress.setText(mMessagebeanHelper.getReceiverAddress().toString());
                    mCreateMessageBitAttach.receiverAddress.setSelection(mMessagebeanHelper.getReceiverAddress().toString().length());

                }
                break;

            case REPLY:
                setWalletData();
                    mCreateMessageBitAttach.receiverAddress.setText(getIntent().getExtras().getString(RECEIVER_ADDRESS));
                    mCreateMessageBitAttach.receiverAddress.setSelection(getIntent().getExtras().getString(RECEIVER_ADDRESS).toString().length());
                break;
        }
    }


    /**
     * method to set wallet data
     */

    private void setWalletData(){
        if (mWalletType != null && mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
            if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                mEotCoinsTotal = getIntent().getDoubleExtra(EOT_WALLET_BAL, 0.0);
            }
        }
        else {
            if (getIntent() != null && getIntent().getExtras() != null && getIntent().getSerializableExtra(WALLET_DETAILS) != null) {
                mSelectedWalletDetails = (WalletDetails) getIntent().getExtras().getSerializable(WALLET_DETAILS);
            }
        }
    }
    /**
     * Method is used to set data
     */
    private void setData() {

        mCreateMessageBitAttach.toolbar.title.setText(getResources().getString(R.string.create_message_with_bitattach));
        mCreateMessageBitAttach.toolbar.deleteIcon.setText(getResources().getString(R.string.m));
        mContactsModel = new ArrayList<>();

        mContactsModel = AppPreferences.getInstance(CreateMessageWithBitAttach.this).getContact();
        if(mContactsModel != null && mContactsModel.size() > 0){
        }
        else{
            askForPermission();
        }
    }

    /**
     * Method used to set view listeners
     */
    private void setListner() {
        mCreateMessageBitAttach.toolbar.deleteIcon.setOnClickListener(this);
        mCreateMessageBitAttach.toolbar.walletInfoBack.setOnClickListener(this);
        mCreateMessageBitAttach.qrScanner.setOnClickListener(this);
        mCreateMessageBitAttach.scanButton.setOnClickListener(this);
        mCreateMessageBitAttach.saveContact.setOnClickListener(this);

        mCreateMessageBitAttach.receiverAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (s.toString().length() >= 1) {
                        mCreateMessageBitAttach.saveContact.setVisibility(View.VISIBLE);
                        filterMatchedData(s.toString());
                    }else{
                        mCreateMessageBitAttach.saveContact.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * total size of the message
             * @param s
             */
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    /**
     * Method used to match data from contacts
     *
     * @param keyword
     */
    private void filterMatchedData(String keyword) {

        try {

            if (mContactsModel != null && mContactsModel.size() > 0) {

                if (mCreateMessageBitAttach != null) {

                    if (mAdapter != null) {
                        mCreateMessageBitAttach.receiverAddress.setAdapter(mAdapter);
                        mCreateMessageBitAttach.receiverAddress.setThreshold(1);
                    } else {
                        mAdapter = new AutoCompleteAdapter(this, R.layout.spinner_autocomplete_layout, mContactsModel, 2);
                        mCreateMessageBitAttach.receiverAddress.setAdapter(mAdapter);
                        mCreateMessageBitAttach.receiverAddress.setThreshold(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Methods to use selected contact number in the field
     *
     * @param mContactsModel -- contacts object
     */
    public void setSelectedContact(ContactsModel mContactsModel) {
        try {
            if(mContactsModel.getmReceiverAddress() != null && !mContactsModel.getmReceiverAddress().equalsIgnoreCase("")) {
                mCreateMessageBitAttach.receiverAddress.setText(mContactsModel.getmReceiverAddress());
                mCreateMessageBitAttach.receiverAddress.setSelection(mContactsModel.getmReceiverAddress().toString().length());
                mCreateMessageBitAttach.receiverAddress.dismissDropDown();
            }
            else{
                mCreateMessageBitAttach.receiverAddress.dismissDropDown();
                Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.no_receiver_addresss), Snackbar.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View mView) {
        switch (mView.getId()) {
            case R.id.delete_icon:
                if (Utils.isNetworkConnected(this)) {

                    if (!mCreateMessageBitAttach.receiverAddress.getText().toString().trim().equals("")) {
                        if(mCreateMessageBitAttach.receiverAddress.getText().toString().trim().length() == 34){

                            if (mScannedResultSessionKey != null && !mScannedResultSessionKey.equals("")) {
                                       if(mBitAttachSize <= 0){
                                           Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.bitattachSize), Snackbar.LENGTH_SHORT);
                                       }
                                       else{
                                           if (getIntent().getExtras().getString(FROM).equals(SENT)) {
                                               MessageBeanHelper mMessagebeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);

                                               if (mMessagebeanHelper.getId() != null) {
                                                   rowId = Long.parseLong(mMessagebeanHelper.getId());
                                               }

                                           } else {
                                               rowId = insertValuesInDB();
                                           }

                                           if (rowId > 0) {
                                               isSavedInDb = true;
                                           }


                                           Intent WalletIntent = new Intent(this, WalletInformation.class);
                                           Bundle mBundle = null;

                                           if (getIntent().getExtras() != null) {
                                               mBundle = getIntent().getExtras();
                                           }

                                           mBundle.putString(RECEIVER_ADDRESS, mCreateMessageBitAttach.receiverAddress.getText().toString().trim());
                                           mBundle.putString(MESSAGE, mScannedResultSessionKey);
                                           mBundle.putString(FROM, BITATTACH);
                                           mBundle.putDouble(BitAtachSize,mBitAttachSize);

                                           mBundle.putString(ROW_ID, rowId + "");
                                           mBundle.putString(WALLET_TYPE,mWalletType);
                                           mBundle.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                                           mBundle.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                                           WalletIntent.putExtras(mBundle);
                                           startActivity(WalletIntent);
                                       }
                            }
                            else {
                                Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.sessionkey_should_not_be_empty), Snackbar.LENGTH_SHORT);
                            }
                        }
                        else {
                            Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.receiver_addresss_invalid), Snackbar.LENGTH_SHORT);

                        }

                    }
                    else {
                        Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.receiver_addresss_not_empty), Snackbar.LENGTH_SHORT);
                    }

                } else {

                    Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }

                break;

            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.qr_scanner:
                startActivityForResult(new Intent(this, ScanQRCode.class), REQUEST_SCAN_PRIVATE_KEY);
                break;

            case R.id.scan_button:
                startActivityForResult(new Intent(this, ScanQRCode.class), REQUEST_SCAN_SESSIONKEY);
                break;

            case R.id.saveContact:
                if(!mCreateMessageBitAttach.receiverAddress.getText().toString().trim().equals("")
                        && mCreateMessageBitAttach.receiverAddress.getText().toString().trim().length() == RECEIVER_ADDRESS_CHARS){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.embedded.contacts");
                    if (launchIntent != null) {
                        launchIntent.putExtra(CONTACT_SAVE,mCreateMessageBitAttach.receiverAddress.getText().toString().trim());
                        startActivity(launchIntent);
                    }
                }else{
                    Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.receiver_addresss_invalid), Snackbar.LENGTH_SHORT);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_SCAN_PRIVATE_KEY && data != null) {
            String scannedResult = data.getStringExtra("data");
            scannedResult = scannedResult.replace("bitcoin:", "");
            mCreateMessageBitAttach.receiverAddress.setText(scannedResult);
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_SCAN_SESSIONKEY && data != null){
            if(data.getStringExtra("data").contains(":")) {
                String[] parts =  data.getStringExtra("data").split(":");
                if(getDeviceToken() != null && parts[0] != null) {
                    try {
                        mScannedResultSessionKey = Utils.getDecryptedSessionKey(parts[0], getDeviceToken());
                        mBitAttachSize = Double.parseDouble(parts[1]);
                        mCreateMessageBitAttach.bitachSessionKey.setText(getResources().getString(R.string.session_key) +"  " + mScannedResultSessionKey);
                        Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.scan_success_msg), Snackbar.LENGTH_SHORT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.scan_fail_msg), Snackbar.LENGTH_SHORT);
                    }
                }else{
                    Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.scan_fail_msg), Snackbar.LENGTH_SHORT);
                }
            }
            else{
                Utils.showSnakbar(mCreateMessageBitAttach.parentLayout, getResources().getString(R.string.scan_fail_msg), Snackbar.LENGTH_SHORT);
            }
        }
    }
    /**
     * Method is used to insert values in database
     */
    public long insertValuesInDB() {

        try {
            if (!getIntent().getExtras().getString(FROM).equals(SENT) && !isSavedInDb) {
                if (!mCreateMessageBitAttach.receiverAddress.getText().toString().trim().equals("")) {

                    if (mScannedResultSessionKey != null && !mScannedResultSessionKey.equals("")) {

                        CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(this);

                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(ContractClass.FeedEntry.MESSAGE, mScannedResultSessionKey);
                        mContentValues.put(ContractClass.FeedEntry.RECEIVER_ADDRESS, mCreateMessageBitAttach.receiverAddress.getText().toString());
                        mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mSelectedWalletDetails.getWALLET_ID());
                        mContentValues.put(ContractClass.FeedEntry.TYPE, DRAFT);
                        mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
                        mContentValues.put(ContractClass.FeedEntry.STATUS, PENDING);
                        mContentValues.put(ContractClass.FeedEntry.IS_NEW, FALSE);
                        mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITATTACH_MESSAGE);
                        mContentValues.put(ContractClass.FeedEntry.WALLETTYPE, mWalletType);

                        if (mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                            mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mEotWalletAddress);
                        }else{
                            mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mSelectedWalletDetails.getWALLET_ID());
                        }

                        return dbOperation.InsertIntoDB(ContractClass.FeedEntry.TABLE_NAME, mContentValues);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }


    @Override
    protected void onStop() {
        super.onStop();
        insertValuesInDB();
    }

    /**
     * open the previous activity on press of back button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * method is used to get permission to read the contacts
     */
    private void askForPermission(){
        if (ContextCompat.checkSelfPermission(CreateMessageWithBitAttach.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Utils.allocateRunTimePermissions(CreateMessageWithBitAttach.this, new String[]{Manifest.permission.READ_CONTACTS},
                    new int[]{MY_PERMISSIONS_CONTACT});
        }
    }

    /**   Method to get callback of permission
     * * @param requestCode
     * * @param permissions
     * * @param grantResults
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_CONTACT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new FetchContacts().execute();
                    return;
                }
            }
        }
    }
    /**
     *  * Class is used to fetch contacts from the device
     **/
    private class FetchContacts extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Void doInBackground(Void... params) {
            mContactsModel = Utils.createContactModelFromCursor(Utils.getContactsList(CreateMessageWithBitAttach.this), CreateMessageWithBitAttach.this);
            if (mContactsModel != null && mContactsModel.size() > 0) {
                AppPreferences.getInstance(CreateMessageWithBitAttach.this).setContact(mContactsModel);
                SDKUtils.showLog("Contacts", mContactsModel.size() + " ");
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     ** Method is used to fetch deviceToken to decrypt the session Key
     **/
    private String getDeviceToken() {
        String token = null;
        mDatabaseHandler = new DatabaseHandler(CreateMessageWithBitAttach.this);
        if (mDatabaseHandler != null) {
            mPushDataModel = new PushDataModel();
            mPushDataModel = mDatabaseHandler.getPushData();
            if (mPushDataModel != null && mPushDataModel.getDeviceToken() != null &&
                    !mPushDataModel.getDeviceToken().equalsIgnoreCase("")) {
                token =  mPushDataModel.getDeviceToken();
            }
        }
        return token;
    }
}
