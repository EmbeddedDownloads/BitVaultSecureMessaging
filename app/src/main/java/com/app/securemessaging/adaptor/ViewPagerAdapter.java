package com.app.securemessaging.adaptor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.view.fragment.InboxFragment;
import com.app.securemessaging.view.fragment.SentFragment;

import java.util.HashMap;
import java.util.Map;

/**
 *  this class is used to set the adaptor of view pager.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter implements  IAppConstants{
    private String mDataHashTxId = "",mReceiverAddress ="",mTag = "";
    private boolean isFromNotification = false;
    private Map<Integer, String> mFragmentTags;
    private FragmentManager mFragmentManager;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
        mFragmentTags = new HashMap<Integer, String>();
    }

    public ViewPagerAdapter(FragmentManager supportFragmentManager, String mTag, String mDataHashTxId, String mReceiverAddress,boolean isFromNotification) {
        super(supportFragmentManager);
        this.mTag = mTag;
        this.mDataHashTxId = mDataHashTxId;
        this.mReceiverAddress = mReceiverAddress;
        this.isFromNotification = isFromNotification;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if(mDataHashTxId != null && !mDataHashTxId.equalsIgnoreCase("")){
                    Bundle bundle = new Bundle();
                    bundle.putString(IAppConstants.DATA_TAG,mTag);
                    bundle.putString(IAppConstants.TAG_NOTIFICATION_DATA,mDataHashTxId);
                    bundle.putString(IAppConstants.TAG_NOTIFICATION_RECEIVER,mReceiverAddress);
                    bundle.putBoolean(FROM_NOTIFICATION,isFromNotification);
                    InboxFragment inboxFragment = new InboxFragment();
                    inboxFragment.setArguments(bundle);
                    return inboxFragment;
                }
                else{
                    return new InboxFragment();
                }
            default:
                return new SentFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "Inbox";
        } else if (position == 1) {
            title = "Sent";
        }
        return title;
    }

   @Override
   public Object instantiateItem(ViewGroup container, int position) {
       Object object = super.instantiateItem(container, position);
       if (object instanceof Fragment) {
           Fragment fragment = (Fragment) object;
           String tag = fragment.getTag();
           if(mFragmentTags != null) {
               mFragmentTags.put(position, tag);
           }
       }
       return object;
   }

   // this method is used to get fragment to recreate it
    public Fragment getFragment(int position) {
        Fragment fragment = null;
        if(mFragmentTags != null) {
            String tag = mFragmentTags.get(position);

            if (tag != null) {
                fragment = mFragmentManager.findFragmentByTag(tag);
            }
        }
        return fragment;
    }

}