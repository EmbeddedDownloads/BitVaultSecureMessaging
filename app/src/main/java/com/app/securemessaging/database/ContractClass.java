package com.app.securemessaging.database;

import android.provider.BaseColumns;

/**
 * Class will contains all fields to be related to database
 */

public class ContractClass {

    /* Inner class that defines the table contents */

    public static abstract class FeedEntry implements BaseColumns {

        public static final String TABLE_NAME = "secure_messaging";
        public static final String MESSAGE = "message";
        public static final String ATTACHMENT = "attachment_data";
        public static final String RECEIVER_ADDRESS = "receiver_address";
        public static final String SENDER_ADDRESS = "sender_address";
        public static final String TXD_ID = "transaction_id";
        public static final String TIME = "time";
        public static final String TIME_IN_MILLI = "time_in_milis";
        public static final String STATUS = "status";  // Pending,Success,fail
        public static final String TYPE = "type";  // Inbox,Sent,Draft,fail
        public static final String MESSAGE_TYPE = "message_type"; // BitAttach, BitAttachWithAttachment, BitVault, BitPhone to Desktop
        public static final String IS_NEW = "is_new";
        public static final String TAG = "message_tag";
        public static final String SESSIONKEY = "session_key";
        public static final String WALLETTYPE = "wallet_type";
        public static final String MESSAGE_FEE = "message_fee";
        public static final String WALLET_NAME = "wallet_name";
    }
}
