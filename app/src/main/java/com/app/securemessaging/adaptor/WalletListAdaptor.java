package com.app.securemessaging.adaptor;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.BR;
import com.app.securemessaging.R;
import com.app.securemessaging.databinding.ListBinding;
import com.app.securemessaging.databinding.SelectWalletBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.SelectWallet;

import java.util.List;

import model.WalletDetails;


/**
 * this class is used to set the adaptor of wallet
 */

public class WalletListAdaptor extends RecyclerView.Adapter<WalletListAdaptor.WalletHolder> {

    private final String TAG = getClass().getSimpleName();
    private final List<WalletDetails> walletBeanClassList;
    private final Context mContext;
    private SelectWalletBinding mSelectWalletBinding;

    /**
     * @param walletBeanClassList list of all wallet
     * @param mContext            context from where this adaptor is called
     */
    public WalletListAdaptor(List<WalletDetails> walletBeanClassList,SelectWalletBinding selectWalletBinding,
                             Context mContext) {
        this.walletBeanClassList = walletBeanClassList;
        this.mContext = mContext;
        this.mSelectWalletBinding = selectWalletBinding;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list, parent, false);
        WalletHolder holder = new WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(WalletHolder holder, int position) {
        final WalletDetails mWalletDetails = walletBeanClassList.get(position);
        holder.getBinding().setVariable(BR.walletBeanClass, mWalletDetails);
        holder.getBinding().executePendingBindings();

        holder.getBinding().walletAvailableCoinTextView.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(mWalletDetails.getWALLET_LAST_UPDATE_BALANCE())));

        if (mWalletDetails.getWALLET_ICON() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mWalletDetails.getWALLET_ICON(), 0, mWalletDetails.getWALLET_ICON().length);
            BitmapDrawable ob = new BitmapDrawable(mContext.getResources(), bitmap);
            holder.getBinding().logoImageView.setBackground(ob);
        } else {
            holder.getBinding().logoImageView.setBackgroundResource(R.drawable.wallet_logo);
        }

    }

    @Override
    public int getItemCount() {
        return walletBeanClassList.size();
    }

    class WalletHolder extends RecyclerView.ViewHolder {
        private ListBinding walletListItemBinding = null;

        public WalletHolder(View v) {
            super(v);

            walletListItemBinding = DataBindingUtil.bind(v);

            walletListItemBinding.parentRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Double.parseDouble(walletBeanClassList.get(getAdapterPosition()).getWALLET_LAST_UPDATE_BALANCE()) <= 0){
                        Utils.showSnakbar(mSelectWalletBinding.parentLayout, mContext.getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
                    }else {
                        ((SelectWallet) mContext).navigateUser(walletBeanClassList.get(getAdapterPosition()));
                    }
                }
            });

        }

        public ListBinding getBinding() {
            return walletListItemBinding;
        }

    }
}

