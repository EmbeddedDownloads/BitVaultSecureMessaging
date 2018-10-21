package com.app.securemessaging.adaptor;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.R;
import com.app.securemessaging.databinding.ListEotWalletBinding;
import com.app.securemessaging.databinding.SelectWalletBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.SelectWallet;

/**
 * this class is used to set the adaptor of eot wallet
 */

public class WalletEotAdapter extends RecyclerView.Adapter<WalletEotAdapter.WalletHolder> {

    private final String TAG = getClass().getSimpleName();
    private final Context mContext;
    private double mEotCoinsTotal = 0.0;
    private SelectWalletBinding mSelectWalletBinding;

    /**
     * @param eotCoinsTotal
     * @param mContext   context from where this adaptor is called
     */
    public WalletEotAdapter(double eotCoinsTotal,SelectWalletBinding selectWalletBindin,Context mContext) {
        this.mContext = mContext;
        this.mEotCoinsTotal = eotCoinsTotal;
        this.mSelectWalletBinding = selectWalletBindin;
    }

    @Override
    public WalletEotAdapter.WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_eot_wallet, parent, false);
        WalletEotAdapter.WalletHolder holder = new WalletEotAdapter.WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(WalletEotAdapter.WalletHolder holder, int position) {
        holder.getBinding().executePendingBindings();

        holder.getBinding().walletAvailableCoinTextView.setText(mContext.getResources().getString(R.string.wallet_eot) + " " +
                Utils.convertDecimalFormatPattern(mEotCoinsTotal));

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class WalletHolder extends RecyclerView.ViewHolder {
        private ListEotWalletBinding walletListItemBinding = null;

        public WalletHolder(View v) {
            super(v);

            walletListItemBinding = DataBindingUtil.bind(v);

            walletListItemBinding.parentRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mEotCoinsTotal <= 0.0){
                        Utils.showSnakbar(mSelectWalletBinding.parentLayout, mContext.getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);

                    }else {
                        ((SelectWallet) mContext).navigateUser(getAdapterPosition());
                    }
                }
            });

        }

        public ListEotWalletBinding getBinding() {
            return walletListItemBinding;
        }

    }
}

