package com.app.securemessaging.view.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.BitattachinboxBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;
import com.app.securemessaging.util.Utils;

import qrcode.QRCodeManager;

/**
 * To show information of contact of the inbox
 */
public class BitAttachInboxActivity extends AppCompatActivity implements View.OnClickListener, IAppConstants {

    private BitattachinboxBinding mActivityInboxBinding;
    private MessageBeanHelper mMessageBeanHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityInboxBinding = DataBindingUtil.setContentView(this, R.layout.bitattachinbox);

        mActivityInboxBinding.toolbar.title.setText(getResources().getString(R.string.inbox));

        setListner();

        getIntentData();

    }

    /**
     * Method used to get data from intent
     */
    private void getIntentData() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            mMessageBeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);
            setData();
        }
    }

    /**
     * Method is used to set up real data
     *
     * @param  -- Selected Message
     */
    private void setData() {
        Bitmap bitmap = null;
        mActivityInboxBinding.time.setText(Utils.converDatewithSec(mMessageBeanHelper.getTimeInMillis()));
        mActivityInboxBinding.toolbar.title.setText(mMessageBeanHelper.getReceiverAddress().toString());
        if(mMessageBeanHelper.getTxdID() != null && !mMessageBeanHelper.getTxdID().isEmpty()
                && mMessageBeanHelper.getMessage() != null && !mMessageBeanHelper.getMessage().isEmpty()){
                bitmap = new QRCodeManager().showQRCodePopupForAddress(mMessageBeanHelper.getTxdID() + ":" + mMessageBeanHelper.getMessage());

        }
        if (bitmap != null) {
            mActivityInboxBinding.qrcode.setImageBitmap(bitmap);
        }

        ContentValues mContebtValues = new ContentValues();
        mContebtValues.put(ContractClass.FeedEntry.IS_NEW, FALSE);

        new CRUD_OperationOfDB(this).UpdateDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?", new String[]{
                mMessageBeanHelper.getId()
        }, mContebtValues);
    }
    /**
     * Intialize click listener
     */

    private void setListner() {
        mActivityInboxBinding.toolbar.walletInfoBack.setOnClickListener(this);
        mActivityInboxBinding.toolbar.deleteIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {

            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.delete_icon:
                showConfirmDialog();
                break;
        }

    }

    /**
     * To delete message a confirm dialog box will appear
     */
    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WalletEmptyDialogBinding walletEmptyDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.wallet_empty_dialog, null, false);
        dialog.setContentView(walletEmptyDialogBinding.getRoot());

        walletEmptyDialogBinding.title.setVisibility(View.VISIBLE);
        walletEmptyDialogBinding.title.setText(getResources().getString(R.string.confirmDialog));
        walletEmptyDialogBinding.okButton.setText(getResources().getString(R.string.yes));
        walletEmptyDialogBinding.cancelButton.setText(getResources().getString(R.string.no));
        walletEmptyDialogBinding.message.setText(getResources().getString(R.string.delete_msg));

        walletEmptyDialogBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                if (new CRUD_OperationOfDB(BitAttachInboxActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?",
                        new String[]{mMessageBeanHelper.getId()}) > 0) {
                    onBackPressed();
                } else {
                    Utils.showSnakbar(mActivityInboxBinding.rlMain, getResources().getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT);
                }

            }
        });
        walletEmptyDialogBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

}
