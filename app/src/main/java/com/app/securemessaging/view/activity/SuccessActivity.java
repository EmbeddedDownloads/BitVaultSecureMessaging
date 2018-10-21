package com.app.securemessaging.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.MessagesendingsuccessfulBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;

import model.WalletDetails;
import qrcode.QRCodeManager;

/**
 * Description of bitcoins deduction and transaction id of the message.
 */

public class SuccessActivity extends AppCompatActivity implements View.OnClickListener, IAppConstants {

    private MessagesendingsuccessfulBinding mSuccessfulSending;
    private Bundle mBundle;
    private String mRowId = "0";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSuccessfulSending = DataBindingUtil.setContentView(this, R.layout.messagesendingsuccessful);

        mSuccessfulSending.secureMsg.txtSecureMsg.setText(getResources().getString(R.string.msg_successful));

        mSuccessfulSending.toolbar.title.setText(getResources().getString(R.string.status));

        setListner();

        getDataFromIntent();

        setData();
    }

    /**
     * Method used to data from intent
     */
    private void getDataFromIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            mBundle = getIntent().getExtras();
        }
    }

    /**
     * Intialize Listener
     */
    private void setListner() {

        mSuccessfulSending.toolbar.walletInfoBack.setOnClickListener(this);
        mSuccessfulSending.button.setOnClickListener(this);
        mSuccessfulSending.rlTransaction.setOnClickListener(this);
    }

    /**
     * Successful send of image pic
     */

    private void setData() {

        mSuccessfulSending.secureMsg.img.setImageResource(R.mipmap.messaging_success);
        mSuccessfulSending.toolbar.deleteIcon.setVisibility(View.GONE);
        mSuccessfulSending.toolbar.walletInfoBack.setVisibility(View.GONE);

        if (mBundle != null) {

            if (mBundle.getString(FROM).equals(BITVAULTATTACH)) {
                mSuccessfulSending.secureMsg.txtSecureMsg.setText(getResources().getString(R.string.file_sent));
            } else if (mBundle.getString(FROM).equals(BITATTACH)) {
                mSuccessfulSending.secureMsg.txtSecureMsg.setText(getResources().getString(R.string.key_successful));
            } else {
                mSuccessfulSending.secureMsg.txtSecureMsg.setText(getResources().getString(R.string.msg_successful));
            }

            if(mBundle.getString(WALLET_TYPE) != null && mBundle.getString(WALLET_TYPE).
                    equals(getResources().getString(R.string.wallet_eot)) ){
                mSuccessfulSending.bitCoinsText.setText(mBundle.getString(MESSAGE_FEE) + " " +
                        getResources().getString(R.string.bitcoinText) + "\n" + getResources().getString(R.string.wallet_eotWallet));

            }else{
                if (mBundle.getSerializable(WALLET_DETAILS) != null) {
                    WalletDetails mWalletDetails = (WalletDetails) mBundle.getSerializable(WALLET_DETAILS);
                    mSuccessfulSending.bitCoinsText.setText(mBundle.getString(MESSAGE_FEE) + " " +
                            getResources().getString(R.string.bitcoinText) + "\n" + mWalletDetails.getWALLET_NAME());
                }
            }
            if(mBundle.getString(TXID) != null) {
                mSuccessfulSending.trans.setText(mBundle.getString(TXID));
                Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(mBundle.getString(TXID));
                if (bitmap != null) {
                    mSuccessfulSending.qrcode.setImageBitmap(bitmap);
                }
            }
            if (mBundle.getString(ROW_ID) != null) {
                mRowId = mBundle.getString(ROW_ID);
            }
        }
    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.wallet_info_back:

                onBackPressed();

                break;

            case R.id.button:

                onBackPressed();

                break;

            case R.id.rl_transaction:

                Intent mTransactionIntent = new Intent(this, TransactionQr.class);
                Bundle mBundle = new Bundle();
                if (this.mBundle != null && this.mBundle.getString(TXID) != null && !this.mBundle.getString(TXID).equals("")) {
                    mBundle.putString(TXID, this.mBundle.getString(TXID));
                }
                mTransactionIntent.putExtras(mBundle);
                startActivity(mTransactionIntent);
                break;
        }
    }

    /**
     * Confirm Dialog box pop up on press of back button
     */
    @Override
    public void onBackPressed() {

        showConfirmDialog();

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

                Intent homeIntent = new Intent(SuccessActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        walletEmptyDialogBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                new CRUD_OperationOfDB(SuccessActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?",
                        new String[]{mRowId});

                Intent homeIntent = new Intent(SuccessActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        dialog.show();

    }
}
