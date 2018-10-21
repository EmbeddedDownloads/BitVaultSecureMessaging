package com.app.securemessaging.view.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.SecuremessagingBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.util.ZipUnZipFile;


import java.io.File;
import java.util.ArrayList;

import bitmanagers.BitVaultDataManager;
import iclasses.SendMessageCallback;
import model.DataMoverModel;
import model.WalletDetails;

/**
 * Message Sending Successful Message.
 */

public class SendingActivity extends AppCompatActivity implements IAppConstants, SendMessageCallback {

    private SecuremessagingBinding mSecureMessagingBinding;
    private Handler mHandler;
    private Runnable mRunnable;
    private ArrayList<ShowImageBean> mArrImageBean;
    private String mReceiverAddress = "", mMessageType = "", mMessage = "";
    private int mCharacter_count = 0;
    private WalletDetails mSelectedWallet = null;
    private BitVaultDataManager mDataMover;
    private DataMoverModel mDataMoverModel;
    private ProgressBar progress;
    private String mBitAttachMessageKey = "",mBitAttachMessageKeyFileLoc = "";
    private long mBitAttachSize = 0;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + SECURE_MESSAGE_PATH + TEXT_MESSAGE;
    private String rootPathZip = Environment.getExternalStorageDirectory().getAbsolutePath() + SECURE_MESSAGE_PATH + SECURE_MESSAGE_ZIP;
    private int animationDuration = 5000;
    private int timerDurationHandler = 2000;
    private int bufferSize = 8192;
    private boolean isOnForeGround = false;
    private String sendMessageType = "",statusMessage = "",sendMessagetxId = "",sendSuccessFailed = "";
    private String mWalletType = "";
    private String mEotWalletAddress = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOnForeGround = true;
        mSecureMessagingBinding = DataBindingUtil.setContentView(this, R.layout.securemessaging);
        setData();

        getIntentData();
        setAnimation();
    }
   /**
    * * Method is used to get data from the intent
    */

   private void setAnimation(){
       mHandler = new Handler();
       mRunnable = new Runnable() {
           @Override
           public void run() {
               double n = 10.0;

               ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", 0, (int) (n * 100));
               animation.setDuration(animationDuration); //in milliseconds
               animation.start();

               mHandler.postDelayed(this, animationDuration);
           }
       };

       progress = (ProgressBar) findViewById(R.id.circle_progress_bar);

       mHandler.postDelayed(mRunnable, 500);
   }
    /**
     * Method is used to get data from the intent
     */
    private void getIntentData() {

        if (getIntent() != null && getIntent().getExtras() != null) {
            mWalletType = getIntent().getStringExtra(WALLET_TYPE);
            getDataFromIntentInAllCase(getIntent().getExtras().getString(FROM));
        }
    }

    /**
     * Method used to set values
     */
    private void setData() {
        mSecureMessagingBinding.toolbar.title.setText(getResources().getString(R.string.sending));
        mSecureMessagingBinding.toolbar.walletInfoBack.setVisibility(View.GONE);
        mSecureMessagingBinding.txtSecureMsg.setText(getResources().getString(R.string.secure_msg_sending));
        mSecureMessagingBinding.toolbar.deleteIcon.setVisibility(View.GONE);
        mArrImageBean = new ArrayList<>();

        mDataMover = BitVaultDataManager.getSecureMessangerInstance();
        mDataMoverModel = new DataMoverModel();
    }

    @Override
    public void onBackPressed() {
        showConfirmDialog();
    }

    /**
     * To pause the running task.
     */
    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Method will evaluate date according to message type
     *
     * @param mMessagetype -- type of the message it is
     */
    public void getDataFromIntentInAllCase(final String mMessagetype) {
        switch (mMessagetype) {
            case BITVAULT:
                if (getIntent() != null && getIntent().getExtras() != null ) {
                    Bundle mBundleData = getIntent().getExtras();
                        if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                            if (getIntent().getExtras().getSerializable(WALLET_DETAILS) != null) {
                                mSelectedWallet = (WalletDetails) mBundleData.getSerializable(WALLET_DETAILS);
                            }
                        } else {
                            if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                                mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                            }
                    }
                    mReceiverAddress = mBundleData.getString(RECEIVER_ADDRESS);
                    mArrImageBean = (ArrayList<ShowImageBean>) mBundleData.getSerializable(SELECTED_LIST);
                    mCharacter_count = mBundleData.getInt(CHARACTER_COUNT);
                    mMessage = mBundleData.getString(MESSAGE);
                            if (Utils.isNetworkConnected(SendingActivity.this)) {
                                new AsynTaskSendMessage(BITVAULT).execute();
                            } else {
                                Utils.showSnakbar(mSecureMessagingBinding.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                            }
                }

                break;

            case BITATTACH:
                if (getIntent() != null && getIntent().getExtras() != null) {
                    Bundle mBundleData = getIntent().getExtras();

                    if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        if(getIntent().getExtras().getSerializable(WALLET_DETAILS) != null) {
                            mSelectedWallet = (WalletDetails) mBundleData.getSerializable(WALLET_DETAILS);
                        }
                    }else{
                        if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                            mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                        }
                    }
                    mReceiverAddress = mBundleData.getString(RECEIVER_ADDRESS);
                    mBitAttachMessageKey = mBundleData.getString(BitAttachSessionKey);
                    mBitAttachSize = mBundleData.getLong(BitAtachSize);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Utils.isNetworkConnected(SendingActivity.this)) {
                                if (mSelectedWallet != null && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null) {
                                        mDataMover.sendMessage(Integer.parseInt(mSelectedWallet.getWALLET_ID()), mBitAttachSize, setUpDataMoverModelToBitAttach(),mBitAttachMessageKeyFileLoc,
                                                SendingActivity.this, getIntent().getExtras().getString(TXID),getResources().getString(R.string.webserver_key_value),BITATTACH,mEotWalletAddress,mWalletType);
                                    } else {
                                        mDataMover.sendMessage(Integer.parseInt(mSelectedWallet.getWALLET_ID()), mBitAttachSize, setUpDataMoverModelToBitAttach(),mBitAttachMessageKeyFileLoc,
                                                SendingActivity.this, "",getResources().getString(R.string.webserver_key_value),BITATTACH,mEotWalletAddress,mWalletType);
                                    }
                                }else{
                                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null) {
                                        mDataMover.sendMessage(-1, mBitAttachSize, setUpDataMoverModelToBitAttach(),mBitAttachMessageKeyFileLoc,
                                                SendingActivity.this, getIntent().getExtras().getString(TXID),getResources().getString(R.string.webserver_key_value),BITATTACH,mEotWalletAddress,mWalletType);
                                    } else {
                                        mDataMover.sendMessage(-1, mBitAttachSize, setUpDataMoverModelToBitAttach(),mBitAttachMessageKeyFileLoc,
                                                SendingActivity.this, "",getResources().getString(R.string.webserver_key_value),BITATTACH,mEotWalletAddress,mWalletType);
                                    }
                                }
                            } else {
                                Utils.showSnakbar(mSecureMessagingBinding.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                            }
                        }
                    }, timerDurationHandler);
                }
                break;

            case BITVAULTATTACH:

                if (getIntent() != null && getIntent().getExtras() != null) {
                    Bundle mBundleData = getIntent().getExtras();
                    if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        if(getIntent().getExtras().getSerializable(WALLET_DETAILS) != null) {
                            mSelectedWallet = (WalletDetails) mBundleData.getSerializable(WALLET_DETAILS);
                        }
                    }else{
                        if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                            mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                        }
                    }
                    mReceiverAddress = mBundleData.getString(RECEIVER_ADDRESS);
                    mArrImageBean = (ArrayList<ShowImageBean>) mBundleData.getSerializable(SELECTED_LIST);
                    mCharacter_count = mBundleData.getInt(CHARACTER_COUNT);
                    mMessage = mBundleData.getString(MESSAGE);

                            if (Utils.isNetworkConnected(SendingActivity.this)) {
                                new AsynTaskSendMessage(BITVAULTATTACH).execute();
                            } else {
                                Utils.showSnakbar(mSecureMessagingBinding.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                            }

                }
                break;
        }
    }
    /**
     * Method to calculate the size of the attachments
     *
     * @return -- calculated size in KB
     */
    private long calculateSize() {

        long mSize = 0;

        if (mArrImageBean != null && mArrImageBean.size() > 0) {
            for (int fileSize = 0; fileSize < mArrImageBean.size(); fileSize++) {
                mSize += mArrImageBean.get(fileSize).getFileSize();
            }
        }

        if (mSize != 0) {
            mSize = mSize / 1024;
        } else {
            mSize = 0;
        }

        return mSize;

    }

    /**
     * Method to create data to send for BitVault to BitVault case
     *
     * @return
     */
    private DataMoverModel setUpDataMoverModel(final String msgCase) {
        if (mDataMoverModel == null) {
            mDataMoverModel = new DataMoverModel();
        }
        if (mDataMoverModel != null) {
            if(msgCase != null && msgCase.equalsIgnoreCase(BITVAULT)){
                mDataMoverModel.setMessage_tag(SECURE_MESSAGE);
            }else{
                mDataMoverModel.setMessage_tag(B2A_FILE);
            }
            mDataMoverModel.setPbcId(PBC_ID);
            mDataMoverModel.setReceiverAddress(mReceiverAddress.toString().trim());

            if (mArrImageBean != null && mArrImageBean.size() > 0) {
                int arraySize = 0;
                if (mMessage != null && !mMessage.equalsIgnoreCase("")) {
                    arraySize = mArrImageBean.size() + 1;
                } else{
                    arraySize = mArrImageBean.size();
                }
                String[] arrayInputFiles =  new String[arraySize];
                for (int i = 0; i < mArrImageBean.size(); i++) {
                    arrayInputFiles[i] = mArrImageBean.get(i).getImage();

                }
                if (mMessage != null && !mMessage.equalsIgnoreCase("")) {
                    Utils.writeToFile(mMessage.getBytes(),rootPath,bufferSize);
                    arrayInputFiles[mArrImageBean.size()] = rootPath;
                }

                boolean isZipTrue =  ZipUnZipFile.zip(arrayInputFiles,rootPathZip);
                if(isZipTrue){
                    mDataMoverModel.setMessageFiles(rootPathZip);
                }
            }
            else{
                if(mMessage != null && !mMessage.equalsIgnoreCase("")){
                    String[] arrayInputFiles =  new String[1];
                    Utils.writeToFile(mMessage.getBytes(),rootPath,bufferSize);
                    arrayInputFiles[0] = rootPath;

                    boolean isZipTrue =  ZipUnZipFile.zip(arrayInputFiles,rootPathZip);
                    if(isZipTrue){
                        mDataMoverModel.setMessageFiles(rootPathZip);
                    }
                }
            }

        }

        return mDataMoverModel;
    }
    /**
     * Method to create data to send for case of desktop to desktop
     *
     * @return
     */
    private DataMoverModel setUpDataMoverModelToBitAttach() {
        if (mDataMoverModel == null) {
            mDataMoverModel = new DataMoverModel();
        }

        if (mDataMoverModel != null) {
            mDataMoverModel.setMessage_tag(A2A_FILE);
            mDataMoverModel.setPbcId(PBC_ID);
            mDataMoverModel.setReceiverAddress(mReceiverAddress.toString().trim());

            if (mBitAttachMessageKey != null && !mBitAttachMessageKey.equalsIgnoreCase("")) {
                String[] arrayInputFiles = new String[1];
                Utils.writeToFile(mBitAttachMessageKey.getBytes(), rootPath, bufferSize);
                arrayInputFiles[0] = rootPath;
                boolean isZipTrue = ZipUnZipFile.zip(arrayInputFiles, rootPathZip);
                if (isZipTrue) {
                    mBitAttachMessageKeyFileLoc = rootPathZip;
                }
            }
        }

        return mDataMoverModel;
    }

    @Override
    public void sendMessageCallback(String status, String message, String txId,String messageType,String sessionKey) {

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        sendMessagetxId = txId;
        statusMessage = message;
        sendMessageType = messageType;
        if (status.equalsIgnoreCase(STATUS_SUCCESS)
                || status.equalsIgnoreCase(STATUS_OK)
                || message.equalsIgnoreCase(MESSAGE_SEND_SUCCESS)
                ) {

            ContentValues mContentValues = new ContentValues();
            mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
            mContentValues.put(ContractClass.FeedEntry.TYPE, SENT);
            mContentValues.put(ContractClass.FeedEntry.TXD_ID, txId);
            mContentValues.put(ContractClass.FeedEntry.SESSIONKEY,sessionKey);
            new CRUD_OperationOfDB(this).UpdateDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?", new String[]{getIntent().getExtras().getString(ROW_ID)}, mContentValues);

            if(isOnForeGround) {
               callSuccessActivity();
            }else{
                sendSuccessFailed = getResources().getString(R.string.sent_success);
                mSecureMessagingBinding.txtSecureMsg.setText("");
                progress.setVisibility(View.GONE);
            }

        } else {
            Utils.showSnakbar(mSecureMessagingBinding.parentLayout, message, Snackbar.LENGTH_SHORT);
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
            mContentValues.put(ContractClass.FeedEntry.TYPE, FAIL);
            mContentValues.put(ContractClass.FeedEntry.TXD_ID, txId);
            mContentValues.put(ContractClass.FeedEntry.SESSIONKEY,sessionKey);
            new CRUD_OperationOfDB(this).UpdateDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?", new String[]{getIntent().getExtras().getString(ROW_ID)}, mContentValues);

            if(isOnForeGround) {
                callFailActivity();
            }else{
                sendSuccessFailed = getResources().getString(R.string.sending_failed);
                mSecureMessagingBinding.txtSecureMsg.setText("");
                progress.setVisibility(View.GONE);
            }

        }
    }

    /**
     * Method is used to call sending Fail status
     */
    private void callFailActivity() {
        Intent failureIntent = new Intent(this, FailActivity.class);
        Bundle mBundle = getIntent().getExtras();
        mBundle.putString(FROM, sendMessageType);
        mBundle.putString(ERRORMESSAGE, statusMessage);
        mBundle.putString(TXID, sendMessagetxId);
        failureIntent.putExtras(mBundle);
        startActivity(failureIntent);
    }

    /**
     * Method is used to call sending success status
     */
    private void callSuccessActivity() {
        Intent successIntent = new Intent(this, SuccessActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable(WALLET_DETAILS, mSelectedWallet);
        mBundle.putSerializable(MESSAGE_FEE, getIntent().getExtras().getString(MESSAGE_FEE));
        mBundle.putString(FROM, sendMessageType);
        mBundle.putString(TXID, sendMessagetxId);
        mBundle.putString(WALLET_TYPE,mWalletType);
        mBundle.putString(ROW_ID, getIntent().getExtras().getString(ROW_ID));
        mBundle.putString(MESSAGE_FEE,getIntent().getExtras().getString(MESSAGE_FEE));
        successIntent.putExtras(mBundle);
        startActivity(successIntent);
    }

    @Override
    protected void onResume() {
        if(!isOnForeGround && sendSuccessFailed.equals(getResources().getString(R.string.sent_success))){
        callSuccessActivity();
        }else if(!isOnForeGround && sendSuccessFailed.equals(getResources().getString(R.string.sending_failed))){
            callFailActivity();
        }else{

        }
        super.onResume();
    }

    /**
     * For saving message a confirm dialog box will appear
     */
    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WalletEmptyDialogBinding walletEmptyDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.wallet_empty_dialog, null, false);
        dialog.setContentView(walletEmptyDialogBinding.getRoot());
        walletEmptyDialogBinding.title.setVisibility(View.VISIBLE);
        walletEmptyDialogBinding.title.setText(getResources().getString(R.string.confirmDialogSave));
        walletEmptyDialogBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent homeIntent = new Intent(SendingActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        walletEmptyDialogBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                new CRUD_OperationOfDB(SendingActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?",
                        new String[]{getIntent().getExtras().getString(ROW_ID)});

                Intent homeIntent = new Intent(SendingActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        dialog.show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnForeGround = false;
    }

    /**
     * Background task class to combines files to zip
     *
     */
    private class AsynTaskSendMessage extends AsyncTask<Void,Void,DataMoverModel>{
        String msgCases = "";
        public AsynTaskSendMessage(String msgCase){
            this.msgCases  = msgCase;

        }
        @Override
        protected DataMoverModel doInBackground(Void... params) {
            DataMoverModel obj = setUpDataMoverModel(msgCases);
            return obj;
        }

        @Override
        protected void onPostExecute(DataMoverModel dataMoverModel) {
            super.onPostExecute(dataMoverModel);
            if(msgCases.equals(BITVAULT)){
                if (mSelectedWallet != null && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null) {
                        mDataMover.sendMessage(Integer.parseInt(mSelectedWallet.getWALLET_ID()), mCharacter_count, calculateSize(), dataMoverModel, SendingActivity.this, getIntent().getExtras().getString(TXID),getResources().getString(R.string.webserver_key_value),BITVAULT,mEotWalletAddress,mWalletType);
                    } else {
                        mDataMover.sendMessage(Integer.parseInt(mSelectedWallet.getWALLET_ID()), mCharacter_count, calculateSize(), dataMoverModel, SendingActivity.this, "",getResources().getString(R.string.webserver_key_value),BITVAULT,mEotWalletAddress,mWalletType);
                    }
                }
                else{
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null) {
                        mDataMover.sendMessage(-1, mCharacter_count, calculateSize(), dataMoverModel, SendingActivity.this, getIntent().getExtras().getString(TXID),getResources().getString(R.string.webserver_key_value),BITVAULT,mEotWalletAddress,mWalletType);
                    } else {
                        mDataMover.sendMessage(-1, mCharacter_count, calculateSize(), dataMoverModel, SendingActivity.this, "",getResources().getString(R.string.webserver_key_value),BITVAULT,mEotWalletAddress,mWalletType);
                    }
                }
            }else{
                if (mSelectedWallet != null ) {
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(TXID) != null) {
                        mDataMover.sendMessage(Integer.parseInt(mSelectedWallet.getWALLET_ID()), calculateSize(), dataMoverModel, SendingActivity.this, getIntent().getExtras().getString(TXID),getResources().getString(R.string.webserver_key_value),BITVAULTATTACH);
                    } else {
                        mDataMover.sendMessage(Integer.parseInt(mSelectedWallet.getWALLET_ID()), calculateSize(), dataMoverModel, SendingActivity.this, "",getResources().getString(R.string.webserver_key_value),BITVAULTATTACH);
                    }
                }
            }

        }
    }
}
