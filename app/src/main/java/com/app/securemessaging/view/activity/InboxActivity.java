package com.app.securemessaging.view.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.android.volley.VolleyError;
import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.ShowAttachmentAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.ActivityInboxBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;
import com.app.securemessaging.util.Utils;

import java.io.File;
import java.util.ArrayList;

import bitmanagers.EOTWalletManager;
import iclasses.EotCallbacks.EotWalletCallback;
import iclasses.WalletBalanceCallback;
import model.WalletBalanceModel;
import model.eotmodel.EotWalletDetail;

/**
 * To show information of contact of the inbox
 */
public class InboxActivity extends AppCompatActivity implements View.OnClickListener, IAppConstants, WalletBalanceCallback,EotWalletCallback {

    private ActivityInboxBinding mActivityInboxBinding;
    private String mMessageType = "";
    private MessageBeanHelper mMessageBeanHelper;
    private EOTWalletManager mEotWalletManager = null;
    private double mEotCoinsTotal = 0.0;
    private String mEotWalletAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
        mActivityInboxBinding = DataBindingUtil.setContentView(this, R.layout.activity_inbox);

        mActivityInboxBinding.toolbar.title.setText(getResources().getString(R.string.inbox));
        mEotWalletManager.getEotWallet(this);
        setListner();
        getIntentData();

    }

    /**
     * Method used to get data from intent
     */
    private void getIntentData() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            mMessageBeanHelper = (MessageBeanHelper) getIntent().getExtras().getSerializable(MESSAGE_OBJECT);
            setData(mMessageBeanHelper);
        }
    }

    /**
     * Method is used to set up real data
     *
     * @param mMessageBeanHelper -- Selected Message
     */
    private void setData(MessageBeanHelper mMessageBeanHelper) {

        this.mMessageBeanHelper = mMessageBeanHelper;
        mMessageType = mMessageBeanHelper.getmType();
        mActivityInboxBinding.time.setText(Utils.converDatewithSec(mMessageBeanHelper.getTimeInMillis()));
        mActivityInboxBinding.message.setText(mMessageBeanHelper.getMessage());

        mActivityInboxBinding.reply.setText("Reply to " + mMessageBeanHelper.getSenderAddress());
        mActivityInboxBinding.toolbar.title.setText(mMessageBeanHelper.getSenderAddress().toString());

        if (mMessageBeanHelper.getmType().equals(BITVAULT_MESSAGE)) {
            mActivityInboxBinding.llAttachment.setVisibility(View.GONE);
        } else if (mMessageBeanHelper.getmType().equals(BITVAULT_MESSAGE_WITH_ATTACHMENT)
                && mMessageBeanHelper.getAttachment() != null && !mMessageBeanHelper.getAttachment().equals("")) {

            ArrayList<ShowImageBean> mList = new ArrayList<>();

            mActivityInboxBinding.llAttachment.setVisibility(View.VISIBLE);

            String mFilesPath[] = mMessageBeanHelper.getAttachment().split(FILE_SEPERATOR_SPLIT);

            mActivityInboxBinding.recyclerAttachment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            for (int i = 0; i < mFilesPath.length; i++) {

                ShowImageBean mShowImageBean = new ShowImageBean();
                mShowImageBean.setImage(mFilesPath[i]);
                String extension = Utils.getFileExtension(new File(mFilesPath[i]));
                mShowImageBean.setType(extension);
                mList.add(mShowImageBean);
            }

            if (mList != null && mList.size() > 0) {
                ShowAttachmentAdapter mShowAttachment = new ShowAttachmentAdapter(mList, this);
                mActivityInboxBinding.recyclerAttachment.setAdapter(mShowAttachment);
            }
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
        mActivityInboxBinding.reply.setOnClickListener(this);

    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {

            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.reply:
                Intent selectWallet = new Intent(this, SelectWallet.class);
                selectWallet.putExtra(MESSAGE_TYPE, mMessageType);
                if(mMessageBeanHelper != null && mMessageBeanHelper.getSenderAddress() != null){
                    char startWith = mMessageBeanHelper.getSenderAddress().charAt(0);
                    if(startWith == 'E'){
                        selectWallet.putExtra(WALLET_TYPE,getResources().getString(R.string.wallet_eot));
                        selectWallet.putExtra(EOT_WALLET_BAL,mEotCoinsTotal);
                        selectWallet.putExtra(EOT_WALLET_ADDRESS,mEotWalletAddress);
                    }else{
                        selectWallet.putExtra(WALLET_TYPE,getResources().getString(R.string.wallet_bitcoin));
                    }
                    selectWallet.putExtra(RECEIVER_ADDRESS, mMessageBeanHelper.getSenderAddress());
                    startActivity(selectWallet);
                }

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

                if (new CRUD_OperationOfDB(InboxActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, ContractClass.FeedEntry._ID + "=?",
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

    @Override
    public void successWalletBalanceCallback(WalletBalanceModel mWalletBalanceModel) {
        if(mWalletBalanceModel != null) {
            mEotCoinsTotal = Double.parseDouble(mWalletBalanceModel.getmWalletBalance());
        }
    }

    @Override
    public void failedWalletBalanceCallback(VolleyError mError) {
    }

    @Override
    public void eotWallet(EotWalletDetail mEotWalletDetail) {
        if(mEotWalletDetail != null){
            mEotWalletAddress = mEotWalletDetail.getAddress();
            mEotWalletManager.getBalance(this);
        }

    }
}
