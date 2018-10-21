package com.app.securemessaging.appinterface;

import android.os.Environment;

/**
 * Class used to declare all application constants
 */

public interface IAppConstants {

    String MESSAGE_TYPE = "message_type";
    String BITVAULT_MESSAGE = "bitvault_message";
    String BITVAULT_MESSAGE_WITH_ATTACHMENT = "bitvault_message_with_attachment";
    String BITATTACH_MESSAGE = "bitattach_message";
    String VAULT_TO_PC_MESSAGE = "vault_to_pc";

    String MEDIA_TYPE_GALLERY_CAMERA = "media_type_gallery_camera";
    String MEDIA_TYPE_CAMERA_IMAGE = "media_type_camera_image";
    String MEDIA_TYPE_CAMERA_VIDEO = "media_type_camera_video";
    String MEDIA_TYPE_GALLERY_VIDEO = "media_type_gallery_video";
    String MEDIA_TYPE_GALLERY_IMAGE = "media_type_gallery_image";

    String MEDIA_TYPE_AUDIO = "media_type_audio";

    String DESKTOP_TO_DEKSTOP = "desktop_to_desktop";
    String PHONE_TO_DESKTOP = "phone_to_desktop";
    String FROM = "from";
    String WALLET_DETAILS = "wallet_details";

    String SELECTED_LIST = "selected_list";
    String RECEIVER_ADDRESS = "receiver_address";
    String CHARACTER_COUNT = "character_count";
    String MESSAGE_OBJECT = "message_object";
    String WALLET = "wallet";
    String MESSAGE = "message";
    String MESSAGE_FEE = "message_fee";
    String TXID = "tx_id";
    String ROW_ID = "row_id";
    String REPLY = "reply";
    String ERRORMESSAGE = "error_message";
    String TRUE = "true";
    String REPEATSERVICE = "repeat_service";
    String FALSE = "false";
    String UPDATE_MESSAGE = "update_message";
    String STOP_REFRESH = "stop_refresh";
    String FILE_SEPERATOR = "$";
    String FILE_SEPERATOR_SPLIT = "\\$";
    String B2A_SESSIONKEY = "b2a_sessionKey";

    /**
     * Message type either inbox. sent or draft
     */
    String INBOX = "inbox";
    String SENT = "sent";
    String DRAFT = "draft";

    /**
     * Status of the sending message either pending, success or fail
     */
    String PENDING = "pending";
    String SUCCESS = "success";
    String FAIL = "fail";

    /**
     * Message type for different types of sending faciliities
     * <p>
     * BITATTACH - for desktop to desktop
     * BITVAULT - for phone to phone
     * BITVAULTATTACH - for phone to desktop
     */
    String BITATTACH = "bit_attach";
    String BITVAULT = "bit_vault";
    String BITVAULTATTACH = "bit_vault_attach";

    /**
     * Message Tags
     */
    String SECURE_MESSAGE = "secure_message";
    String A2A_FILE = "A2A_Sessionkey";
    String B2A_FILE = "B2A_FileNotification";
    String D2D_NOTIFICATION ="A2A_FileNotification";
    String PBC_ID = "pbc_id";

    /**
     * Message Priority
     */

    String PRIORITY_HIGH = "high";
    String PRIORITY_MEDIUM = "medium";
    String PRIORITY_LOW = "low";

    /**
     * for Status
     */
    String STATUS_OK = "ok";
    String STATUS_SUCCESS = "success";


    String RECEIVED_MEDIA_PATH = Environment.getExternalStorageDirectory() + "/" + "ReceivedMedia";

    String SECURE_MESSAGE_PATH = "/home/vvdn/Downloads/sendMessagePath/";
    String SECURE_MESSAGE_ZIP = "sendMessageZipFile.txt";
    String TEXT_MESSAGE = "text_messsage.txt";

//    String SDK_DB_DIR_NAME="SecureSDK";

    String VIDEO = "video";
    String IMAGE = "image";
    String AUDIO = "audio";

    //Strings used for notification navigation
    String TAG_NOTIFICATION_RECEIVER = "receiver_address";
    String TAG_NOTIFICATION_DATA = "data";
    String TAG_NOTIFICATION_SENDER = "sender_address";
    String TAG_NOTIFICATION_TEXT = "You have received a new secure message from ";
    String DATA_TAG = "tag";

    // string used in utils for fetching user phone contacts
    String MOBILE = "MobileModel";
    String HOME = "Home";
    String WORK = "Work";
    String MAIN = "Main";
    String CUSTOM = "Custom";

    // string used for BitAttach
    String BitAttachSessionKey = "MessagesessionKey";
    String BitAtachSize = "BitAttachFileSize";

    String TAG_RECEIVERADD_CONTACT ="keyaddress";

    String MESSAGE_SEND_SUCCESS = "Send message successfully";


    String WALLET_ADDARRAY_B2A = "walletAddArrayB2A";
    String WALLET_NAMEARRAY_B2A = "walletNameArrayB2A";
    int HOME_ACTIVITY_RUNNABLE = 300;
    String TAG_VIDEO_URI = "video/*";
    String TAG_AUDIO_URI = "audio/*";
    String TAG_IMAGE_URI = "image/*";
    String TAG_PROVIDER = ".provider";
    String TAG_CHOOSE_RECEIVER = "Choose an ReceiverAddress";
    String TAG_BITCOINS = "bitcoin:";
    String TAG_DATA = "data";
    long MAX_MESSAGE_SIZE = 25 * 1024;
    int RECEIVER_ADDRESS_CHARS = 34;
    String CONTACT_SAVE = "saveContactByMessaging";
    String WALLET_TYPE = "walletType";
    String EOT_WALLET_BAL = "EotwalletBal";
    String EOT_WALLET_ADDRESS = "EotwalletAddress";
    String QR_WALLET_ADDRESS = "EotwalletAddress";


    String NOTIFICATION_FILTER = "com.embedded.download.intent.action.Notification";
    String BUNDLE_DATA = "bundle_data";
    String FROM_NOTIFICATION = "from_notification";
    String NOTIFICATION_ID = "notification_id";


}
