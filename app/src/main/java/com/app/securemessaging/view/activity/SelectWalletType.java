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

import com.android.volley.VolleyError;
import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.WalletTypeListAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.databinding.ActivitySelectWalletTypeBinding;
import com.app.securemessaging.util.Utils;

import java.util.ArrayList;

import bitmanagers.BitVaultWalletManager;
import bitmanagers.EOTWalletManager;
import commons.SecureSDKException;
import iclasses.EotCallbacks.EotWalletCallback;
import iclasses.WalletArrayCallback;
import iclasses.WalletBalanceCallback;
import model.WalletBalanceModel;
import model.WalletDetails;
import model.eotmodel.EotWalletDetail;
import utils.SDKUtils;

/**
 * Selecting wallet Type to do payment for message sending.
 */
public class SelectWalletType extends AppCompatActivity implements View.OnClickListener,IAppConstants,
        WalletArrayCallback, WalletBalanceCallback,EotWalletCallback {

    private ActivitySelectWalletTypeBinding mSelectWalletTypeBinding;
    private String from = "", mReceiverAddress = "";
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private EOTWalletManager mEotWalletManager = null;
    private double mBitcoinsTotal = 0.0;
    private double mEotCoinsTotal = 0.0;
    private String mEotWalletAddress = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectWalletTypeBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_wallet_type);
        mBitVaultWalletManager = BitVaultWalletManager.getWalletInstance();
        mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
        getWalletsSDK();
        setData();

        setListner();

        getDatafromIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        }
    }

    /**
     * Intialize Click Listener
     */

    private void setListner() {

        mSelectWalletTypeBinding.toolbar.walletInfoBack.setOnClickListener(this);
    }

    /**
     * Method is used to set Adapter
     *
     * @param mBitcoinsTotal
     * @param mEotCoinsTotal
     */
    private void setAdapter(double mBitcoinsTotal, double mEotCoinsTotal) {
        mSelectWalletTypeBinding.rvWalletType.setLayoutManager(new LinearLayoutManager(this));
        WalletTypeListAdapter adaptor = new WalletTypeListAdapter(mBitcoinsTotal,mEotCoinsTotal,SelectWalletType.this);
        mSelectWalletTypeBinding.rvWalletType.setAdapter(adaptor);
    }


    /**
     * Method is used to set data
     */

    private void setData() {

        mSelectWalletTypeBinding.toolbar.title.setText(getResources().getString(R.string.select_wallet_type));
        mSelectWalletTypeBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);

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
                    mBitVaultWalletManager.getWallets(SelectWalletType.this);
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
     * Method is called when user will click on any wallet type from the list
     *
     * @param walletTypePosition -- selected wallet type
     */
    public void navigateUser(int walletTypePosition) {
        Bundle mBundle = new Bundle();
        Intent selectWallet = new Intent(SelectWalletType.this, SelectWallet.class);

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getSerializable(MESSAGE_OBJECT) != null) {
            mBundle.putSerializable(MESSAGE_OBJECT, getIntent().getExtras().getSerializable(MESSAGE_OBJECT));
            selectWallet.putExtra(MESSAGE_TYPE, from);
        } else if (getIntent() != null &&  getIntent().getStringExtra(RECEIVER_ADDRESS) != null) {
            selectWallet.putExtra(MESSAGE_TYPE, from);
            selectWallet.putExtra(RECEIVER_ADDRESS, mReceiverAddress);
        }else{
            selectWallet.putExtra(MESSAGE_TYPE, from);
        }

         if(walletTypePosition == 0){
             if(mBitcoinsTotal <= 0){
                 Utils.showSnakbar(mSelectWalletTypeBinding.parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
             }else {
                 selectWallet.putExtra(WALLET_TYPE,getResources().getString(R.string.wallet_bitcoin));
                 selectWallet.putExtras(mBundle);
                 startActivity(selectWallet);
             }

         }else if(walletTypePosition == 1){
             if(mEotCoinsTotal <= 0){
                 Utils.showSnakbar(mSelectWalletTypeBinding.parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
             }else {
                 selectWallet.putExtra(WALLET_TYPE, getResources().getString(R.string.wallet_eot));
                 selectWallet.putExtra(EOT_WALLET_BAL, mEotCoinsTotal);
                 selectWallet.putExtra(EOT_WALLET_ADDRESS, mEotWalletAddress);
                 selectWallet.putExtras(mBundle);
                 startActivity(selectWallet);
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
        int walletListSize = mWallets.size();
        for (int i = 0; i <walletListSize ; i++) {
            mBitcoinsTotal += Double.parseDouble(mWallets.get(i).getWALLET_LAST_UPDATE_BALANCE());
        }
        mEotWalletManager.getEotWallet(this);

    }

    @Override
    public void successWalletBalanceCallback(WalletBalanceModel mWalletBalanceModel) {
        if(mWalletBalanceModel.getmWalletBalance() != null){
            mEotCoinsTotal = Double.parseDouble(mWalletBalanceModel.getmWalletBalance());
        }
        setAdapter(mBitcoinsTotal,mEotCoinsTotal);
    }

    @Override
    public void failedWalletBalanceCallback(VolleyError mError) {
        Utils.showSnakbar(mSelectWalletTypeBinding.parentLayout,getResources().getString(R.string.EotBalNotUpdated),Snackbar.LENGTH_SHORT);
        setAdapter(mBitcoinsTotal,mEotCoinsTotal);
    }

    @Override
    public void eotWallet(EotWalletDetail mEotWalletDetail) {
        if(mEotWalletDetail != null){
            mEotWalletAddress = mEotWalletDetail.getAddress();
        }else{
            Utils.showSnakbar(mSelectWalletTypeBinding.parentLayout,getResources().getString(R.string.EotBalNotUpdated),Snackbar.LENGTH_SHORT);
        }
        mEotWalletManager.getBalance(this);
    }
}
