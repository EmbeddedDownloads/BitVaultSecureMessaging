package com.app.securemessaging.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.ShowAttachmentAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.ActivitySentMessageBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;
import com.app.securemessaging.util.Utils;

import java.io.File;
import java.util.ArrayList;

import qrcode.QRCodeManager;

/**
 * Description of sent message with address and transaction id.
 */
public class SentMessageActivity extends AppCompatActivity implements View.OnClickListener, IAppConstants {

    private ActivitySentMessageBinding mActivitySendMessage;
    private MessageBeanHelper mMessageBeanHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySendMessage = DataBindingUtil.setContentView(this, R.layout.activity_sent_message);

        mActivitySendMessage.toolbar.title.setText(getResources().getString(R.string.sent));

        setListner();

        getDataFromIntent();
    }

    /**
     * Method is used to get data from intent
     */
    private void getDataFromIntent() {

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle mBundle = getIntent().getExtras();

            if (mBundle.getSerializable(MESSAGE_OBJECT) != null) {
                mMessageBeanHelper = (MessageBeanHelper) mBundle.getSerializable(MESSAGE_OBJECT);
                setData(mMessageBeanHelper);
            }
        }
    }

    /**
     * Method used to set data related to that wallet
     *
     * @param mMessageBeanHelper
     */
    private void setData(MessageBeanHelper mMessageBeanHelper) {

        this.mMessageBeanHelper = mMessageBeanHelper;
        mActivitySendMessage.time.setText(Utils.converDatewithSec(mMessageBeanHelper.getTimeInMillis().toString()));
        if (mMessageBeanHelper.getTxdID() != null) {
            mActivitySendMessage.trans.setText(mMessageBeanHelper.getTxdID().toString());
        }
        mActivitySendMessage.toolbar.title.setText(mMessageBeanHelper.getReceiverAddress().toString());

        Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(mMessageBeanHelper.getTxdID());
        if (bitmap != null) {
            mActivitySendMessage.qrcode.setImageBitmap(bitmap);
        }

        if (mMessageBeanHelper.getmType().equals(BITVAULT_MESSAGE)) {
            mActivitySendMessage.message.setText(mMessageBeanHelper.getMessage().toString());

            mActivitySendMessage.llAttachment.setVisibility(View.VISIBLE);
            mActivitySendMessage.qrSessionKey.setVisibility(View.GONE);

        } else if(mMessageBeanHelper.getmType().equals(BITATTACH_MESSAGE)){
            mActivitySendMessage.qrSessionKey.setVisibility(View.GONE);
            mActivitySendMessage.message.setText("");
        }

        else if (mMessageBeanHelper.getmType().equals(BITVAULT_MESSAGE_WITH_ATTACHMENT)) {
            mActivitySendMessage.message.setText(mMessageBeanHelper.getMessage().toString());
            mActivitySendMessage.qrSessionKey.setVisibility(View.GONE);
            setAttchmentData();
        }
        else if(mMessageBeanHelper.getmType().equals(VAULT_TO_PC_MESSAGE)){
            mActivitySendMessage.message.setText(getResources().getString(R.string.linesB2ACase));
            mActivitySendMessage.qrSessionKey.setVisibility(View.VISIBLE);
            setAttchmentData();
        }
    }

    /**
     * Method is used to set data from attachment
     */
    private void setAttchmentData(){
        ArrayList<ShowImageBean> mList = new ArrayList<>();

        mActivitySendMessage.llAttachment.setVisibility(View.VISIBLE);

        String mFilesPath[] = mMessageBeanHelper.getAttachment().split(FILE_SEPERATOR_SPLIT);

        mActivitySendMessage.recyclerAttachment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        for (int i = 0; i < mFilesPath.length; i++) {

            ShowImageBean mShowImageBean = new ShowImageBean();
            mShowImageBean.setImage(mFilesPath[i]);
            mShowImageBean.setType(Utils.getFileExtension(new File(mFilesPath[i])));

            mList.add(mShowImageBean);
        }

        if (mList != null && mList.size() > 0) {
            ShowAttachmentAdapter mShowAttachment = new ShowAttachmentAdapter(mList, this);
            mActivitySendMessage.recyclerAttachment.setAdapter(mShowAttachment);
        }
    }

    /**
     * Intialize Click Listener
     */
    private void setListner() {
        mActivitySendMessage.toolbar.walletInfoBack.setOnClickListener(this);
        mActivitySendMessage.toolbar.deleteIcon.setOnClickListener(this);
        mActivitySendMessage.rlTransactionid.setOnClickListener(this);
        mActivitySendMessage.qrSessionKey.setOnClickListener(this);
    }

    @Override
    public void onClick(View mView) {
        switch (mView.getId()) {
            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.rl_transactionid:
                Intent mTransactionIntent = new Intent(this, TransactionQr.class);
                Bundle mBundle = new Bundle();
                if (mMessageBeanHelper != null && mMessageBeanHelper.getTxdID() != null && !mMessageBeanHelper.getTxdID().equals("")) {
                    mBundle.putString(TXID, this.mMessageBeanHelper.getTxdID());
                }
                mTransactionIntent.putExtras(mBundle);
                startActivity(mTransactionIntent);
                break;

            case R.id.delete_icon:
                showConfirmDialog();
                break;

            case R.id.qrSessionKey:
                Intent mB2AIntent = new Intent(this, TransactionQr.class);
                Bundle mB2ABundle = new Bundle();
                if (mMessageBeanHelper != null && mMessageBeanHelper.getTxdID() != null && !mMessageBeanHelper.getTxdID().equals("")) {
                    mB2ABundle.putString(TXID, this.mMessageBeanHelper.getTxdID());
                    mB2ABundle.putString(B2A_SESSIONKEY,this.mMessageBeanHelper.getSessionKey());
                }
                mB2AIntent.putExtras(mB2ABundle);
                startActivity(mB2AIntent);

                break;
        }
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
        walletEmptyDialogBinding.title.setText(getResources().getString(R.string.confirmDialog));
        walletEmptyDialogBinding.okButton.setText(getResources().getString(R.string.yes));
        walletEmptyDialogBinding.cancelButton.setText(getResources().getString(R.string.no));
        walletEmptyDialogBinding.message.setText(getResources().getString(R.string.delete_msg));

        walletEmptyDialogBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                if (new CRUD_OperationOfDB(SentMessageActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?",
                        new String[]{mMessageBeanHelper.getId()}) > 0) {
                    onBackPressed();
                } else {
                    Utils.showSnakbar(mActivitySendMessage.rlMain, getResources().getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT);
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
