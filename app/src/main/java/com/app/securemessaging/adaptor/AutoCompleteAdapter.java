package com.app.securemessaging.adaptor;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.util.CircularImageView;
import com.app.securemessaging.view.activity.CreateMessage;
import com.app.securemessaging.view.activity.CreateMessageWithBitAttach;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to set the adaptor of inbox message.
 */
public class AutoCompleteAdapter extends ArrayAdapter implements IAppConstants, Filterable {
    LayoutInflater flater;
    private List<ContactsModel> selectFilesAdapters;
    private List<ContactsModel> mFilteredSelectFileList;
    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults filterResults = null;
            try {
                filterResults = new FilterResults();
                ArrayList<ContactsModel> tempList = new ArrayList<ContactsModel>();

                if (mFilteredSelectFileList != null && mFilteredSelectFileList.size() > 0) {
                    mFilteredSelectFileList.clear();
                }
                 int sizeSelectList = selectFilesAdapters.size();
                if (constraint != null && selectFilesAdapters != null) {

                    for (int i = 0; i < sizeSelectList; i++) {

                        ContactsModel mContact = selectFilesAdapters.get(i);
                        if (selectFilesAdapters != null && sizeSelectList > 0) {
                            if(mContact.getmReceiverAddress() != null && !mContact.getmReceiverAddress().equalsIgnoreCase("")){
                                if (mContact.getmName().toLowerCase().contains(constraint.toString().toLowerCase())
                                        || mContact.getmReceiverAddress().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                    tempList.add(mContact);
                                }
                            }
                            else{
                                if (mContact.getmName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                    tempList.add(mContact);
                                }
                            }


                        }
                    }
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            try {
                if(results.values!=null) {
                    mFilteredSelectFileList = (ArrayList) results.values;
                }else {
                    mFilteredSelectFileList = new ArrayList<>();
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Context mContext;
    private int mValueScreen = 0;
    public AutoCompleteAdapter(Activity context, int resouceId, List<ContactsModel> list,int valScreen) {
        super(context, resouceId, list);
        this.mContext = context;
        flater = context.getLayoutInflater();
        selectFilesAdapters = list;
        mFilteredSelectFileList = new ArrayList<>();
        this.mValueScreen = valScreen;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowview = null;

            ContactsModel rowItem = mFilteredSelectFileList.get(position);

            rowview = flater.inflate(R.layout.spinner_autocomplete_layout, parent, false);

            CircularImageView mImageView = (CircularImageView) rowview.findViewById(R.id.userimage);
            if (rowItem.getmImage() != null) {

                mImageView.setImageURI(Uri.parse(rowItem.getmImage()));
            }
            TextView userName = (TextView) rowview.findViewById(R.id.userName);
            userName.setText(rowItem.getmName());

            TextView userReceiverAddr = (TextView) rowview.findViewById(R.id.userReceiverAddress);
            userReceiverAddr.setText(rowItem.getmReceiverAddress());

            RelativeLayout mRelativeMain = (RelativeLayout) rowview.findViewById(R.id.relative_main);

            mRelativeMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mFilteredSelectFileList != null && mFilteredSelectFileList.size() > 0) {
                        if(mValueScreen == 1) {
                            ((CreateMessage) mContext).setSelectedContact(mFilteredSelectFileList.get(position));
                        }else if (mValueScreen == 2){
                            ((CreateMessageWithBitAttach) mContext).setSelectedContact(mFilteredSelectFileList.get(position));
                        }
                    }
                }
            });

        return rowview;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowview = null;
        try {
            ContactsModel rowItem = selectFilesAdapters.get(position);

            rowview = flater.inflate(R.layout.spinner_autocomplete_layout, parent, false);

            TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
            txtTitle.setText(rowItem.getmName());

            TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);
            mwalletBalance.setText(rowItem.getmPhone());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowview;
    }

    @Override
    public int getCount() {
        try {
            if(mFilteredSelectFileList != null) {
                return mFilteredSelectFileList.size();
            }
            else{
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
            return myFilter;
    }
}


