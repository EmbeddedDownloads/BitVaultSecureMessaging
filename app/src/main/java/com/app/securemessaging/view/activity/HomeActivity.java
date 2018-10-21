package com.app.securemessaging.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.OvershootInterpolator;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.ViewPagerAdapter;
import com.app.securemessaging.appinterface.DeleteSelectedItems;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.database.CRUD_OperationOfDB;
import com.app.securemessaging.database.ContractClass;
import com.app.securemessaging.databinding.ActivityHomeBinding;
import com.app.securemessaging.databinding.WalletEmptyDialogBinding;

import java.util.ArrayList;

import bitmanagers.BitVaultDataManager;

/**
 * Landing Page.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener, IAppConstants {

    private ViewPagerAdapter viewPagerAdapter;
    private ActivityHomeBinding binding;
    private ArrayList<String> mSelectedItemIds;
    private DeleteSelectedItems mDeleteSelectedItems;

    private boolean isFromNotification = false;
    private String mDataHashTxId = "", mReceiverAddress = "", mTag = "";
    private boolean isInboxScreen = true;
    private String mContactReceiverAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setValuesFromIntent();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initialise_viewPager();
        binding.toolbar.title.setText(getResources().getString(R.string.secure_messaging));
        binding.toolbar.walletInfoBack.setVisibility(View.INVISIBLE);
        binding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);

        setListner();

        setFloatingMenuButton();

        createCustomAnimation();
    }

    /**
     * Initialise and set floating menu listeners
     */
    private void setFloatingMenuButton() {
        binding.menuRed.setClosedOnTouchOutside(true);
        binding.menuRed.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.menuRed.isOpened()) {

                    Intent selectWallet = new Intent(HomeActivity.this, SelectWalletType.class);
                    selectWallet.putExtra(MESSAGE_TYPE, BITVAULT_MESSAGE);
                    if (mContactReceiverAddress != null && !mContactReceiverAddress.equals("")) {
                        selectWallet.putExtra(RECEIVER_ADDRESS, mContactReceiverAddress);
                    }
                    hideFloatButtons();
                    startActivity(selectWallet);


                }

                binding.menuRed.toggle(true);
            }
        });
    }

    /**
     * Set values received from Intent
     */
    private void setValuesFromIntent() {
        if ( getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_DATA) != null) {
                isFromNotification = getIntent().getExtras().getBoolean(FROM_NOTIFICATION);
                mDataHashTxId = getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_DATA);
                mReceiverAddress = getIntent().getExtras().getString(IAppConstants.TAG_NOTIFICATION_RECEIVER);
                mTag = getIntent().getExtras().getString(IAppConstants.DATA_TAG);
            } else if (getIntent().getExtras().getString(IAppConstants.TAG_RECEIVERADD_CONTACT) != null) {
                mContactReceiverAddress = getIntent().getExtras().getString(IAppConstants.TAG_RECEIVERADD_CONTACT);
            }
        }
    }

    /**
     * Initialize View Pager Listerners
     */
    private void initialise_viewPager(){

        binding.viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mSelectedItemIds != null && mSelectedItemIds.size() > 0) {
                    mSelectedItemIds.clear();
                }
                int lPosition = 0;
                if (position == 0) {
                    lPosition = 1;
                    isInboxScreen = true;
                } else {
                    lPosition = 0;
                    isInboxScreen = false;
                }
                final Fragment fragment = viewPagerAdapter.getFragment(lPosition);
                if (fragment != null) {
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            fragment.onResume();
                        }
                    };
                    mainHandler.postDelayed(myRunnable, HOME_ACTIVITY_RUNNABLE);

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        if (isFromNotification) {
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mTag, mDataHashTxId, mReceiverAddress, isFromNotification);
        } else {
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        }

        binding.viewPager.setAdapter(viewPagerAdapter);


        binding.tabLayout.setupWithViewPager(binding.viewPager);

    }

    /**
     * Intialize click listener
     */
    private void setListner() {
        binding.toolbar.walletInfoBack.setOnClickListener(this);
        binding.fabBitattach.setOnClickListener(this);
        binding.fabBitattachpc.setOnClickListener(this);
        binding.fabBitattachDeviceToken.setOnClickListener(this);
        binding.toolbar.deleteIcon.setOnClickListener(this);
        binding.fabReceiveMessage.setOnClickListener(this);
    }

    /**
     * method to return selected screen status
     */

    public boolean isInboxSelected() {
        return isInboxScreen;
    }

    @Override
    public void onClick(View mView) {

        Intent selectWallet = new Intent(HomeActivity.this, SelectWalletType.class);
        switch (mView.getId()) {
            case R.id.wallet_info_back:
                onBackPressed();
                break;

            case R.id.fab_bitattach:
                if (binding.menuRed.isOpened())
                    binding.menuRed.close(true);

                selectWallet.putExtra(MESSAGE_TYPE, BITATTACH_MESSAGE);
                if (mContactReceiverAddress != null && !mContactReceiverAddress.equals("")) {
                    selectWallet.putExtra(RECEIVER_ADDRESS, mContactReceiverAddress);
                }
                hideFloatButtons();
                startActivity(selectWallet);


                break;

            case R.id.fab_bitattachpc:
                Intent selectWalletBit = new Intent(HomeActivity.this, SelectWallet.class);
                if (binding.menuRed.isOpened())
                    binding.menuRed.close(true);

                selectWalletBit.putExtra(MESSAGE_TYPE, VAULT_TO_PC_MESSAGE);
                if (mContactReceiverAddress != null && !mContactReceiverAddress.equals("")) {
                    selectWalletBit.putExtra(RECEIVER_ADDRESS, mContactReceiverAddress);
                }
                selectWalletBit.putExtra(WALLET_TYPE,getResources().getString(R.string.wallet_bitcoin));
                hideFloatButtons();
                startActivity(selectWalletBit);


                break;

            case R.id.fab_bitattachDeviceToken:
                hideFloatButtons();
                Intent device_register = new Intent(HomeActivity.this, DeviceTokenActivity.class);
                startActivity(device_register);
                break;

            case R.id.fab_receiveMessage:
                hideFloatButtons();
                Intent receiverAddress = new Intent(HomeActivity.this, ReceiveAddressActivity.class);
                startActivity(receiverAddress);
                break;

            case R.id.delete_icon:

                showConfirmDialog();

                break;
        }

    }


    /**
     * This method is used to delete entry from the table
     */
    public void deleteEntry() {
        try {
            if (mSelectedItemIds != null && mSelectedItemIds.size() > 0) {
                String arr[] = new String[mSelectedItemIds.size()];
                String selection = "";

                for (int i = 0; i < mSelectedItemIds.size(); i++) {
                    arr[i] = mSelectedItemIds.get(i);
                    if (i == mSelectedItemIds.size() - 1) {
                        selection += ContractClass.FeedEntry._ID + "=?";
                    } else {
                        selection += ContractClass.FeedEntry._ID + "=? OR ";
                    }
                }

                if (new CRUD_OperationOfDB(HomeActivity.this).deleteFromDB(ContractClass.FeedEntry.TABLE_NAME, selection,
                        arr) > 0) {
                    mDeleteSelectedItems.selectedPositions(mSelectedItemIds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Animation for fab button to animate
     */
    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(binding.menuRed.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(binding.menuRed.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(binding.menuRed.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(binding.menuRed.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        binding.menuRed.setIconToggleAnimatorSet(set);
    }

    @Override
    public void onBackPressed() {
        if (binding.menuRed.isOpened()) {
            binding.menuRed.close(true);
        } else if (mSelectedItemIds != null && mSelectedItemIds.size() > 0) {
            mDeleteSelectedItems.selectedPositions();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (BitVaultDataManager.getSecureMessangerInstance().getUserValid()) {
        }
    }

    /**
     * Method to select entries to delete
     */
    public void selectedItemIds(ArrayList<String> mSelectedItemIds, DeleteSelectedItems mDeleteSelectedItems) {

        this.mDeleteSelectedItems = mDeleteSelectedItems;

        this.mSelectedItemIds = mSelectedItemIds;

        if (mSelectedItemIds != null && mSelectedItemIds.size() > 0) {
            binding.toolbar.deleteIcon.setVisibility(View.VISIBLE);
        } else {
            binding.toolbar.deleteIcon.setVisibility(View.INVISIBLE);
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

                deleteEntry();

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

    /**
     * This Method is used to hide FloatingPoint Buttons after click on any of them
     */
    private void hideFloatButtons() {
        binding.fabBitattachDeviceToken.toggle(true);
        binding.fabBitattachpc.toggle(true);
        binding.fabBitattach.toggle(true);
        binding.menuRed.toggle(true);
        binding.fabReceiveMessage.toggle(true);
    }

}