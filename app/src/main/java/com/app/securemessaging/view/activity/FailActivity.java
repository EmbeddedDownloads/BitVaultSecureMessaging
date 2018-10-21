package com.app.securemessaging.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.SendingFailedBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;
import com.app.securemessaging.util.Utils;

/**
 * Message Sending failed message.
 */

public class FailActivity extends AppCompatActivity implements View.OnClickListener, IAppConstants {

    private SendingFailedBinding mSendingFail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSendingFail = DataBindingUtil.setContentView(this, R.layout.sending_failed);

        mSendingFail.toolbar.title.setText(getResources().getString(R.string.sending_fail));
        mSendingFail.toolbar.walletInfoBack.setVisibility(View.GONE);

        setListner();

        setData();

    }

    /**
     * Set image for failure of sending message
     */
    private void setData() {

        mSendingFail.secureMsg.img.setImageResource(R.mipmap.messaging_error);
        mSendingFail.toolbar.deleteIcon.setVisibility(View.GONE);

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(ERRORMESSAGE) != null) {
            mSendingFail.secureMsg.txtSecureMsg.setText(getIntent().getExtras().getString(ERRORMESSAGE));
            mSendingFail.secureMsg.txtSecureMsg.setTextColor(getResources().getColor(R.color.red));
        } else {
            mSendingFail.secureMsg.txtSecureMsg.setText(getResources().getString(R.string.error_mesg_failed));
            mSendingFail.secureMsg.txtSecureMsg.setTextColor(getResources().getColor(R.color.red));
        }
    }

    /**
     * Intialize click listener
     */
    private void setListner() {

        mSendingFail.toolbar.walletInfoBack.setOnClickListener(this);
        mSendingFail.retryButton.setOnClickListener(this);
        mSendingFail.cancel.setOnClickListener(this);
    }


    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.retryButton:
                if (Utils.isNetworkConnected(this)) {
                    Intent sendingActivity = new Intent(FailActivity.this, SendingActivity.class);
                    sendingActivity.putExtras(getIntent().getExtras());
                    startActivity(sendingActivity);
                } else {
                    Utils.showSnakbar(mSendingFail.parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }

                break;

            case R.id.cancel:

                Intent homeIntent = new Intent(FailActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);

                break;
        }
    }

    /**
     * Confirm Dialog Box will appear
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

                Intent homeIntent = new Intent(FailActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        walletEmptyDialogBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                new CRUD_OperationOfDB(FailActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?",
                        new String[]{getIntent().getExtras().getString(ROW_ID)});

                Intent homeIntent = new Intent(FailActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        dialog.show();

    }
}
