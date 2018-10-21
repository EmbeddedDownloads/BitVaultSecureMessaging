package com.app.securemessaging.view.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.SpinnerAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.WalletInfoBinding;
import com.app.securemessaging.util.BitVaultManagerSingleton;
import com.app.securemessaging.util.Utils;

import org.spongycastle.pqc.asn1.ParSet;

import java.util.ArrayList;

import bitmanagers.BitVaultDataManager;
import iclasses.FeeCallback;
import iclasses.WalletArrayCallback;
import model.DataMoverModel;
import model.WalletDetails;
import utils.SDKUtils;

/**
 * Description of fees,transaction id and confirmation to proceed the process.
 */

public class WalletInformation extends AppCompatActivity implements View.OnClickListener, IAppConstants, FeeCallback, WalletArrayCallback {

    ArrayList<WalletDetails> mRequestedWallets;
    private WalletInfoBinding mWalletInfo;
    private WalletDetails mSelectedWallet;
    private BitVaultDataManager mDataMover;
    private DataMoverModel mDataMoverModel;
    private ArrayList<ShowImageBean> mArrImageBean;
    private String mReceiverAddress = "", mMessageType = "";
    private int mCharacter_count = 0;
    private Bundle mBundle;
    private double mMessageFee = 0;
    private String mSessionKeyBitAttch = "";
    private long mBitAttachSize = 0;
    private String mWalletType = "";
    private String mEotWalletAddress = "";
    private double mEotCoinsTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWalletInfo = DataBindingUtil.setContentView(this, R.layout.wallet_info);

        mWalletInfo.toolbar.title.setText(getResources().getString(R.string.information));

        initData();

        setListner();

        getIntentData();
        spinnerSelectListener();
    }

    /**
     * Method to change wallet on select spinner
     */
    private void spinnerSelectListener(){
        if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
            mWalletInfo.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    mSelectedWallet = mRequestedWallets.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }
    @Override
    protected void onResume() {
            if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                getwalletDetails();
            }
        super.onResume();
    }

    /**
     * Method is used to get the all wallet details
     */
    private void getwalletDetails() {
        try {
            BitVaultManagerSingleton.getInstance().getWallets(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is used to initialize Data
     */
    private void initData() {

        mDataMover = BitVaultDataManager.getSecureMessangerInstance();
        mDataMoverModel = new DataMoverModel();

        if(getIntent().getExtras() != null) {
            mMessageType = getIntent().getExtras().getString(FROM);
        }

        mWalletInfo.toolbar.deleteIcon.setVisibility(View.INVISIBLE);

        mBundle = getIntent().getExtras();

    }

    /**
     * Method is used to get data from the intent
     */
    private void getIntentData() {

        if (getIntent() != null && getIntent().getExtras() != null) {
            mWalletType = getIntent().getStringExtra(WALLET_TYPE);
            getDataFromIntentInAllCase(mMessageType);
        }
    }

    /**
     * Method used to get the message fees
     */
    private void getMessageFee(String messageType) {

        if (mDataMover != null) {
            if (Utils.isNetworkConnected(this)) {
                if(messageType.equalsIgnoreCase(BITVAULT)) {
                    mDataMover.getFee(mCharacter_count, calculateSize(), this);
                }else if (messageType.equalsIgnoreCase(BITVAULTATTACH)){
                    mDataMover.getFee(mCharacter_count, calculateSize(), this);
                }
                else if(messageType.equalsIgnoreCase(BITATTACH)){
                    mDataMover.getFee(0, mBitAttachSize, this);
                }
            } else {
                Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
            }
        }
    }

    /**
     * Intialize listener
     */

    private void setListner() {
        mWalletInfo.toolbar.walletInfoBack.setOnClickListener(this);
        mWalletInfo.sendButton.setOnClickListener(this);
        mWalletInfo.cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.send_button:


                if (mWalletType.equals(getResources().getString(R.string.wallet_bitcoin))) {
                    if (Double.parseDouble(mSelectedWallet.getWALLET_LAST_UPDATE_BALANCE()) >= Double.parseDouble(mWalletInfo.transactionFee.getText().toString().replace("BTC", ""))) {
                        performClickOperation(mMessageType);
                    } else {
                        Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
                    }
                }
                else if(mWalletType.equals(getResources().getString(R.string.wallet_eot))){
                    if(mEotCoinsTotal >= Double.parseDouble(mWalletInfo.transactionFee.getText().toString().replace("EOT", ""))){
                        performClickOperation(mMessageType);
                    }else {
                        Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
                    }
                }
                else if(mWalletInfo.transactionFee.getText().toString().equals(getResources().getString(R.string.dummy_fee))
                         || mWalletInfo.transactionFee.getText().toString().equals(getResources().getString(R.string.dummy_fee_eot))){
                    Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.message_fee_cannot_be_zero), Snackbar.LENGTH_SHORT);
                }

                break;

            case R.id.cancel:
                Intent homeActivity = new Intent(this, HomeActivity.class);
                homeActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeActivity);
                break;
        }
    }

    /**
     * Method to perform click operation according to the message type
     *
     * @param mMessageType -- message type
     */
    private void performClickOperation(String mMessageType) {

        switch (mMessageType) {
            case BITVAULT:

                if (Utils.isNetworkConnected(this)) {

                    Intent sendMessage = new Intent(this, SendingActivity.class);
                    if (mBundle == null) {
                        mBundle = getIntent().getExtras();
                    }
                    mBundle.putString(FROM, getIntent().getExtras().getString(FROM));
                    if (mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        mBundle.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                        mBundle.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                    }else {
                        mBundle.putSerializable(WALLET_DETAILS, mSelectedWallet);
                    }
                    mBundle.putString(WALLET_TYPE,mWalletType);
                    mBundle.putString(MESSAGE_FEE, mWalletInfo.transactionFee.getText().toString());
                    sendMessage.putExtras(mBundle);

                    startActivity(sendMessage);
                } else {
                    Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }


                break;

            case BITATTACH:

                if (Utils.isNetworkConnected(this)) {

                    Intent sendMessage = new Intent(this, SendingActivity.class);
                    if (mBundle == null) {
                        mBundle = getIntent().getExtras();
                    }
                    mBundle.putString(FROM, getIntent().getExtras().getString(FROM));

                    if (mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        mBundle.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                        mBundle.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                    }else {
                        mBundle.putSerializable(WALLET_DETAILS, mSelectedWallet);
                    }
                    mBundle.putString(MESSAGE_FEE, mWalletInfo.transactionFee.getText().toString());
                    mBundle.putString(WALLET_TYPE,mWalletType);
                    mBundle.putString(BitAttachSessionKey,mSessionKeyBitAttch);
                    mBundle.putLong(BitAtachSize,mBitAttachSize);

                    sendMessage.putExtras(mBundle);
                    startActivity(sendMessage);
                }else {
                    Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }

                break;

            case BITVAULTATTACH:

                if (Utils.isNetworkConnected(this)) {

                    Intent sendMessage = new Intent(this, SendingActivity.class);
                    if (mBundle == null) {
                        mBundle = getIntent().getExtras();
                    }
                    mBundle.putString(FROM, getIntent().getExtras().getString(FROM));

                    if (mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        mBundle.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                        mBundle.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                    }else {
                        mBundle.putSerializable(WALLET_DETAILS, mSelectedWallet);
                    }
                    mBundle.putString(WALLET_TYPE,mWalletType);
                    mBundle.putString(MESSAGE_FEE, mWalletInfo.transactionFee.getText().toString());
                    sendMessage.putExtras(mBundle);
                    startActivity(sendMessage);
                } else {
                    Utils.showSnakbar(mWalletInfo.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }

                break;
        }
    }

    /**
     * Method will evaluate date according to message type
     *
     * @param mMessagetype -- type of the message it is
     */
    public void getDataFromIntentInAllCase(final String mMessagetype) {
        switch (mMessagetype) {
            case BITVAULT:

                if (getIntent().getExtras() != null) {

                    Bundle mBundleData = getIntent().getExtras();

                    if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                       if(getIntent().getExtras().getSerializable(WALLET_DETAILS) != null) {
                           mSelectedWallet = (WalletDetails) mBundleData.getSerializable(WALLET_DETAILS);
                       }
                        mWalletInfo.transactionFee.setText(getResources().getString(R.string.dummy_fee));
                        mWalletInfo.walletEot.setVisibility(View.GONE);
                        mWalletInfo.sendSpinner.setVisibility(View.VISIBLE);
                    }else{
                        if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                            mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                            mEotCoinsTotal = getIntent().getDoubleExtra(EOT_WALLET_BAL, 0.0);
                        }
                        mWalletInfo.walletEot.setVisibility(View.VISIBLE);
                        mWalletInfo.sendSpinner.setVisibility(View.GONE);
                        mWalletInfo.walletEot.setText(getResources().getString(R.string.wallet_eotWallet) + " - "
                                + String.valueOf(mEotCoinsTotal) + " " + getResources().getString(R.string.wallet_eot));
                        mWalletInfo.transactionFee.setText(getResources().getString(R.string.dummy_fee_eot));

                    }
                    mReceiverAddress = mBundleData.getString(RECEIVER_ADDRESS);
                    mArrImageBean = (ArrayList<ShowImageBean>) mBundleData.getSerializable(SELECTED_LIST);
                    mCharacter_count = mBundleData.getInt(CHARACTER_COUNT);

                    calculateSize();

                    mWalletInfo.size.setText(Utils.convertKBIntoHigherUnit(mCharacter_count * 2));

                    getMessageFee(mMessageType);
                }

                break;

            case BITATTACH:
                if (getIntent().getExtras() != null) {

                    Bundle mBundleData = getIntent().getExtras();

                    if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        if(getIntent().getExtras().getSerializable(WALLET_DETAILS) != null) {
                            mSelectedWallet = (WalletDetails) mBundleData.getSerializable(WALLET_DETAILS);
                        }
                        mWalletInfo.transactionFee.setText(getResources().getString(R.string.dummy_fee));
                        mWalletInfo.walletEot.setVisibility(View.GONE);
                        mWalletInfo.sendSpinner.setVisibility(View.VISIBLE);
                    }else{
                        if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                            mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                            mEotCoinsTotal = getIntent().getDoubleExtra(EOT_WALLET_BAL, 0.0);
                        }
                        mWalletInfo.walletEot.setVisibility(View.VISIBLE);
                        mWalletInfo.sendSpinner.setVisibility(View.GONE);
                        mWalletInfo.walletEot.setText(getResources().getString(R.string.wallet_eotWallet) + " - "
                                + String.valueOf(mEotCoinsTotal) + " " + getResources().getString(R.string.wallet_eot));
                        mWalletInfo.transactionFee.setText(getResources().getString(R.string.dummy_fee_eot));

                    }
                    mReceiverAddress = mBundleData.getString(RECEIVER_ADDRESS);
                    mSessionKeyBitAttch = mBundleData.getString(MESSAGE);
                    mWalletInfo.size.setText(Utils.convertKBIntoHigherUnitDouble(mBundleData.getDouble(BitAtachSize)));
                    mBitAttachSize = (long) mBundleData.getDouble(BitAtachSize) /1024;
                    if(mBitAttachSize <= 0){
                        mBitAttachSize = 1;
                    }
                    getMessageFee(mMessageType);
                }

                break;

            case BITVAULTATTACH:

                if (getIntent().getExtras() != null) {

                    Bundle mBundleData = getIntent().getExtras();

                    if (mWalletType != null && !mWalletType.isEmpty() && !mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                        if(getIntent().getExtras().getSerializable(WALLET_DETAILS) != null) {
                            mSelectedWallet = (WalletDetails) mBundleData.getSerializable(WALLET_DETAILS);
                        }
                        mWalletInfo.transactionFee.setText(getResources().getString(R.string.dummy_fee));
                        mWalletInfo.walletEot.setVisibility(View.GONE);
                        mWalletInfo.sendSpinner.setVisibility(View.VISIBLE);
                    }else{
                        if (getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null) {
                            mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                            mEotCoinsTotal = getIntent().getDoubleExtra(EOT_WALLET_BAL, 0.0);
                        }
                        mWalletInfo.walletEot.setVisibility(View.VISIBLE);
                        mWalletInfo.sendSpinner.setVisibility(View.GONE);
                        mWalletInfo.walletEot.setText(getResources().getString(R.string.wallet_eotWallet) + " - "
                                + String.valueOf(mEotCoinsTotal) + " " + getResources().getString(R.string.wallet_eot));
                        mWalletInfo.transactionFee.setText(getResources().getString(R.string.dummy_fee_eot));

                    }
                    mReceiverAddress = mBundleData.getString(RECEIVER_ADDRESS);
                    mArrImageBean = (ArrayList<ShowImageBean>) mBundleData.getSerializable(SELECTED_LIST);
                    mCharacter_count = mBundleData.getInt(CHARACTER_COUNT);

                    calculateSize();

                    getMessageFee(mMessageType);
                }
                break;
        }
    }


    /**
     * Method will evaluate date according to message type
     *
     * @param mMessagetype -- type of the message it is
     */
    public Bundle getDataFromIntentInAllCase(final String mMessagetype, MessageBeanHelper mMessageBeanHelper) {

        Bundle mBundleData = null;

        switch (mMessagetype) {
            case BITVAULT:

                if (getIntent().getExtras() != null) {

                    mBundleData = getIntent().getExtras();

                    mMessageBeanHelper.setReceiverAddress(mBundleData.getString(RECEIVER_ADDRESS));
                    mMessageBeanHelper.setMessage(mBundleData.getString(MESSAGE));
                    mMessageBeanHelper.setId(mBundleData.getString(ROW_ID));
                    ArrayList<ShowImageBean> mList = (ArrayList<ShowImageBean>) mBundleData.getSerializable(SELECTED_LIST);
                    String mAttachment = "";
                    int sizeList = mList.size();
                    if (mList != null && sizeList > 0) {
                        for (int i = 0; i < sizeList; i++) {
                            mAttachment += mList.get(i).getImage() + IAppConstants.FILE_SEPERATOR;
                        }
                    }
                    mMessageBeanHelper.setAttachment(mAttachment);

                    mBundleData.putSerializable(MESSAGE_OBJECT, mMessageBeanHelper);
                }

                break;

            case BITATTACH:
                break;

            case PHONE_TO_DESKTOP:
                break;
        }

        return mBundleData;
    }


    /**
     * Method is used the spinner
     */

    private void setSpinner(ArrayList<WalletDetails> mRequestedWallets) {

        int position = 0;

        for (int i = 0; i < mRequestedWallets.size(); i++) {
            if (mRequestedWallets.get(i).getWALLET_ID().equals(mSelectedWallet.getWALLET_ID())) {
                mSelectedWallet = mRequestedWallets.get(i);
                position = i;
            }
        }

        if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
            SpinnerAdapter dataAdapter = new SpinnerAdapter(this, R.layout.wallet_spinner_item, R.layout.spinner_layout, mRequestedWallets);
            mWalletInfo.sendSpinner.setAdapter(dataAdapter);
            mWalletInfo.sendSpinner.setSelection(position);
        }
    }

    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWalletsList) {

        if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
            mRequestedWallets.clear();
        }
        mRequestedWallets = mRequestedWalletsList;
        setSpinner(mRequestedWallets);
    }

    /**
     * Call method of message fee from sdk
     *
     * @param status
     * @param message
     * @param bitcoins
     */
    @Override
    public void getFeeCallBack(String status, String message, double bitcoins) {
        if (status.equalsIgnoreCase(STATUS_OK)) {
            mMessageFee = bitcoins;
            SDKUtils.showLog("FEE", bitcoins + "");
            String value = Utils.convertDecimalFormatPattern(bitcoins);
            if (!mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                mWalletInfo.transactionFee.setText(value + " " + getResources().getString(R.string.btc));
            }else{
                mWalletInfo.transactionFee.setText(value + " " +
                        getResources().getString(R.string.wallet_eot));
            }
            saveFeeToDatabase(mWalletInfo.transactionFee.getText().toString().trim());
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
            mWalletInfo.size.setText(Utils.convertKBIntoHigherUnit(mSize));
            mSize = mSize / 1024;
        } else {
            mSize = 0;
        }

        return mSize;

    }

    @Override
    public void onBackPressed() {

        if (mMessageType != null && mMessageType.equalsIgnoreCase(BITVAULT)) {
            MessageBeanHelper mMessageBeanHelper = new MessageBeanHelper();
            Intent mCreateMessageIntent = new Intent(this, CreateMessage.class);
            mCreateMessageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle mBundle = getDataFromIntentInAllCase(mMessageType, mMessageBeanHelper);
            mBundle.putString(FROM, SENT);
            mCreateMessageIntent.putExtras(mBundle);
            startActivity(mCreateMessageIntent);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Method to save message Fee in local database
     *
     */
    private void saveFeeToDatabase(String messageFee){
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(ContractClass.FeedEntry.MESSAGE_FEE, messageFee);
        if(mSelectedWallet != null){
            mContentValues.put(ContractClass.FeedEntry.WALLET_NAME, mSelectedWallet.getWALLET_NAME());
        }
        new CRUD_OperationOfDB(this).UpdateDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?", new String[]{getIntent().getExtras().getString(ROW_ID)}, mContentValues);

    }
}