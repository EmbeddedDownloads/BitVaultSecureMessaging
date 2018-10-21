package com.app.securemessaging.view.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.app.securemessaging.databinding.ChooseImageVideoBinding;
import com.app.securemessaging.databinding.CreateMessageBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.util.WrapContentLinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import model.WalletDetails;
import qrcode.ScanQRCode;
import utils.SDKUtils;

/**
 * information related to message and attachments file
 */

public class CreateMessage extends AppCompatActivity implements View.OnClickListener, IAppConstants {
    public static final int CAMERA_PIC_REQUEST = 2500;
    public static final int CAMERA_VIDEO_REQUEST = 2600;
    public static final int ACTIVITY_RECORD_SOUND = 1;
    public static final int GALLERY_PIC_REQUEST = 3;
    private final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 100;
    private final int REQUEST_SCAN_PRIVATE_KEY = 500;
    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 200;
    private final int MY_PERMISSIONS_CAMERA = 300;
    private final int oneMessageLength = 180;
    private List<String> granted;
    private List<String> denied;
    private AutoCompleteAdapter mAdapter;
    private CreateMessageBinding binding;
    private ShowImageAdapter adapter;
    private Cursor cursor;
    private ContentResolver contentResolver;
    private ArrayList<ShowImageBean> mPath;
    private Intent camIntent;
    private boolean isFromCamera = false;
    private WalletDetails mSelectedWalletDetails;
    private boolean isSavedInDb = false;
    private long rowId = 0;
    private ArrayList<ContactsModel> mContactsModel;
    private boolean isThroughAttachment = false;
    private boolean isThroughOnActivity = false;
    private Uri mImageUri;
    private static final int MY_PERMISSIONS_CONTACT = 101;
    private String messageType = "";
    private HashMap<String,String> walletArrayB2A = new HashMap<>();
    private String mWalletType = "";
    private String mEotWalletAddress = "";
    private double mEotCoinsTotal = 0.0;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".mp4");

        return destination;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.create_message);

        setListner();

        initData();

        setData();
    }

    /**
     * Method used to match data from contacts
     *
     * @param keyword
     */
    private void filterMatchedData(String keyword) {

        try {

            if (mContactsModel != null && mContactsModel.size() > 0) {

                if (binding != null) {

                    if (mAdapter != null) {
                        binding.receiverAddress.setAdapter(mAdapter);
                        binding.receiverAddress.setThreshold(1);
                    } else {
                        mAdapter = new AutoCompleteAdapter(this, R.layout.spinner_autocomplete_layout, mContactsModel, 1);
                        binding.receiverAddress.setAdapter(mAdapter);
                        binding.receiverAddress.setThreshold(1);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is used to set data
     */
    private void setData() {
        binding.toolbar.deleteIcon.setText(getResources().getString(R.string.m));
    }

    /**
     * Method is used to get data from intent
     */
    private void getDataFromIntent() {

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getStringExtra(FROM) != null) {
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
                if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                    MessageBeanHelper mMessagebeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);

                    binding.receiverAddress.setText(mMessagebeanHelper.getReceiverAddress().toString());
                    binding.receiverAddress.setSelection(mMessagebeanHelper.getReceiverAddress().toString().length());
                    if(getIntent().getStringExtra(MESSAGE_TYPE).equalsIgnoreCase(BITVAULT_MESSAGE)){
                        binding.editMessage.setText(mMessagebeanHelper.getMessage().toString());
                        binding.editMessage.setSelection(mMessagebeanHelper.getMessage().toString().length());
                    }
                    if (mMessagebeanHelper.getAttachment() != null && !mMessagebeanHelper.getAttachment().toString().equals("")) {

                        String mSelectedImages[] = mMessagebeanHelper.getAttachment().split(IAppConstants.FILE_SEPERATOR_SPLIT);

                        for (int i = 0; i < mSelectedImages.length; i++) {
                            ShowImageBean obj = new ShowImageBean();
                            obj.setImage(mSelectedImages[i]);
                            obj.setType(Utils.getMediaType(Utils.getFileExtension(new File(mSelectedImages[i]))));
                            obj.setFileSize(new File(mSelectedImages[i]).length());
                            mPath.add(obj);
                        }


                        if (adapter == null) {
                            adapter = new ShowImageAdapter(mPath, this);
                            binding.showImage.setAdapter(adapter);
                        } else {
                            adapter.setUpdatedData(mPath);
                        }


                    }

                    binding.editMessage.requestFocus();
                }
                break;

            case REPLY:
                    setWalletData();
                    binding.receiverAddress.setText(getIntent().getExtras().getString(RECEIVER_ADDRESS));
                    binding.receiverAddress.setSelection(getIntent().getExtras().getString(RECEIVER_ADDRESS).toString().length());
                    binding.editMessage.requestFocus();
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
     * set the title of the toolbar
     */
    private void initData() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getStringExtra(MESSAGE_TYPE) != null) {
            messageType = getIntent().getStringExtra(MESSAGE_TYPE);
            if(messageType.equalsIgnoreCase(VAULT_TO_PC_MESSAGE)){
                binding.toolbar.title.setText(getResources().getString(R.string.attach_files));
                binding.editMessage.setVisibility(View.GONE);
                binding.charCountTv.setVisibility(View.GONE);

                if(getIntent() != null && getIntent().getStringArrayExtra(WALLET_ADDARRAY_B2A) != null
                         && getIntent().getStringArrayExtra(WALLET_NAMEARRAY_B2A) != null){

                    final String[] walletAdd  = getIntent().getStringArrayExtra(WALLET_ADDARRAY_B2A);
                    final String[] walletName  = getIntent().getStringArrayExtra(WALLET_NAMEARRAY_B2A);

                    for (int i = 0; i < walletAdd.length ; i++) {
                        walletArrayB2A.put(walletName[i],walletAdd[i]);
                    }

                    selectReceiverAdderess(this,walletName);
                    binding.receiverAddress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectReceiverAdderess(CreateMessage.this,walletName);
                        }
                    });
                }


            }
            else{
                binding.toolbar.title.setText(getResources().getString(R.string.create_message));
                binding.editMessage.setVisibility(View.VISIBLE);
                binding.charCountTv.setVisibility(View.VISIBLE);

            }
        }
        contentResolver = getContentResolver();
        mPath = new ArrayList<>();

        granted = new ArrayList<String>();
        denied = new ArrayList<String>();
        adapter = new ShowImageAdapter(mPath, this);
        mContactsModel = new ArrayList<>();

        mContactsModel = AppPreferences.getInstance(CreateMessage.this).getContact();
        if(mContactsModel != null && mContactsModel.size() > 0){
        }
        else{
            if(!messageType.equalsIgnoreCase(VAULT_TO_PC_MESSAGE)){
                askForPermission();
            }
        }
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(this, WrapContentLinearLayoutManager.HORIZONTAL, true);
        binding.showImage.setLayoutManager(layoutManager);
        binding.showImage.setAdapter(adapter);

    }

    /**
     * intialize the click button
     */

    private void setListner() {
        binding.toolbar.walletInfoBack.setOnClickListener(this);
        binding.toolbar.deleteIcon.setOnClickListener(this);
        binding.attach.setOnClickListener(this);
        binding.camera.setOnClickListener(this);
        binding.gallery.setOnClickListener(this);
        binding.recorder.setOnClickListener(this);
        binding.qrScanner.setOnClickListener(this);
        binding.saveContact.setOnClickListener(this);

        /**
         * EditText text change listner
         */
        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            /**
             * total size of the message
             * @param s
             */
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > oneMessageLength)
                    binding.charCountTv.setText((oneMessageLength - (s.toString().length() % oneMessageLength)) + "/" + ((s.toString().length() / oneMessageLength) + 1));
                else {
                    binding.charCountTv.setVisibility(View.VISIBLE);
                    binding.charCountTv.setText((oneMessageLength - s.toString().length()) + "/1");
                }
            }
        });


        // Text change listner for receiver address

        binding.receiverAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int listSize = mContactsModel.size();
                    if (s.toString().length() >= 1) {
                        for (int i = 0; i <listSize ; i++) {
                            if(mContactsModel.get(i).getmReceiverAddress().equals(s.toString())){
                                binding.saveContact.setVisibility(View.GONE);
                                break;
                            }else if(i == listSize - 1){
                                binding.saveContact.setVisibility(View.VISIBLE);
                                break;
                            }

                        }
                        filterMatchedData(s.toString());
                    }else{
                      binding.saveContact.setVisibility(View.GONE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == CAMERA_PIC_REQUEST) {

                try {
                    Uri imageUri;
                    if (data != null && data.getData() != null)
                        imageUri = data.getData();
                    else
                        imageUri = mImageUri;

                    isThroughAttachment = false;
                    isThroughOnActivity = true;

                    String imagePath = "";

                    imagePath = getRealPathFromURI(imageUri);

                    if (imagePath != null && new File(imagePath).exists()) {

                        ShowImageBean obj = new ShowImageBean();
                        obj.setImage(imagePath);
                        obj.setType(MEDIA_TYPE_CAMERA_IMAGE);
                        obj.setFileSize(new File(imagePath).length());
                        mPath.add(obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (requestCode == CAMERA_VIDEO_REQUEST && data != null) {
                isThroughAttachment = false;
                isThroughOnActivity = true;
                Uri vid = data.getData();
                String videoPath = getRealPathFromURI(vid);

                try {
                    if (videoPath != null && new File(videoPath).exists()) {

                        ShowImageBean obj = new ShowImageBean();
                        obj.setImage(videoPath);
                        obj.setType(MEDIA_TYPE_CAMERA_VIDEO);
                        obj.setFileSize(new File(videoPath).length());
                        mPath.add(obj);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (requestCode == GALLERY_PIC_REQUEST && data != null) {
                try {
                    isThroughAttachment = false;
                    isThroughOnActivity = true;

                    if (data.getClipData() != null) {

                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {

                            Uri selectedImage = data.getClipData().getItemAt(i).getUri();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

                            if (filePathColumn != null) {
                                Cursor cursor = getContentResolver().query(selectedImage,
                                        filePathColumn, null, null, null);

                                if (cursor != null && cursor.getCount() > 0) {
                                    cursor.moveToFirst();

                                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                    String picturePath = cursor.getString(columnIndex);

                                    int iDColumn = cursor.getColumnIndex(filePathColumn[1]);
                                    String id = cursor.getString(iDColumn);

                                    cursor.close();


                                    File mFile = new File(picturePath);

                                    if (Utils.getMediaType(Utils.getFileExtension(mFile)).equalsIgnoreCase(VIDEO)
                                            ) {

                                        String videoThumb = "";

                                        String[] VIDEOTHUMBNAIL_TABLE = new String[]{
                                                MediaStore.Video.Media._ID, // 0
                                                MediaStore.Video.Media.DATA, // 1 from android.provider.MediaStore.Video
                                        };

                                        Uri videoUri = MediaStore.Video.Thumbnails.getContentUri("external");

                                        Cursor c = getContentResolver().query(videoUri, VIDEOTHUMBNAIL_TABLE, MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                                                new String[]{id}, null);

                                        if (c != null && c.getCount() > 0) {
                                            c.moveToFirst();
                                            do {
                                                videoThumb = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                                            } while (c.moveToNext());
                                        }
                                        c.close();

                                        if (picturePath != null && new File(picturePath).exists()) {

                                            ShowImageBean obj = new ShowImageBean();
                                            obj.setImage(picturePath);
                                            obj.setType(MEDIA_TYPE_GALLERY_VIDEO);
                                            obj.setThumbnail(videoThumb);
                                            obj.setFileSize(new File(picturePath).length());

                                            mPath.add(obj);
                                        }

                                    } else if (Utils.getMediaType(Utils.getFileExtension(mFile)).equalsIgnoreCase(IMAGE)) {
                                        if (picturePath != null && new File(picturePath).exists()) {

                                            ShowImageBean obj = new ShowImageBean();
                                            obj.setImage(picturePath);
                                            obj.setType(MEDIA_TYPE_GALLERY_IMAGE);
                                            obj.setFileSize(new File(picturePath).length());

                                            mPath.add(obj);

                                        }
                                    }
                                } else {
                                    Uri fileUri = selectedImage;

                                    String path = "";

                                    if (getRealPathFromURI(fileUri) != null) {

                                        path = getRealPathFromURI(fileUri);
                                    } else {

                                        if (fileUri.toString().contains("file://")) {
                                            path = fileUri.toString().replace("file://", "");
                                            if (path.contains("%20")) {
                                                path = path.replace("%20", " ");
                                            }
                                        }
                                    }

                                    File mFile = new File(path);

                                    if (Utils.getMediaType(Utils.getFileExtension(mFile)).equalsIgnoreCase(VIDEO)
                                            ) {

                                        String videoThumb = "";

                                        ShowImageBean obj = new ShowImageBean();
                                        obj.setImage(path);
                                        obj.setType(MEDIA_TYPE_GALLERY_VIDEO);
                                        obj.setThumbnail(videoThumb);
                                        obj.setFileSize(new File(path).length());

                                        mPath.add(obj);


                                    } else if (Utils.getMediaType(Utils.getFileExtension(mFile)).equalsIgnoreCase(IMAGE)) {
                                        if (path != null && new File(path).exists()) {

                                            ShowImageBean obj = new ShowImageBean();
                                            obj.setImage(path);
                                            obj.setType(MEDIA_TYPE_GALLERY_IMAGE);
                                            obj.setFileSize(new File(path).length());

                                            mPath.add(obj);
                                        }
                                    }

                                }
                            }


                        }
                    }


                } catch (Exception exception) {
                    Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == ACTIVITY_RECORD_SOUND && data != null) {
                isThroughAttachment = false;
                isThroughOnActivity = true;
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, null, null, null, null);
                ShowImageBean obj = new ShowImageBean();

                if (data != null && data.getData() != null) {

                    Uri fileUri = data.getData();
                    String path = getRealPathFromURI(fileUri);

                    if (path != null) {
                        obj.setImage(path);
                        obj.setType(MEDIA_TYPE_AUDIO);
                        obj.setFileSize(new File(path).length());
                        mPath.add(obj);
                    } else {
                        String soundPath = "";
                        if (data.getExtras() != null && data.getExtras().get("data") != null) {
                            soundPath = data.getExtras().get("data").toString();
                            if (soundPath.contains("file://")) {
                                soundPath = soundPath.replace("file://", "");
                                if (soundPath.contains("%20")) {
                                    soundPath = soundPath.replace("%20", " ");
                                }
                            }
                        }

                        obj.setImage(soundPath != null && (!soundPath.equals("")) ? soundPath : path);
                        obj.setType(MEDIA_TYPE_AUDIO);
                        obj.setFileSize(new File(soundPath).length());
                        mPath.add(obj);
                    }
                } else if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToLast();

                    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    if (path != null && new File(path).exists()) {


                        String soundPath = "";
                        if (data.getExtras() != null && data.getExtras().get("data") != null) {
                            soundPath = data.getExtras().get("data").toString();
                            if (soundPath.contains("file://")) {
                                soundPath = soundPath.replace("file://", "");
                                if (soundPath.contains("%20")) {
                                    soundPath = soundPath.replace("%20", " ");
                                }
                            }
                        }

                        obj.setImage(soundPath != null && (!soundPath.equals("")) ? soundPath : path);
                        obj.setType(MEDIA_TYPE_AUDIO);
                        obj.setFileSize(new File(path).length());
                        mPath.add(obj);
                    }
                }
            } else if (requestCode == REQUEST_SCAN_PRIVATE_KEY && data != null) {
                isThroughAttachment = false;
                isThroughOnActivity = true;
                String scannedResult = data.getStringExtra(TAG_DATA);
                scannedResult = scannedResult.replace(TAG_BITCOINS, "");
                binding.receiverAddress.setText(scannedResult);
                binding.receiverAddress.setSelection(scannedResult.toString().length());
            }
        }


        if (adapter == null) {
            adapter = new ShowImageAdapter(mPath, this);
            binding.showImage.setAdapter(adapter);
        } else {
            adapter.setUpdatedDataSingleItem(mPath);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isThroughAttachment && !isThroughOnActivity) {
            getDataFromIntent();
            isThroughOnActivity = false;
        }

        granted = Utils.getAllPermisiions(this, granted);
    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {

            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.delete_icon:
                if (Utils.isNetworkConnected(this)) {

                    if (!binding.receiverAddress.getText().toString().trim().equals("")) {
                        if(binding.receiverAddress.getText().toString().trim().length() == RECEIVER_ADDRESS_CHARS){

                                if (!binding.editMessage.getText().toString().trim().equals("") ||
                                        (mPath != null && mPath.size() > 0)) {

                                    if (calculateSize() <= (MAX_MESSAGE_SIZE)) {
                                        if (getIntent().getExtras().getString(FROM).equals(SENT)) {
                                            MessageBeanHelper mMessagebeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);

                                            if (mMessagebeanHelper.getId() != null) {
                                                rowId = Long.parseLong(mMessagebeanHelper.getId());
                                            }

                                            updateDb(mMessagebeanHelper.getId());
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

                                        mBundle.putSerializable(SELECTED_LIST, mPath);
                                        mBundle.putString(RECEIVER_ADDRESS, binding.receiverAddress.getText().toString().trim());
                                        mBundle.putString(ROW_ID, rowId + "");
                                        mBundle.putString(WALLET_TYPE, mWalletType);

                                        if (messageType.equalsIgnoreCase(VAULT_TO_PC_MESSAGE)) {
                                            mBundle.putInt(CHARACTER_COUNT, 0);
                                            mBundle.putString(MESSAGE, "");
                                            mBundle.putString(FROM, BITVAULTATTACH);
                                        } else {
                                            mBundle.putInt(CHARACTER_COUNT, binding.editMessage.getText().toString().trim().length());
                                            mBundle.putString(MESSAGE, binding.editMessage.getText().toString().trim());
                                            mBundle.putString(FROM, BITVAULT);
                                            mBundle.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                                            mBundle.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                                        }

                                        WalletIntent.putExtras(mBundle);
                                        startActivity(WalletIntent);
                                    } else {
                                        Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.attachment_not_greater_5mb), Snackbar.LENGTH_SHORT);
                                    }
                                } else {
                                    if (messageType.equalsIgnoreCase(VAULT_TO_PC_MESSAGE)) {
                                        Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.attchfile_not_be_empty), Snackbar.LENGTH_SHORT);

                                    } else {
                                        Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.message_should_not_be_empty), Snackbar.LENGTH_SHORT);
                                    }
                                }

                        }
                        else {
                              Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.receiver_addresss_invalid), Snackbar.LENGTH_SHORT);

                          }

                    }
                    else {
                        Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.receiver_addresss_not_empty), Snackbar.LENGTH_SHORT);
                    }

                } else {

                    Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }

                break;

            case R.id.attach:

                if (binding.attachments.getVisibility() == View.GONE)
                    enterRevel(binding.attachments);
                else
                    exitReveal(binding.attachments);


                break;

            case R.id.recorder:

                boolean isPermissionGranted = false;
                isThroughAttachment = true;

                if (granted != null && granted.size() > 0) {

                    if (granted.contains(Manifest.permission.RECORD_AUDIO)) {

                        isPermissionGranted = true;

                        Intent soundRecorderIntent = new Intent();
                        soundRecorderIntent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);

                        PackageManager packageManager = getPackageManager();
                        if (soundRecorderIntent.resolveActivity(packageManager) != null) {
                            startActivityForResult(soundRecorderIntent, ACTIVITY_RECORD_SOUND);
                        } else {
                            Toast.makeText(CreateMessage.this, getResources().getString(R.string.voice_recorder), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (!isPermissionGranted) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(CreateMessage.this, Manifest.permission.RECORD_AUDIO) &&
                            denied.size() > 0 && denied.contains(Manifest.permission.RECORD_AUDIO)) {
                        showPermissionsSettingsPage(getResources().getString(R.string.recorder));
                    } else {
                        Utils.allocateRunTimePermissions(CreateMessage.this, new String[]{Manifest.permission.RECORD_AUDIO}, new int[]{ACTIVITY_RECORD_SOUND});
                    }
                }

                break;

            case R.id.gallery:

                try {

                    isThroughAttachment = true;
                    isFromCamera = false;
                    boolean isPermissionGrantedGallery = false;

                    if (granted != null && granted.size() > 0) {
                        if (granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                && granted.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                            isPermissionGrantedGallery = true;

                            Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                            if (imageUri != null) {

                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("*/*");
                                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{TAG_IMAGE_URI, TAG_VIDEO_URI});
                                startActivityForResult(photoPickerIntent, GALLERY_PIC_REQUEST);

                            }
                        }
                    }
                    if (!isPermissionGrantedGallery) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(CreateMessage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                denied.size() > 0 && denied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showPermissionsSettingsPage(getResources().getString(R.string.write_message));
                        } else {
                            Utils.allocateRunTimePermissions(CreateMessage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE});
                        }
                    }
                } catch (Exception exception) {
                    Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.camera:

                isThroughAttachment = true;
                isFromCamera = true;
                boolean isPermissionGrantedCamera = false;

                if (granted != null && granted.size() > 0) {

                    if (granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            && granted.contains(Manifest.permission.CAMERA)) {

                        isPermissionGrantedCamera = true;
                        showConfirmDialog();
                    }
                }
                if (!isPermissionGrantedCamera) {

                    if (granted != null && granted.size() > 0 && !granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(CreateMessage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                denied.size() > 0 && denied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showPermissionsSettingsPage(getResources().getString(R.string.write_message));
                        } else {
                            Utils.allocateRunTimePermissions(CreateMessage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE});
                        }
                    }

                    if (granted != null && granted.size() > 0 && granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(CreateMessage.this, Manifest.permission.CAMERA) &&
                                denied.size() > 0 && denied.contains(Manifest.permission.CAMERA)) {
                            showPermissionsSettingsPage(getResources().getString(R.string.camera));
                        } else {
                            Utils.allocateRunTimePermissions(CreateMessage.this, new String[]{Manifest.permission.CAMERA}, new int[]{MY_PERMISSIONS_CAMERA});
                        }
                    }
                }

                break;

            case R.id.qr_scanner:
                isThroughAttachment = true;
                startActivityForResult(new Intent(this, ScanQRCode.class), REQUEST_SCAN_PRIVATE_KEY);
                break;

            case R.id.saveContact:
                if(!binding.receiverAddress.getText().toString().trim().equals("")
                        && binding.receiverAddress.getText().toString().trim().length() == RECEIVER_ADDRESS_CHARS){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.embedded.contacts");
                    if (launchIntent != null) {
                        launchIntent.putExtra(CONTACT_SAVE,binding.receiverAddress.getText().toString().trim());
                        startActivity(launchIntent);
                    }
                }else{
                    Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.receiver_addresss_invalid), Snackbar.LENGTH_SHORT);
                }


                break;
        }
    }

    /**
     * open the previous activity on press of back button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Method is used to remove item from the list of images
     *
     * @param mShowImagebean -- Model class
     */

    public void removeItemFromList(ShowImageBean mShowImagebean) {
        try {
            if (mPath != null && mPath.size() > 0) {
                int position = mPath.indexOf(mShowImagebean);
                mPath.remove(mShowImagebean);
                adapter.notifyItemRemoved(position);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get callback of permission
     *
     * @param requestCode  -- code of the request
     * @param permissions  -- permissions
     * @param grantResults -- results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (isFromCamera) {
                        Utils.allocateRunTimePermissions(CreateMessage.this, new String[]{Manifest.permission.CAMERA}, new int[]{MY_PERMISSIONS_CAMERA});
                        binding.camera.performClick();
                    } else {
                        binding.gallery.performClick();
                    }

                } else {
                    denied.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                return;
            }


            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    denied.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                return;
            }

            case ACTIVITY_RECORD_SOUND: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.RECORD_AUDIO);

                    binding.recorder.performClick();

                } else {
                    denied.add(Manifest.permission.RECORD_AUDIO);
                }
                return;
            }

            case MY_PERMISSIONS_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.CAMERA);
                    binding.camera.performClick();
                } else {
                    denied.add(Manifest.permission.CAMERA);
                }
                return;
            }
            case MY_PERMISSIONS_CONTACT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new FetchContacts().execute();
                    return;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isThroughAttachment) {

            if (getIntent().getExtras().getString(FROM).equals(SENT)) {
                MessageBeanHelper mMessagebeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);

                if (mMessagebeanHelper.getId() != null) {
                    rowId = Long.parseLong(mMessagebeanHelper.getId());
                }

                updateDb(mMessagebeanHelper.getId());
            } else {
                insertValuesInDB();
            }
        }
    }

    /**
     * Method used to show snackbar to navigate to settings page for allowing permissions.
     */
    private void showPermissionsSettingsPage(String mMessage) {

        Snackbar.make(binding.parentLayout, mMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .show();
    }

    /**
     * Method is used to insert values in database
     */
    public long insertValuesInDB() {

        try {
            if (!getIntent().getExtras().getString(FROM).equals(SENT) && !isSavedInDb) {
                if (!binding.receiverAddress.getText().toString().trim().equals("")) {

                    if (!binding.editMessage.getText().toString().trim().equals("") ||
                            (mPath != null && mPath.size() > 0)) {

                        CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(this);

                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(ContractClass.FeedEntry.MESSAGE, binding.editMessage.getText().toString().trim());
                        mContentValues.put(ContractClass.FeedEntry.RECEIVER_ADDRESS, binding.receiverAddress.getText().toString());
                        mContentValues.put(ContractClass.FeedEntry.TYPE, DRAFT);
                        mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
                        mContentValues.put(ContractClass.FeedEntry.STATUS, PENDING);
                        mContentValues.put(ContractClass.FeedEntry.IS_NEW, FALSE);
                        mContentValues.put(ContractClass.FeedEntry.WALLETTYPE, mWalletType);

                        if (mWalletType != null && mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                            mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mEotWalletAddress);
                        }else{
                            mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mSelectedWalletDetails.getWALLET_ID());
                        }

                        String mAttachment = "";

                        if (mPath != null && mPath.size() > 0) {

                            for (int i = 0; i < mPath.size(); i++) {
                                mAttachment += mPath.get(i).getImage() + FILE_SEPERATOR;
                            }
                            if(messageType.equalsIgnoreCase(VAULT_TO_PC_MESSAGE)){
                                mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, VAULT_TO_PC_MESSAGE);
                            }
                            else{
                                mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITVAULT_MESSAGE_WITH_ATTACHMENT);
                            }

                        } else {

                            mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITVAULT_MESSAGE);
                        }

                        mContentValues.put(ContractClass.FeedEntry.ATTACHMENT, mAttachment);

                        return dbOperation.InsertIntoDB(ContractClass.FeedEntry.TABLE_NAME, mContentValues);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Method is used to insert values in database
     */
    public long updateDb(final String rowID) {

        try {
            if (!binding.receiverAddress.getText().toString().trim().equals("")) {

                if (!binding.editMessage.getText().toString().trim().equals("") ||
                        (mPath != null && mPath.size() > 0)) {

                    CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(this);

                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(ContractClass.FeedEntry.MESSAGE, binding.editMessage.getText().toString().trim());
                    mContentValues.put(ContractClass.FeedEntry.RECEIVER_ADDRESS, binding.receiverAddress.getText().toString());
                    mContentValues.put(ContractClass.FeedEntry.TYPE, DRAFT);
                    mContentValues.put(ContractClass.FeedEntry.TIME_IN_MILLI, System.currentTimeMillis());
                    mContentValues.put(ContractClass.FeedEntry.STATUS, PENDING);
                    mContentValues.put(ContractClass.FeedEntry.IS_NEW, FALSE);
                    mContentValues.put(ContractClass.FeedEntry.WALLETTYPE, mWalletType);

                    if (mWalletType != null && mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mEotWalletAddress);
                    }else{
                        mContentValues.put(ContractClass.FeedEntry.SENDER_ADDRESS, mSelectedWalletDetails.getWALLET_ID());
                    }

                    String mAttachment = "";

                    if (mPath != null && mPath.size() > 0) {

                        for (int i = 0; i < mPath.size(); i++) {
                            mAttachment += mPath.get(i).getImage() + FILE_SEPERATOR;
                        }
                        if(messageType.equalsIgnoreCase(VAULT_TO_PC_MESSAGE)){
                            mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, VAULT_TO_PC_MESSAGE);
                        }
                        else{
                            mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITVAULT_MESSAGE_WITH_ATTACHMENT);
                        }
                    } else {

                        mContentValues.put(ContractClass.FeedEntry.MESSAGE_TYPE, BITVAULT_MESSAGE);
                    }

                    mContentValues.put(ContractClass.FeedEntry.ATTACHMENT, mAttachment);

                    return dbOperation.UpdateDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?", new String[]{rowID}, mContentValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Method used to start animation
     *
     * @param animView - view
     */
    private void enterRevel(LinearLayout animView) {

        int centerX = animView.getRight();
        int centerY = (animView.getTop() + animView.getBottom()) / 2;

        int startRadius = 0;
        int endRadius = (int) Math.hypot(animView.getWidth(), animView.getHeight());

        Animator anim =
                ViewAnimationUtils.createCircularReveal(animView, centerX, centerY, startRadius, endRadius);
        animView.setVisibility(View.VISIBLE);
        anim.start();
    }

    /**
     * Method used to end animation
     *
     * @param animView - view
     */
    void exitReveal(final LinearLayout animView) {
        // previously visible view
        int centerX = animView.getRight();
        int centerY = (animView.getTop() + animView.getBottom()) / 2;
        int startRadius = Math.max(animView.getWidth(), animView.getHeight());
        int endRadius = 0;

        Animator anim = ViewAnimationUtils.createCircularReveal(animView, centerX, centerY, startRadius, endRadius);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animView.setVisibility(View.GONE);
            }
        });
        anim.start();
    }

    /**
     * Methods to use selected contact number in the field
     *
     * @param mContactsModel -- contacts object
     */
    public void setSelectedContact(ContactsModel mContactsModel) {
        try {
            if(mContactsModel.getmReceiverAddress() != null && !mContactsModel.getmReceiverAddress().equalsIgnoreCase("")) {
                binding.receiverAddress.setText(mContactsModel.getmReceiverAddress());
                binding.receiverAddress.setSelection(mContactsModel.getmReceiverAddress().toString().length());
                binding.receiverAddress.dismissDropDown();
            }
            else{
                binding.receiverAddress.dismissDropDown();
                Utils.showSnakbar(binding.parentLayout, getResources().getString(R.string.no_receiver_addresss), Snackbar.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * For saving message a confirm dialog box will appear
     */
    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ChooseImageVideoBinding walletEmptyDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.choose_image_video, null, false);
        dialog.setContentView(walletEmptyDialogBinding.getRoot());
        walletEmptyDialogBinding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE,
                        System.currentTimeMillis() + ".png");
                mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                camIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(camIntent, CAMERA_PIC_REQUEST);

            }
        });
        walletEmptyDialogBinding.video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                camIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                // start the Video Capture Intent
                startActivityForResult(camIntent, CAMERA_VIDEO_REQUEST);
            }
        });
        dialog.show();

    }

    /**
     * Method to get video path from uri
     *
     * @param contentUri -- content uri
     * @return --- video path
     */
    public String getRealPathFromURI(Uri contentUri) {
        try {

            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to calculate the size of the attachments
     *
     * @return -- calculated size in KB
     */
    private long calculateSize() {

        long mSize = 0;

        if (mPath != null && mPath.size() > 0) {
            for (int fileSize = 0; fileSize < mPath.size(); fileSize++) {
                mSize += mPath.get(fileSize).getFileSize();
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
     *  * Method is used to ask run time permisions for read contacts
     **/
    private void askForPermission(){
        if (ContextCompat.checkSelfPermission(CreateMessage.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Utils.allocateRunTimePermissions(CreateMessage.this, new String[]{Manifest.permission.READ_CONTACTS},
                    new int[]{MY_PERMISSIONS_CONTACT});
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
            mContactsModel = Utils.createContactModelFromCursor(Utils.getContactsList(CreateMessage.this), CreateMessage.this);
            if (mContactsModel != null && mContactsModel.size() > 0) {
                AppPreferences.getInstance(CreateMessage.this).setContact(mContactsModel);
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
     *  * Method is used to select receiver address in case of B2A
     **/
    private void selectReceiverAdderess(Context context, final String[] WalletNames){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(TAG_CHOOSE_RECEIVER);

        builder.setItems(WalletNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedText = walletArrayB2A.get(WalletNames[which].toString());
                binding.receiverAddress.setText(selectedText);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}