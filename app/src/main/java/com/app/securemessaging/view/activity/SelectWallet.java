package com.app.securemessaging.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.WalletEotAdapter;
import com.app.securemessaging.adaptor.WalletListAdaptor;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.databinding.SelectWalletBinding;
import com.app.securemessaging.util.Utils;
import java.util.ArrayList;
import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import iclasses.WalletArrayCallback;
import model.WalletDetails;

/**
 * Selecting wallet for sending and receiving bitcoins.
 */

public class SelectWallet extends AppCompatActivity implements View.OnClickListener, IAppConstants, WalletArrayCallback {

    private SelectWalletBinding mSelectWalletBinding;
    private String from = "", mReceiverAddress = "",mWalletType = "";
    private ArrayList<WalletDetails> mRequestedWallets;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private String mEotWalletAddress = "";
    private double mEotCoinsTotal = 0.0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectWalletBinding = DataBindingUtil.setContentView(this, R.layout.select_wallet);
        mBitVaultWalletManager = BitVaultWalletManager.getWalletInstance();
        setData();

        setListner();

        getDatafromIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent() != null && getIntent().getStringExtra(WALLET_TYPE) != null){
            mWalletType = getIntent().getStringExtra(WALLET_TYPE);
            if(mWalletType !=null && mWalletType.equals(getResources().getString(R.string.wallet_eot))){
                if(getIntent() != null && getIntent().getStringExtra(EOT_WALLET_ADDRESS) != null){
                    mEotWalletAddress = getIntent().getStringExtra(EOT_WALLET_ADDRESS);
                }
                    mEotCoinsTotal = getIntent().getDoubleExtra(EOT_WALLET_BAL,0.0);
                    setAdapter(mEotCoinsTotal);
            }else{
                mRequestedWallets = new ArrayList<>();
                getWalletsSDK();
            }
        }
    }

    /**
     * Method is used to get data from the intent
     */
    private void getDatafromIntent() {

        if (getIntent() != null) {

            if (getIntent().getStringExtra(MESSAGE_TYPE) != null) {
                from = getIntent().getStringExtra(MESSAGE_TYPE);
            }

            if (getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                mReceiverAddress = getIntent().getStringExtra(RECEIVER_ADDRESS);
            }

            if (getIntent().getExtras() != null) {

                Bundle mBundle = getIntent().getExtras();
                MessageBeanHelper mMessageBeanHelper = (MessageBeanHelper) mBundle.getSerializable(MESSAGE_OBJECT);
                from = mBundle.getString(MESSAGE_TYPE);
            }
        }
    }

    /**
     * Intialize Click Listener
     */

    private void setListner() {

        mSelectWalletBinding.toolbar.walletInfoBack.setOnClickListener(this);
    }

    /**
     * Method is used to set Adapter
     *
     * @param walletBeanClassList - Arraylist of WalletBeanClass
     */
    private void setAdapter(ArrayList<WalletDetails> walletBeanClassList) {
        mSelectWalletBinding.rvContacts.setLayoutManager(new LinearLayoutManager(this));
        WalletListAdaptor adaptor = new WalletListAdaptor(walletBeanClassList,mSelectWalletBinding,SelectWallet.this);
        mSelectWalletBinding.rvContacts.setAdapter(adaptor);
    }

    /**
     * Method is used to set Adapter
     *
     * @param mEotCoinsTotal
     */
    private void setAdapter(double mEotCoinsTotal) {
        mSelectWalletBinding.rvContacts.setLayoutManager(new LinearLayoutManager(this));
        WalletEotAdapter adaptor = new WalletEotAdapter(mEotCoinsTotal,mSelectWalletBinding, SelectWallet.this);
        mSelectWalletBinding.rvContacts.setAdapter(adaptor);
    }

    /**
     * Method is used to set data
     */

    private void setData() {
        mSelectWalletBinding.toolbar.title.setText(getResources().getString(R.string.select_wallet));
        mSelectWalletBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);

    }


    /**
     * Method to get Wallets from SDK
     */
    private void getWalletsSDK() {

        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mBitVaultWalletManager.getWallets(SelectWallet.this);
                } catch (SecureSDKException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {

            case R.id.wallet_info_back:
                onBackPressed();
                break;
        }
    }

    /**
     * Method is called when user will click on any wallet from the list
     *
     * @param mWalletBeanClass -- selected wallet
     */
    public void navigateUser(WalletDetails mWalletBeanClass) {

        if (!from.equals("")) {
            switch (from) {

                case BITVAULT_MESSAGE:

                    Bundle mBundle = new Bundle();
                    Intent createMessage = new Intent(SelectWallet.this, CreateMessage.class);
                    mBundle.putSerializable(WALLET_DETAILS, mWalletBeanClass);

                    // this is used to sent message from draft
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                        mBundle.putString(FROM, SENT);
                        mBundle.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));

                    }
                    // this is used to reply message from draft
                    else if (getIntent() != null &&  getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                        mBundle.putString(FROM, REPLY);
                        mBundle.putString(RECEIVER_ADDRESS, getIntent().getStringExtra(RECEIVER_ADDRESS));
                    } else {
                        mBundle.putString(FROM, WALLET);
                    }
                    mBundle.putString(MESSAGE_TYPE,BITVAULT_MESSAGE);
                    mBundle.putString(WALLET_TYPE,mWalletType);
                    createMessage.putExtras(mBundle);
                    startActivity(createMessage);

                    break;

                case BITVAULT_MESSAGE_WITH_ATTACHMENT:

                    Bundle mBundleBitcoin = new Bundle();
                    Intent createBitcoinMessage = new Intent(SelectWallet.this, CreateMessage.class);
                    mBundleBitcoin.putSerializable(WALLET_DETAILS, mWalletBeanClass);
                    // this is used to sent message from draft
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                        mBundleBitcoin.putString(FROM, SENT);
                        mBundleBitcoin.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));
                    }
                    // this is used to sent message from reply
                    else if (getIntent() != null && getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                        mBundleBitcoin.putString(FROM, REPLY);
                        mBundleBitcoin.putString(RECEIVER_ADDRESS, getIntent().getStringExtra(RECEIVER_ADDRESS));
                    } else {
                        mBundleBitcoin.putString(FROM, WALLET);
                    }
                    mBundleBitcoin.putString(MESSAGE_TYPE,BITVAULT_MESSAGE);
                    mBundleBitcoin.putString(WALLET_TYPE, mWalletType);
                    createBitcoinMessage.putExtras(mBundleBitcoin);
                    startActivity(createBitcoinMessage);

                    break;

                case BITATTACH_MESSAGE:
                    Bundle mBundleBitAttach = new Bundle();
                    mBundleBitAttach.putSerializable(WALLET_DETAILS, mWalletBeanClass);
                    Intent createBitAttachMessage = new Intent(SelectWallet.this, CreateMessageWithBitAttach.class);
                    // this is used to sent message from draft
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                        mBundleBitAttach.putString(FROM, SENT);
                        mBundleBitAttach.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));
                    }
                    // this is used to sent message from reply
                    else if (getIntent() != null && getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                        mBundleBitAttach.putString(FROM, REPLY);
                        mBundleBitAttach.putString(RECEIVER_ADDRESS, getIntent().getStringExtra(RECEIVER_ADDRESS));
                    } else {
                        mBundleBitAttach.putString(FROM, WALLET);
                    }
                    mBundleBitAttach.putString(WALLET_TYPE, mWalletType);
                    createBitAttachMessage.putExtras(mBundleBitAttach);
                    startActivity(createBitAttachMessage);
                    break;

                case VAULT_TO_PC_MESSAGE:

                    try {
                        Bundle mBundleB2A = new Bundle();
                        Intent createMessageB2A = new Intent(SelectWallet.this, CreateMessage.class);
                        mBundleB2A.putSerializable(WALLET_DETAILS, mWalletBeanClass);

                        // this is used to sent message from draft
                        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                            mBundleB2A.putString(FROM, SENT);
                            mBundleB2A.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));

                            MessageBeanHelper mMessagebeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);
                            if (!mMessagebeanHelper.getReceiverAddress().equals(BitVaultWalletManager.getWalletInstance().getWalletAddress(Integer.parseInt(mWalletBeanClass.getWALLET_ID())))) {
                                mBundleB2A.putString(MESSAGE_TYPE, VAULT_TO_PC_MESSAGE);
                                createMessageB2A.putExtras(mBundleB2A);
                                startActivity(createMessageB2A);
                            } else {
                                Utils.showSnakbar(mSelectWalletBinding.parentLayout, getResources().getString(R.string.b2a_receverAddComp), Snackbar.LENGTH_SHORT);
                            }

                        } else {
                            mBundleB2A.putString(FROM, WALLET);
                            String[] walletsName = new String[4];
                            String[] walletsAddress = new String[4];
                            int len = 0;
                            int sizeWallet = mRequestedWallets.size();

                            for (int i = 0; i < sizeWallet; i++) {
                                if (!mRequestedWallets.get(i).getWALLET_ID().equals(mWalletBeanClass.getWALLET_ID())) {
                                    walletsAddress[len] = BitVaultWalletManager.getWalletInstance().
                                            getWalletAddress(Integer.parseInt(mRequestedWallets.get(i).getWALLET_ID()));

                                    walletsName[len] = mRequestedWallets.get(i).getWALLET_NAME();
                                    len++;
                                }
                            }
                            mBundleB2A.putStringArray(WALLET_ADDARRAY_B2A, walletsAddress);
                            mBundleB2A.putStringArray(WALLET_NAMEARRAY_B2A, walletsName);

                            mBundleB2A.putString(MESSAGE_TYPE, VAULT_TO_PC_MESSAGE);
                            mBundleB2A.putString(WALLET_TYPE, mWalletType);
                            createMessageB2A.putExtras(mBundleB2A);
                            startActivity(createMessageB2A);
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    break;

            }
        }
    }
    /**
     * Method is called when user will click on eot wallet
     *
     * @param position -- selected wallet
     */
    public void navigateUser(int position) {
        if (!from.equals("")) {
            switch (from) {

                case BITVAULT_MESSAGE:

                    Bundle mBundle = new Bundle();
                    Intent createMessage = new Intent(SelectWallet.this, CreateMessage.class);
                    mBundle.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                    mBundle.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);

                    // this is used to sent message from draft
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                        mBundle.putString(FROM, SENT);
                        mBundle.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));

                    }
                    // this is used to reply message from draft
                    else if (getIntent() != null && getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                        mBundle.putString(FROM, REPLY);
                        mBundle.putString(RECEIVER_ADDRESS, getIntent().getStringExtra(RECEIVER_ADDRESS));
                    } else {
                        mBundle.putString(FROM, WALLET);
                    }
                    mBundle.putString(MESSAGE_TYPE, BITVAULT_MESSAGE);
                    mBundle.putString(WALLET_TYPE, mWalletType);
                    createMessage.putExtras(mBundle);
                    startActivity(createMessage);

                    break;

                case BITVAULT_MESSAGE_WITH_ATTACHMENT:

                    Bundle mBundleBitcoin = new Bundle();
                    Intent createBitcoinMessage = new Intent(SelectWallet.this, CreateMessage.class);
                    mBundleBitcoin.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                    mBundleBitcoin.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                    // this is used to sent message from draft
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                        mBundleBitcoin.putString(FROM, SENT);
                        mBundleBitcoin.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));
                    }
                    // this is used to sent message from reply
                    else if (getIntent() != null && getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                        mBundleBitcoin.putString(FROM, REPLY);
                        mBundleBitcoin.putString(RECEIVER_ADDRESS, getIntent().getStringExtra(RECEIVER_ADDRESS));
                    } else {
                        mBundleBitcoin.putString(FROM, WALLET);
                    }
                    mBundleBitcoin.putString(MESSAGE_TYPE, BITVAULT_MESSAGE);
                    mBundleBitcoin.putString(WALLET_TYPE, mWalletType);
                    createBitcoinMessage.putExtras(mBundleBitcoin);
                    startActivity(createBitcoinMessage);

                    break;

                case BITATTACH_MESSAGE:
                    Bundle mBundleBitAttach = new Bundle();
                    mBundleBitAttach.putString(EOT_WALLET_ADDRESS, mEotWalletAddress);
                    mBundleBitAttach.putDouble(EOT_WALLET_BAL, mEotCoinsTotal);
                    Intent createBitAttachMessage = new Intent(SelectWallet.this, CreateMessageWithBitAttach.class);
                    // this is used to sent message from draft
                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
                        mBundleBitAttach.putString(FROM, SENT);
                        mBundleBitAttach.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));
                    }
                    // this is used to sent message from reply
                    else if (getIntent() != null && getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
                        mBundleBitAttach.putString(FROM, REPLY);
                        mBundleBitAttach.putString(RECEIVER_ADDRESS, getIntent().getStringExtra(RECEIVER_ADDRESS));
                    } else {
                        mBundleBitAttach.putString(FROM, WALLET);
                    }
                    mBundleBitAttach.putString(WALLET_TYPE, mWalletType);
                    createBitAttachMessage.putExtras(mBundleBitAttach);
                    startActivity(createBitAttachMessage);
                    break;
            }


        }
    }
        /**
         * Callback of getWallets from SDK
         *
         * @param mWallets - Wallet Details object
         */

    @Override
    public void getWallets(ArrayList<WalletDetails> mWallets) {

        if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
            mRequestedWallets.clear();
        }

        mRequestedWallets.addAll(mWallets);
        setAdapter(mRequestedWallets);
    }

}
