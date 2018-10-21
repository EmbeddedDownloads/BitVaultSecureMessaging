package com.app.securemessaging.service;


import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.util.ZipUnZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bitmanagers.BitVaultDataManager;
import bitmanagers.BitVaultWalletManager;
import bitmanagers.EOTWalletManager;
import iclasses.EotCallbacks.EotWalletCallback;
import iclasses.ReceiveMessageCallBack;
import iclasses.WalletArrayCallback;
import model.WalletDetails;
import model.eotmodel.EotWalletDetail;

/**
 * Service used to poll PBC to get the message and insert into database
 */

public class ReceiveMessageService implements WalletArrayCallback, ReceiveMessageCallBack, IAppConstants ,EotWalletCallback {

    private BitVaultWalletManager mBitVaultManager;
    private BitVaultDataManager mBitVaultDataManager;
    private EOTWalletManager mEotWalletManager = null;
    private List<WalletDetails> mWalletDetails;
    private List<Integer> intWalletId;
    private Context mContext = null;
    private int mNotification_Id = 0;

    /**
     * Method used to start receive message process by Notification case
     */
    public void receiveStart(Context context, String mDataHashTxId, String mReceiverAddress, String mTag,int notification_Id){
        this.mContext = context;
        mNotification_Id = notification_Id;
        mBitVaultManager = BitVaultWalletManager.getWalletInstance();
        mBitVaultDataManager = BitVaultDataManager.getSecureMessangerInstance();
        mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
        if (mBitVaultDataManager != null)
              mBitVaultDataManager.receiveMessage(mTag,mReceiverAddress,mDataHashTxId,ReceiveMessageService.this);
    }

    /**
     * Method used to start receive message process by Pull to refresh case
     */
    public void receiveStart(Context context){
        this.mContext = context;
        mNotification_Id = -1;
        fetchWallets();
    }

    /**
     * Method used to fetch wallets and start receiving messages
     */
    private void fetchWallets(){
        try {
            mBitVaultManager = BitVaultWalletManager.getWalletInstance();
            mBitVaultDataManager = BitVaultDataManager.getSecureMessangerInstance();
            mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
            mWalletDetails = new ArrayList<>();
            mBitVaultManager.getWallets(ReceiveMessageService.this);
            createMediaDirectory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {

        if (mWalletDetails != null && mWalletDetails.size() > 0) {
            mWalletDetails.clear();
        }

        try {
            if (BitVaultDataManager.getSecureMessangerInstance().getUserValid()) {
                mWalletDetails = mRequestedWallets;
                intWalletId = new ArrayList<>();
                for (int i = 0; i < mWalletDetails.size(); i++) {
                    intWalletId.add(Integer.parseInt(mWalletDetails.get(i).getWALLET_ID()));
                }

                if (intWalletId != null && intWalletId.size() > 0
                        && BitVaultDataManager.getSecureMessangerInstance().getUserValid()) {
                        mEotWalletManager.getEotWallet(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void receiveMessageCallback(String status, String message, String messageList, String senderAdress,
    String tag,String txId) {
        try {
            if (BitVaultDataManager.getSecureMessangerInstance().getUserValid()) {

                if (status != null && status.equalsIgnoreCase(STATUS_SUCCESS) || status.equalsIgnoreCase(STATUS_OK)) {
                    if (message != null && messageList != null && !messageList.equals("")) {
                        decrypteMessage(messageList, senderAdress,tag,txId);
                    } else {
                        callBroadcastReceiver(STOP_REFRESH);
                    }
                } else {
                    callBroadcastReceiver(STOP_REFRESH);
                }
                removeNotification();
            } else {
                callBroadcastReceiver(STOP_REFRESH);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to decrypt message
     *
     * @param messageList
     * @param mSenderAddress
     */
    private void decrypteMessage(String messageList, String mSenderAddress,String tag,String txId) {

        String decodeMessage = "";
        String mAttachmentFiles = "";

        if(BitVaultDataManager.getSecureMessangerInstance().getUserValid()){
            if (messageList != null && !messageList.equalsIgnoreCase("")) {
                String UnZipRootPath = IAppConstants.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis();
                ZipUnZipFile.unzip(messageList,UnZipRootPath);

                File dir = new File(UnZipRootPath);
                File[] files = dir.listFiles();

                for (int i = 0; i <files.length ; i++) {
                    if(Utils.getFileExtension(files[i]).equalsIgnoreCase(".txt")){
                        decodeMessage = Utils.readDataFromTxt(files[i].getAbsolutePath());
                    }
                    else{
                        mAttachmentFiles  += files[i].getAbsolutePath() + IAppConstants.FILE_SEPERATOR;

                    }
                }
                if ((mAttachmentFiles != null && mAttachmentFiles.length() > 0 ? insertValuesInDB(decodeMessage, mAttachmentFiles, mSenderAddress,tag,txId) : insertValuesInDB(decodeMessage, mSenderAddress,tag,txId)) > 0) {
                    callBroadcastReceiver(UPDATE_MESSAGE);
                }
            }
        }


    }

    /**
     * Method is used to insert values in database
     */
    public long insertValuesInDB(String decodedMessage, String mSenderAddress,String tag,String txId) {

        try {

            CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(mContext);

            ContentValues mContentValues = new ContentValues();
            mContentValues.put(ContractClass.FeedEntry.MESSAGE, decodedMessage);
            mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mSenderAddress);
            mContentValues.put(ContractClass.FeedEntry.TYPE, INBOX);
            mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
            mContentValues.put(ContractClass.FeedEntry.STATUS, SUCCESS);
            mContentValues.put(ContractClass.FeedEntry.IS_NEW, TRUE);
            mContentValues.put(ContractClass.FeedEntry.TXD_ID,txId);
            mContentValues.put(ContractClass.FeedEntry.TAG,tag);
            String mAttachment = "";


            if(tag != null && !tag.equalsIgnoreCase("")){
                if(tag.equalsIgnoreCase(IAppConstants.SECURE_MESSAGE)){
                    mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITVAULT_MESSAGE);
                }
                else if(tag.equalsIgnoreCase(IAppConstants.A2A_FILE)){
                    mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITATTACH_MESSAGE);
                }
                else{

                }
            }


            mContentValues.put(ContractClass.FeedEntry.ATTACHMENT, mAttachment);

            return dbOperation.InsertIntoDB(ContractClass.FeedEntry.TABLE_NAME, mContentValues);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }


    /**
     * Method is used to insert values in database
     */
    public long insertValuesInDB(String decodedMessage, String mAttachedFiles, String mSenderAddress,String tag,String txId) {

        try {

            CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(mContext);

            ContentValues mContentValues = new ContentValues();
            mContentValues.put(ContractClass.FeedEntry.MESSAGE, decodedMessage);
            mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mSenderAddress);
            mContentValues.put(ContractClass.FeedEntry.TYPE, INBOX);
            mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
            mContentValues.put(ContractClass.FeedEntry.STATUS, SUCCESS);
            mContentValues.put(ContractClass.FeedEntry.IS_NEW, TRUE);
            mContentValues.put(ContractClass.FeedEntry.TXD_ID,txId);
            mContentValues.put(ContractClass.FeedEntry.TAG,tag);

            String mAttachment = "";


            if(tag != null && !tag.equalsIgnoreCase("")){
                if(tag.equalsIgnoreCase(IAppConstants.SECURE_MESSAGE)){
                    mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITVAULT_MESSAGE_WITH_ATTACHMENT);
                }
                else if(tag.equalsIgnoreCase(IAppConstants.A2A_FILE)){
                    mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITATTACH_MESSAGE);
                }
                else{

                }
            }

            mContentValues.put(ContractClass.FeedEntry.ATTACHMENT, mAttachedFiles.toString());

            return dbOperation.InsertIntoDB(ContractClass.FeedEntry.TABLE_NAME, mContentValues);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Method used to send Broadcast to inbox fragment if broadcast is registered
     *
     * @param from
     */
    public void callBroadcastReceiver(String from) {
        Intent intent = new Intent("com.app.securemessaging.UpdateUI");
        intent.putExtra(FROM, from);
        mContext.sendBroadcast(intent);
    }

    /***
     * This method is used to create the folder to store received attachment inside the external storage.
     */
    private void createMediaDirectory() {
        try {
            File mFilename = new File(RECEIVED_MEDIA_PATH);
            if (!mFilename.exists()) {
                mFilename.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * This method is used to remove notification from notification Bar.
     */
    private void removeNotification(){
        try {
            if(mContext != null) {
                NotificationManager notificationManager = (NotificationManager) mContext.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotification_Id > 0 ) {
                    notificationManager.cancel(mNotification_Id);
                    mNotification_Id = 0;
                } else if (mNotification_Id == -1 ) {
                    notificationManager.cancelAll();
                    mNotification_Id = 0;
                } else {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eotWallet(EotWalletDetail mEotWalletDetail) {
        if(mEotWalletDetail != null){
            String eotWalletAddress = mEotWalletDetail.getAddress();
            // Call secure sdk method to get the message
            if (mBitVaultDataManager != null) {
                mBitVaultDataManager.receiveMessage(intWalletId,eotWalletAddress, ReceiveMessageService.this);
            }
        }

    }
}
