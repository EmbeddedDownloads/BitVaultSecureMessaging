package com.app.securemessaging.adaptor;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.BR;
import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.DeleteSelectedItems;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.bean.MessageBeanHelper;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.databinding.SingleMessageLayoutBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.HomeActivity;
import com.app.securemessaging.view.activity.SelectWallet;
import com.app.securemessaging.view.activity.SelectWalletType;
import com.app.securemessaging.view.activity.SendingActivity;
import com.app.securemessaging.view.activity.SentMessageActivity;

import org.spongycastle.util.encoders.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.WalletDetails;


/**
 * this class is used to set the adaptor of sent message.
 */
public class SentMessageAdapter extends RecyclerView.Adapter<SentMessageAdapter.MyViewHolder> implements IAppConstants, DeleteSelectedItems {
    private List<MessageBeanHelper> messageBeanList;
    private Context context;
    private ArrayList<String> mSelectedPosition;
    private ArrayList<ContactsModel> mContactsModel;

    public SentMessageAdapter(Context context, List<MessageBeanHelper> messageBeanList,ArrayList<ContactsModel> contactsModel) {
        this.context = context;
        this.messageBeanList = messageBeanList;
        mSelectedPosition = new ArrayList<>();
        this.mContactsModel = contactsModel;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final MessageBeanHelper messageBean = messageBeanList.get(position);
        holder.getBinding().setVariable(BR.messageBeanhelper, messageBean);
        holder.getBinding().executePendingBindings();

        if (!messageBean.isSelected()) {
            holder.getBinding().rowItem.setBackgroundColor(context.getResources().getColor(R.color.row_background));
            holder.getBinding().contactImage.setImageDrawable(context.getResources().getDrawable(R.drawable.common_image));
            holder.getBinding().receiverAddress.setText(messageBeanList.get(position).getReceiverAddress());
            if(mContactsModel != null && mContactsModel.size() >0) {
                int contactSize = mContactsModel.size();
                for (int i = 0; i < contactSize; i++) {
                    if (messageBean.getReceiverAddress().equals(mContactsModel.get(i).getmReceiverAddress())) {
                        holder.getBinding().receiverAddress.setText(mContactsModel.get(i).getmName());
                        if(mContactsModel.get(i).getmImage() != null){
                            holder.getBinding().contactImage.setImageURI(Uri.parse(mContactsModel.get(i).getmImage()));
                        }
                        break;
                    }
                }
            }
            mSelectedPosition.remove(messageBean.getId());
            ((HomeActivity) context).selectedItemIds(mSelectedPosition, this);
        } else {
            holder.getBinding().rowItem.setBackgroundColor(context.getResources().getColor(R.color.row_background_selected));
            mSelectedPosition.add(messageBeanList.get(position).getId());
            holder.getBinding().contactImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.checked));
            ((HomeActivity) context).selectedItemIds(mSelectedPosition, this);
        }


        if (messageBean.getTypeDraftSent().equals(DRAFT)) {
            holder.getBinding().time.setText(context.getResources().getString(R.string.draft));
        } else if (messageBean.getTypeDraftSent().equals(FAIL)) {
            holder.getBinding().time.setText(context.getResources().getString(R.string.fail));
        } else {
            holder.getBinding().time.setText(Utils.converDatewithSec(messageBean.getTimeInMillis()));
        }

        if(messageBean.getmType() != null && messageBean.getmType().equalsIgnoreCase(VAULT_TO_PC_MESSAGE)){
            if(messageBean.getTxdID() != null && !messageBean.getTxdID().equalsIgnoreCase("")){
                byte [] txiD = Utils.hashGenerate(messageBean.getTxdID());
                if(txiD != null && txiD.length > 0){
                    String hashTxId = Base64.toBase64String(txiD);
                    holder.getBinding().message.setText(context.getResources().getString(R.string.a2a_inbox_hint) + hashTxId.substring(hashTxId.length() - 5));

                }
            }
        }

        setAttachmentData(messageBean.getmType(), holder);

    }

    /**
     * Method to set attachment icon on the basis of type of message
     *
     * @param mMessageType
     * @param holder
     */
    private void setAttachmentData(String mMessageType, MyViewHolder holder) {

        switch (mMessageType) {
            case BITATTACH_MESSAGE:
                holder.getBinding().bitAttachment.setVisibility(View.VISIBLE);
                holder.getBinding().attachment.setVisibility(View.GONE);
                holder.getBinding().b2aAttach.setVisibility(View.GONE);
                break;
            case BITVAULT_MESSAGE:
                holder.getBinding().bitAttachment.setVisibility(View.GONE);
                holder.getBinding().attachment.setVisibility(View.GONE);
                holder.getBinding().b2aAttach.setVisibility(View.GONE);
                break;
            case BITVAULT_MESSAGE_WITH_ATTACHMENT:
                holder.getBinding().attachment.setVisibility(View.VISIBLE);
                holder.getBinding().bitAttachment.setVisibility(View.GONE);
                holder.getBinding().b2aAttach.setVisibility(View.GONE);
                break;

            case VAULT_TO_PC_MESSAGE:
                holder.getBinding().bitAttachment.setVisibility(View.GONE);
                holder.getBinding().attachment.setVisibility(View.GONE);
                holder.getBinding().b2aAttach.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return messageBeanList.size();
    }

    @Override
    public void selectedPositions(ArrayList<String> mSelectedPositions) {

        List<MessageBeanHelper> messageBeanListDel = new ArrayList<>();
        int sizeMessageBeanList = messageBeanList.size();
        for (int i = 0; i < sizeMessageBeanList; i++) {
            if (mSelectedPositions.contains(messageBeanList.get(i).getId())) {
                messageBeanListDel.add(messageBeanList.get(i));
                mSelectedPosition.remove(messageBeanList.get(i).getId());
            }
        }

        if (messageBeanListDel != null && messageBeanListDel.size() > 0) {
            messageBeanList.removeAll(messageBeanListDel);
        }

        notifyDataSetChanged();
    }

    @Override
    public void selectedPositions() {
        int sizeMessageBeanList = messageBeanList.size();
        if (messageBeanList != null && sizeMessageBeanList > 0) {
            for (int i = 0; i < sizeMessageBeanList; i++) {
                messageBeanList.get(i).setSelected(false);
            }

            notifyDataSetChanged();
        }
    }


    /**
     * View holder inner class to use same view objects
     *
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        private SingleMessageLayoutBinding binding;

        public MyViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mSelectedPosition != null && mSelectedPosition.size() > 0) {
                        updateRow(getAdapterPosition());
                    } else {
                        Intent transactionIntent = null;

                        if (messageBeanList.get(getAdapterPosition()).getTypeDraftSent().equals(DRAFT)
                                || messageBeanList.get(getAdapterPosition()).getTypeDraftSent().equals(FAIL)) {
                            // The commented code will be required later
                            if(messageBeanList.get(getAdapterPosition()).getTxdID() != null &&
                                    !messageBeanList.get(getAdapterPosition()).getTxdID().equals("")){

                                transactionIntent = new Intent(context, SendingActivity.class);
                                Bundle mBundle = new Bundle();

                                if(messageBeanList.get(getAdapterPosition()).getmType().equals(BITVAULT_MESSAGE)) {
                                    mBundle.putString(FROM, BITVAULT);
                                    mBundle.putString(MESSAGE,messageBeanList.get(getAdapterPosition()).getMessage());
                                    mBundle.putSerializable(SELECTED_LIST,setFilePaths(messageBeanList.get(getAdapterPosition())));
                                    mBundle.putInt(CHARACTER_COUNT,messageBeanList.get(getAdapterPosition()).getMessage().length());
                                }else if(messageBeanList.get(getAdapterPosition()).getmType().equals(BITVAULT_MESSAGE_WITH_ATTACHMENT)){
                                    mBundle.putString(FROM, BITVAULT);
                                    mBundle.putString(MESSAGE,messageBeanList.get(getAdapterPosition()).getMessage());
                                    mBundle.putSerializable(SELECTED_LIST,setFilePaths(messageBeanList.get(getAdapterPosition())));
                                    mBundle.putInt(CHARACTER_COUNT,messageBeanList.get(getAdapterPosition()).getMessage().length());

                                }else if(messageBeanList.get(getAdapterPosition()).getmType().equals(BITATTACH_MESSAGE)){
                                    mBundle.putString(FROM, BITVAULTATTACH);
                                    mBundle.putString(BitAttachSessionKey,messageBeanList.get(getAdapterPosition()).getMessage());
                                    mBundle.putString(BitAtachSize,"100");

                                }else if(messageBeanList.get(getAdapterPosition()).getmType().equals(VAULT_TO_PC_MESSAGE)){
                                    mBundle.putString(FROM, BITATTACH);
                                    mBundle.putSerializable(SELECTED_LIST,setFilePaths(messageBeanList.get(getAdapterPosition())));
                                }

                                transactionIntent.putExtra(WALLET_TYPE,messageBeanList.get(getAdapterPosition()).getmWalletType());
                                mBundle.putString(RECEIVER_ADDRESS,messageBeanList.get(getAdapterPosition()).getReceiverAddress());
                                mBundle.putString(TXID,messageBeanList.get(getAdapterPosition()).getTxdID());
                                mBundle.putString(ROW_ID,messageBeanList.get(getAdapterPosition()).getId());
                                mBundle.putString(MESSAGE_FEE,messageBeanList.get(getAdapterPosition()).getmMessageFee());
                                if(messageBeanList.get(getAdapterPosition()).getmWalletType() != null
                                        && messageBeanList.get(getAdapterPosition()).getmWalletType().equals(context.getResources().getString(R.string.wallet_eot))){
                                    mBundle.putString(EOT_WALLET_ADDRESS,messageBeanList.get(getAdapterPosition()).getSenderAddress());
                                }else{
                                    WalletDetails walletDetails = new WalletDetails();
                                    walletDetails.setWALLET_ID(messageBeanList.get(getAdapterPosition()).getSenderAddress());
                                    walletDetails.setWALLET_NAME(messageBeanList.get(getAdapterPosition()).getmWalletName());
                                    mBundle.putSerializable(WALLET_DETAILS,walletDetails);
                                }
                                transactionIntent.putExtras(mBundle);
                            }
                            else{
                             if(messageBeanList.get(getAdapterPosition()).getmType().equals(VAULT_TO_PC_MESSAGE)) {
                                 transactionIntent = new Intent(context, SelectWallet.class);
                                 transactionIntent.putExtra(WALLET_TYPE,context.getString(R.string.wallet_bitcoin));

                             }else{
                                 transactionIntent = new Intent(context, SelectWalletType.class);
                             }
                                Bundle mBundle = new Bundle();
                                mBundle.putSerializable(MESSAGE_OBJECT, messageBeanList.get(getAdapterPosition()));
                                mBundle.putString(MESSAGE_TYPE, messageBeanList.get(getAdapterPosition()).getmType());
                                transactionIntent.putExtras(mBundle);
                           }
                        } else {
                            transactionIntent = new Intent(context, SentMessageActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable(MESSAGE_OBJECT, messageBeanList.get(getAdapterPosition()));
                            transactionIntent.putExtras(mBundle);
                        }

                        if (transactionIntent != null) {
                            context.startActivity(transactionIntent);
                        }
                    }
                }
            });

            binding.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateRow(getAdapterPosition());
                    return true;
                }
            });
        }


        public SingleMessageLayoutBinding getBinding() {
            return binding;
        }


        /**
         * Method used to update selected row position
         *
         * @param position
         */
        private void updateRow(int position) {

            if (messageBeanList.get(position).isSelected()) {
                messageBeanList.get(position).setSelected(false);
            } else {
                messageBeanList.get(position).setSelected(true);
            }
            notifyItemChanged(position);
        }
    }

    /**
     * Method used to set file paths to intent
     *
     * @param messageBeanList
     */
    private ArrayList<ShowImageBean> setFilePaths(MessageBeanHelper messageBeanList){
            ArrayList<ShowImageBean> mPath = new ArrayList<>();
            if(messageBeanList != null) {
                String mSelectedImages[] = messageBeanList.getAttachment().split(IAppConstants.FILE_SEPERATOR_SPLIT);
                for (int i = 0; i < mSelectedImages.length; i++) {
                    ShowImageBean obj = new ShowImageBean();
                    obj.setImage(mSelectedImages[i]);
                    obj.setType(Utils.getMediaType(Utils.getFileExtension(new File(mSelectedImages[i]))));
                    obj.setFileSize(new File(mSelectedImages[i]).length());
                    mPath.add(obj);
                }
            }
            return  mPath;
    }

}
