package com.app.securemessaging.view.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.InboxMessageOuterAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.bean.MessageBeanOuterHelper;
import com.app.securemessaging.database.AppPreferences;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.FragmentInboxBinding;
import com.app.securemessaging.service.ReceiveMessageService;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.CreateMessageWithBitAttach;
import com.app.securemessaging.view.activity.HomeActivity;

import java.util.ArrayList;
import java.util.List;

import utils.SDKUtils;

/**
 * View Pager class to show inbox messages
 */
public class InboxFragment extends Fragment implements IAppConstants {
    private ArrayList<MessageBeanHelper> messageBeanHelperNewList = new ArrayList<>();
    private ArrayList<MessageBeanHelper> messageBeanHelperOldList = new ArrayList<>();
    private List<MessageBeanOuterHelper> messageBeanHelperOuter = new ArrayList<>();
    private InboxMessageOuterAdapter adapter;
    private FragmentInboxBinding binding;
    private Cursor mCursor;
    private CRUD_OperationOfDB mDBOperations;

    private String mDataHashTxId = "",mReceiverAddress ="",mTag = "";
    private boolean isFromNotification = false;
    private boolean isNewExists = false;

    private MyReceiver myReceiver;

    private ArrayList<ContactsModel> mContactsModel;
    private static final int MY_PERMISSIONS_CONTACT = 101;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_inbox, container, false);

        Bundle b = getArguments();
        if(b != null) {
            mTag = b.getString(IAppConstants.DATA_TAG);
            mDataHashTxId = b.getString(IAppConstants.TAG_NOTIFICATION_DATA);
            mReceiverAddress = b.getString(IAppConstants.TAG_NOTIFICATION_RECEIVER);
            isFromNotification = b.getBoolean(FROM_NOTIFICATION, isFromNotification);
        }
        initData();
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (binding.swipeContainer.isRefreshing()) {
                    if (Utils.isNetworkConnected(getActivity())) {
                        new ReceiveMessageService().receiveStart(getActivity());
                    } else {
                        Utils.showSnakbar(binding.rlMain, getActivity().getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                    }
                } else {
                }
            }
        });

        return binding.getRoot();
    }

    /**
     * Method used to initialize the data
     */
    private void initData() {
        mContactsModel = new ArrayList<>();
        mContactsModel = AppPreferences.getInstance(getActivity()).getContact();
        if(mContactsModel != null && mContactsModel.size() > 0){
        }
        else{
            askForPermission();
        }

        mDBOperations = new CRUD_OperationOfDB(getActivity());
        myReceiver = new MyReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myReceiver != null) {
            getActivity().registerReceiver(myReceiver,
                    new IntentFilter("com.app.securemessaging.UpdateUI"));

        }
         if(isFromNotification){
             new ReceiveMessageService().receiveStart(getActivity(),mDataHashTxId,mReceiverAddress,mTag,0);
             isFromNotification = false;
         }
         else{
             new ReceiveMessageService().receiveStart(getActivity());
         }

        if (messageBeanHelperOuter != null && messageBeanHelperOuter.size() > 0) {
            messageBeanHelperOuter.clear();
        }

        getAllDataNewMessageFromDB();

        getAllDataOldMessageFromDB();

        if (messageBeanHelperOuter != null && messageBeanHelperOuter.size() > 0) {

            binding.scrollBelow.setVisibility(View.GONE);
            binding.scrollAbove.setVisibility(View.GONE);

            binding.emptyResult.emptyText.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new InboxMessageOuterAdapter(getActivity(), messageBeanHelperOuter, isNewExists,this,mContactsModel);
            } else {
                adapter.setUpdatedData(messageBeanHelperOuter);
            }
            binding.recyclerView.setAdapter(adapter);

        } else {
            binding.emptyResult.emptyText.setVisibility(View.VISIBLE);
            binding.scrollBelow.setVisibility(View.VISIBLE);
            binding.scrollAbove.setVisibility(View.GONE);
        }
    }


    /**
     * Method to get new messages from db
     */
    private void getAllDataNewMessageFromDB() {

        mCursor = mDBOperations.fetchData(ContractClass.FeedEntry.TABLE_NAME, null, ContractClass.FeedEntry.TYPE + "=? AND " + ContractClass.FeedEntry.IS_NEW + "=?", new String[]{INBOX, TRUE}, ContractClass.FeedEntry.TIME_IN_MILLI + " DESC");

        if (messageBeanHelperNewList == null) {
            messageBeanHelperNewList = new ArrayList<>();
        }

        if (messageBeanHelperNewList != null && messageBeanHelperNewList.size() > 0) {
            messageBeanHelperNewList.clear();
        }

        if (mCursor != null && mCursor.getCount() > 0) {

            mCursor.moveToFirst();

            do {

                MessageBeanHelper mMessageBeanHelper = new MessageBeanHelper();
                mMessageBeanHelper.setMessage(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.MESSAGE)));
                mMessageBeanHelper.setReceiverAddress(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.RECEIVER_ADDRESS)));
                mMessageBeanHelper.setSenderAddress(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.SENDER_ADDRESS)));
                mMessageBeanHelper.setTxdID(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TXD_ID)));
                mMessageBeanHelper.setTimeInMillis(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TIME_IN_MILLI)));
                mMessageBeanHelper.setStatus(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.STATUS)));
                mMessageBeanHelper.setTypeDraftSent(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TYPE)));
                mMessageBeanHelper.setmType(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.MESSAGE_TYPE)));
                mMessageBeanHelper.setId(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry._ID)));
                mMessageBeanHelper.setIs_new(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.IS_NEW)));
                mMessageBeanHelper.setmTag(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TAG)));
                mMessageBeanHelper.setmWalletType(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.WALLETTYPE)));

                if (mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.ATTACHMENT)) != null)
                    mMessageBeanHelper.setAttachment(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.ATTACHMENT)));

                messageBeanHelperNewList.add(mMessageBeanHelper);
            } while ((mCursor.moveToNext()));
        }

        if (messageBeanHelperNewList != null && messageBeanHelperNewList.size() > 0) {
            messageBeanHelperOuter.add(new MessageBeanOuterHelper(messageBeanHelperNewList.size() + " " + getActivity().getResources().getString(R.string.new_secure_message), messageBeanHelperNewList));
        }


    }

    /**
     * Method to get old messages from db
     */
    private void getAllDataOldMessageFromDB() {

        mCursor = mDBOperations.fetchData(ContractClass.FeedEntry.TABLE_NAME, null, ContractClass.FeedEntry.TYPE + "=? AND " + ContractClass.FeedEntry.IS_NEW + "=?", new String[]{INBOX, FALSE}, ContractClass.FeedEntry.TIME_IN_MILLI + " DESC");

        if (messageBeanHelperOldList == null) {
            messageBeanHelperOldList = new ArrayList<>();
        }

        if (messageBeanHelperOldList != null && messageBeanHelperOldList.size() > 0) {
            messageBeanHelperOldList.clear();
        }

        if (mCursor != null && mCursor.getCount() > 0) {

            mCursor.moveToFirst();


            do {

                MessageBeanHelper mMessageBeanHelper = new MessageBeanHelper();
                mMessageBeanHelper.setMessage(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.MESSAGE)));
                mMessageBeanHelper.setReceiverAddress(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.RECEIVER_ADDRESS)));
                mMessageBeanHelper.setSenderAddress(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.SENDER_ADDRESS)));
                mMessageBeanHelper.setTxdID(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TXD_ID)));
                mMessageBeanHelper.setTimeInMillis(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TIME_IN_MILLI)));
                mMessageBeanHelper.setStatus(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.STATUS)));
                mMessageBeanHelper.setTypeDraftSent(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TYPE)));
                mMessageBeanHelper.setmType(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.MESSAGE_TYPE)));
                mMessageBeanHelper.setId(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry._ID)));
                mMessageBeanHelper.setIs_new(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.IS_NEW)));
                mMessageBeanHelper.setmTag(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.TAG)));
                mMessageBeanHelper.setmWalletType(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.WALLETTYPE)));
                mMessageBeanHelper.setmMessageFee(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.MESSAGE_FEE)));
                mMessageBeanHelper.setmWalletName(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.WALLET_NAME)));

                if (mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.ATTACHMENT)) != null)
                    mMessageBeanHelper.setAttachment(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.ATTACHMENT)));

                messageBeanHelperOldList.add(mMessageBeanHelper);
            } while ((mCursor.moveToNext()));

        }

        if (messageBeanHelperOldList != null && messageBeanHelperOldList.size() > 0) {
            messageBeanHelperOuter.add(new MessageBeanOuterHelper(messageBeanHelperOldList.size() + " " + getActivity().getResources().getString(R.string.older_messages), messageBeanHelperOldList));
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }



    /**
     * Method is used to update UI if any new message received
     */
    public void updateUI() {

        if (binding.swipeContainer != null && binding.swipeContainer.isRefreshing()) {
            binding.swipeContainer.setRefreshing(false);
        }

        if (messageBeanHelperOuter != null && messageBeanHelperOuter.size() > 0) {
            messageBeanHelperOuter.clear();
        }

        getAllDataNewMessageFromDB();

        getAllDataOldMessageFromDB();

        if (messageBeanHelperOuter != null && messageBeanHelperOuter.size() > 0) {
            binding.scrollBelow.setVisibility(View.GONE);
            binding.scrollAbove.setVisibility(View.GONE);
            binding.emptyResult.emptyText.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new InboxMessageOuterAdapter(getActivity(), messageBeanHelperOuter, isNewExists,this,mContactsModel);
            } else {
                adapter.setUpdatedData(messageBeanHelperOuter);
            }
            binding.recyclerView.setAdapter(adapter);
        } else {
            binding.emptyResult.emptyText.setVisibility(View.VISIBLE);
            binding.scrollBelow.setVisibility(View.VISIBLE);
            binding.scrollAbove.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myReceiver != null) {
            getActivity().unregisterReceiver(myReceiver);
        }
    }

    /**
     * Class is used to receive broadcast if any new message will appear
     */
    public class MyReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            if (intent != null && intent.getExtras() != null && intent.getExtras().getString(FROM) != null) {
                if (intent.getExtras().getString(FROM).equals(UPDATE_MESSAGE)) {
                    updateUI();
                } else if (binding.swipeContainer != null && binding.swipeContainer.isRefreshing()) {
                    binding.swipeContainer.setRefreshing(false);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
                else if (intent.getExtras().getString(FROM).equals(FROM_NOTIFICATION)) {
                    if (((HomeActivity) getActivity()).isInboxSelected()) {
                        String tag = intent.getExtras().getString(IAppConstants.DATA_TAG);
                        String dataHashTxId = intent.getExtras().getString(IAppConstants.TAG_NOTIFICATION_DATA);
                        String receiverAddress = intent.getExtras().getString(IAppConstants.TAG_NOTIFICATION_RECEIVER);
                        int notification_Id = intent.getExtras().getInt(NOTIFICATION_ID);
                        new ReceiveMessageService().receiveStart(getActivity(), dataHashTxId, receiverAddress, tag, notification_Id);
                    }

                }
            }

        }
    }

    /**
     * method is used to get permission to read the contacts
     */
    private void askForPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Utils.allocateRunTimePermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    new int[]{MY_PERMISSIONS_CONTACT});
        }
    }

    /**   Method to get callback of permission
     * * @param requestCode
     * * @param permissions
     * * @param grantResults
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_CONTACT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new FetchContacts().execute();
                    return;
                }
            }
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
            mContactsModel = Utils.createContactModelFromCursor(Utils.getContactsList(getActivity()), getActivity());
            if (mContactsModel != null && mContactsModel.size() > 0) {
                AppPreferences.getInstance(getActivity()).setContact(mContactsModel);
                SDKUtils.showLog("Contacts", mContactsModel.size() + " ");
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}

