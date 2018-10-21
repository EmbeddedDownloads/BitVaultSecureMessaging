package com.app.securemessaging.adaptor;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.R;
import com.app.securemessaging.databinding.ListWalletTypeBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.SelectWalletType;

/**
 * this class is used to set the adaptor of wallet types
 */
public class WalletTypeListAdapter extends RecyclerView.Adapter<WalletTypeListAdapter.WalletHolder> {

    private final String TAG = getClass().getSimpleName();
    private double mBitcoinsTotal = 0;
    private double mEotCoinsTotal = 0;
    private final Context mContext;

    /**
     * @param bitcoinsTotal,eotCoinsTotal
     * @param mContext
     */
    public WalletTypeListAdapter(double bitcoinsTotal,double eotCoinsTotal, Context mContext) {
        this.mContext = mContext;
        this.mBitcoinsTotal = bitcoinsTotal;
        this.mEotCoinsTotal = eotCoinsTotal;
    }

    @Override
    public WalletTypeListAdapter.WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_wallet_type, parent, false);
        WalletTypeListAdapter.WalletHolder holder = new WalletTypeListAdapter.WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(WalletTypeListAdapter.WalletHolder holder, int position) {
        holder.getBinding().executePendingBindings();

        if(position == 0){
            holder.getBinding().typelogoImageView.setBackground(mContext.getDrawable(R.drawable.wallet_logo));
            holder.getBinding().walletTypeNameTextView.setText(mContext.getString(R.string.wallet_bitcoin));
            holder.getBinding().logoTextView.setText("n");
            holder.getBinding().walletAvailableCoinTextView.setText(" " + Utils.convertDecimalFormatPattern(mBitcoinsTotal));

        }else{
            holder.getBinding().typelogoImageView.setBackground(mContext.getDrawable(R.drawable.eottype));
            holder.getBinding().walletTypeNameTextView.setText(mContext.getString(R.string.wallet_eot));
            holder.getBinding().logoTextView.setText("");
            holder.getBinding().walletAvailableCoinTextView.setText(mContext.getResources().getString(R.string.wallet_eot) + " " +
                    Utils.convertDecimalFormatPattern(mEotCoinsTotal));

        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    class WalletHolder extends RecyclerView.ViewHolder {
        private ListWalletTypeBinding walletTypeListItemBinding = null;

        public WalletHolder(View v) {
            super(v);

            walletTypeListItemBinding = DataBindingUtil.bind(v);

            walletTypeListItemBinding.parentRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ((SelectWalletType) mContext).navigateUser(getAdapterPosition());
                }
            });

        }

        public ListWalletTypeBinding getBinding() {
            return walletTypeListItemBinding;
        }

    }
}