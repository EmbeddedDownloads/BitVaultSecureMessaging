package com.app.securemessaging.view.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.SentMessageAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.database.AppPreferences;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.FragmentSentBinding;
import com.app.securemessaging.util.Utils;

import java.util.ArrayList;
import java.util.List;

import utils.SDKUtils;

/**
 * View Pager class to show sent messages
 */
public class SentFragment extends Fragment implements IAppConstants {

    private List<MessageBeanHelper> messageBeanHelperList;
    private SentMessageAdapter adapter;
    private FragmentSentBinding binding;
    private CRUD_OperationOfDB mDBOperations;
    private Cursor mCursor;
    private ArrayList<ContactsModel> mContactsModel;
    private static final int MY_PERMISSIONS_CONTACT = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sent, container, false);

        initData();

        return binding.getRoot();

    }


    @Override
    public void onResume() {
        super.onResume();

        getAllDataFromDB();

        if (messageBeanHelperList != null && messageBeanHelperList.size() > 0) {
            binding.emptyResult.emptyText.setVisibility(View.GONE);
            adapter = new SentMessageAdapter(getContext(), messageBeanHelperList,mContactsModel);
            binding.recyclerView.setAdapter(adapter);
        } else {
            binding.emptyResult.emptyText.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Method to initialize variables
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
        messageBeanHelperList = new ArrayList<>();
    }

    /**
     * Method to get data
     */
    private void getAllDataFromDB() {

        mCursor = mDBOperations.fetchData(ContractClass.FeedEntry.TABLE_NAME, null, ContractClass.FeedEntry.TYPE + "=? OR " + ContractClass.FeedEntry.TYPE + "=? OR " + ContractClass.FeedEntry.TYPE + "=?", new String[]{DRAFT, SENT, FAIL}, ContractClass.FeedEntry.TIME_IN_MILLI + " DESC");

        if (mCursor != null && mCursor.getCount() > 0) {

            mCursor.moveToFirst();

            if (messageBeanHelperList == null) {
                messageBeanHelperList = new ArrayList<>();
            }

            if (messageBeanHelperList != null && messageBeanHelperList.size() > 0) {
                messageBeanHelperList.clear();
            }

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
                mMessageBeanHelper.setAttachment(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.ATTACHMENT)));
                mMessageBeanHelper.setSessionKey(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.SESSIONKEY)));
                mMessageBeanHelper.setmWalletType(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.WALLETTYPE)));
                mMessageBeanHelper.setmMessageFee(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.MESSAGE_FEE)));
                mMessageBeanHelper.setmWalletName(mCursor.getString(mCursor.getColumnIndex(ContractClass.FeedEntry.WALLET_NAME)));

                messageBeanHelperList.add(mMessageBeanHelper);
            } while ((mCursor.moveToNext()));

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
