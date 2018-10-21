package com.app.securemessaging.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.VolleyError;
import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.SpinnerAdapterAll;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.databinding.ActivityReceiveAddressBinding;
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

public class ReceiveAddressActivity extends AppCompatActivity implements View.OnClickListener,IAppConstants,WalletArrayCallback, WalletBalanceCallback,EotWalletCallback {

    private ActivityReceiveAddressBinding mActivityReceiveAddressBinding;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private EOTWalletManager mEotWalletManager = null;
    private ArrayList<WalletDetails> mRequestedWallets;
    private double mEotCoinsTotal = 0.0;
    private String mEotWalletAddress = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityReceiveAddressBinding = DataBindingUtil.setContentView(this, R.layout.activity_receive_address);
        mBitVaultWalletManager = BitVaultWalletManager.getWalletInstance();
        mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
        setData();
        getWalletsSDK();
        spinnerSelectListener();
    }

    /**
     * Method is used to set data
     */

    private void setData() {
        mActivityReceiveAddressBinding.toolbar.title.setText(getResources().getString(R.string.receive_Message));
        mActivityReceiveAddressBinding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);
        mActivityReceiveAddressBinding.qrScanner.setOnClickListener(this);
        mActivityReceiveAddressBinding.toolbar.walletInfoBack.setOnClickListener(this);

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
     mBitVaultWalletManager.getWallets(ReceiveAddressActivity.this);
    } catch (SecureSDKException e) {
     e.printStackTrace();
    } catch (Exception e) {
     e.printStackTrace();
    }
    }
   });
 }

    /**
     * Method to change wallet on select spinner
     */
    private void spinnerSelectListener(){
     mActivityReceiveAddressBinding.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position == 0){
                        mActivityReceiveAddressBinding.selectedWalletAdd.setText(mEotWalletAddress);
                      }else {
                        if(BitVaultWalletManager.getWalletInstance() != null) {
                            String walletId = mRequestedWallets.get(position - 1).getWALLET_ID();
                            try {
                                mActivityReceiveAddressBinding.selectedWalletAdd.setText(BitVaultWalletManager.getWalletInstance().getWalletAddress(Integer.parseInt(walletId)));
                            } catch (SecureSDKException e) {
                                e.printStackTrace();
                            }
                        }
                      }
                    }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
    }

      @Override
      public void getWallets(ArrayList<WalletDetails> requestedWallets) {
        if(requestedWallets != null) {
         if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
          mRequestedWallets.clear();
         }
         mRequestedWallets = requestedWallets;
         mEotWalletManager.getEotWallet(this);
        }else{
         Utils.showSnakbar(mActivityReceiveAddressBinding.rlMain,getResources().getString(R.string.walletFetchIssue), Snackbar.LENGTH_SHORT);
        }
     }
    @Override
    public void eotWallet(EotWalletDetail mEotWalletDetail) {
     if(mEotWalletDetail != null){
         mEotWalletAddress = mEotWalletDetail.getAddress();
         mEotWalletManager.getBalance(this);
        }else{
          Utils.showSnakbar(mActivityReceiveAddressBinding.rlMain,getResources().getString(R.string.walletFetchIssue), Snackbar.LENGTH_SHORT);
       }

    }

    @Override
    public void successWalletBalanceCallback(WalletBalanceModel mWalletBalanceModel) {
     if(mWalletBalanceModel.getmWalletBalance() != null){
      mEotCoinsTotal = Double.parseDouble(mWalletBalanceModel.getmWalletBalance());
     }else{
      Utils.showSnakbar(mActivityReceiveAddressBinding.rlMain,getResources().getString(R.string.EotBalNotUpdated),Snackbar.LENGTH_SHORT);
     }
        setSpinnerAdapter();
    }

    @Override
    public void failedWalletBalanceCallback(VolleyError mError) {
     Utils.showSnakbar(mActivityReceiveAddressBinding.rlMain,getResources().getString(R.string.EotBalNotUpdated),Snackbar.LENGTH_SHORT);
        setSpinnerAdapter();
    }

    /**
     * Method to set spinner adapter for all wallet
     */
   private void setSpinnerAdapter(){
    SpinnerAdapterAll dataAdapter = new SpinnerAdapterAll(this, R.layout.wallet_spinner_item, R.layout.spinner_layout,
            mRequestedWallets,mEotCoinsTotal);
    mActivityReceiveAddressBinding.sendSpinner.setAdapter(dataAdapter);
    mActivityReceiveAddressBinding.sendSpinner.setSelected(false);
   }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.qr_scanner:
                Intent intent = new Intent(ReceiveAddressActivity.this,ScanReceiverAddressActivity.class);
                intent.putExtra(QR_WALLET_ADDRESS,mActivityReceiveAddressBinding.selectedWalletAdd.getText().toString().trim());
                startActivity(intent);
                break;

            case R.id.wallet_info_back:
                onBackPressed();
                break;
            default:

                break;

        }

    }
}
