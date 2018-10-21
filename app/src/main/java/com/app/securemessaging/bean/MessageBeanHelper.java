package com.app.securemessaging.bean;

import java.io.Serializable;

/**
 * Message Bean Helper class to Set message,name and time.
 */
public class MessageBeanHelper implements Serializable{

    /**
     *  STATUS = "status";  // Pending,Success,fail
     *  TYPE = "type";  // Inbox,Sent,Draft
     */

    private String name = "Anonymous", message = "", time = "", attachment = "", receiverAddress = "Anonymous", senderAddress = "", txdID = "", timeInMillis = "", status = "", typeDraftSent = "";
    private String mType = "",id ="" , is_new = "" ,mTag = "" ,mSessionKey = "",mMessageFee = "";
    private String mWalletType = "";
    private boolean isSelected = false;
    private String mWalletName = "";

    /**
     * parameterize constructors for message helper bean
     */

    public MessageBeanHelper(String name, String message, String time, String mType) {
        this.message = message;
        this.name = name;
        this.time = time;
        this.mType = mType;
    }

    /**
     * constructors for message helper bean
     */
    public MessageBeanHelper() {
    }

    /**
     * parameterize constructors for message helper bean
     */
    public MessageBeanHelper(String name, String message, String time, String mAttachment,
                             String mReceiverAddress, String mSenderAddress, String mTxdID,
                             String mTimeInMillis, String mStatus, String mTypeDraftSent,
                             String mType,String tag ,String sessionKey,String walletTYpe) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.attachment = mAttachment;
        this.receiverAddress = mReceiverAddress;
        this.senderAddress = mSenderAddress;
        this.txdID = mTxdID;
        this.timeInMillis = mTimeInMillis;
        this.status = mStatus;
        this.typeDraftSent = mTypeDraftSent;
        this.mType = mType;
        this.mTag = tag;
        this.mSessionKey = sessionKey;
        this.mWalletType = walletTYpe;
    }

    public String getmWalletName() {
        return mWalletName;
    }

    public void setmWalletName(String mWalletName) {
        this.mWalletName = mWalletName;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getTxdID() {
        return txdID;
    }

    public void setTxdID(String txdID) {
        this.txdID = txdID;
    }

    public String getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(String timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeDraftSent() {
        return typeDraftSent;
    }

    public void setTypeDraftSent(String typeDraftSent) {
        this.typeDraftSent = typeDraftSent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIs_new() {
        return is_new;
    }

    public void setIs_new(String is_new) {
        this.is_new = is_new;
    }

    public String getmTag() {
        return mTag;
    }

    public void setmTag(String mTag) {
        this.mTag = mTag;
    }

    public String getSessionKey() {
        return mSessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.mSessionKey = sessionKey;
    }
    public String getmWalletType() {
        return mWalletType;
    }

    public void setmWalletType(String mWalletType) {
        this.mWalletType = mWalletType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public String getmMessageFee() {
        return mMessageFee;
    }

    public void setmMessageFee(String mMessageFee) {
        this.mMessageFee = mMessageFee;
    }
}
