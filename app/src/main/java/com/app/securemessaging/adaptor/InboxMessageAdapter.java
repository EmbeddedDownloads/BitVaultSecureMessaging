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
import com.app.securemessaging.databinding.SingleMessageLayoutInboxBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.BitAttachInboxActivity;
import com.app.securemessaging.view.activity.HomeActivity;
import com.app.securemessaging.view.activity.InboxActivity;
import com.app.securemessaging.view.fragment.InboxFragment;

import org.spongycastle.util.encoders.Base64;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to set the adaptor of inbox message.
 */
public class InboxMessageAdapter extends RecyclerView.Adapter<InboxMessageAdapter.MyViewHolder> implements IAppConstants, DeleteSelectedItems {
    private List<MessageBeanHelper> messageBeanList;
    private Context context;
    private String mTitle = "";
    private ArrayList<String> mSelectedPosition;
    private InboxFragment mInboxFragment ;
    private ArrayList<ContactsModel> mContactsModel;

    public InboxMessageAdapter(Context context, List<MessageBeanHelper> messageBeanList, String mTitle,
                               InboxFragment mInboxFragment,ArrayList<ContactsModel> contactsModel) {
        this.context = context;
        this.messageBeanList = messageBeanList;
        this.mTitle = mTitle;
        mSelectedPosition = new ArrayList<>();
        this.mInboxFragment = mInboxFragment ;
        this.mContactsModel = contactsModel;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout_inbox, null);
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
            if(mContactsModel != null && mContactsModel.size() > 0) {
              if (messageBean.getSenderAddress() != null) {
                  int contactSize = mContactsModel.size();
                  for (int i = 0; i < contactSize; i++) {
                      if (messageBean.getSenderAddress().equals(mContactsModel.get(i).getmReceiverAddress())) {
                          holder.getBinding().senderAddress.setText(mContactsModel.get(i).getmName());
                          if(mContactsModel.get(i).getmImage() != null){
                              holder.getBinding().contactImage.setImageURI(Uri.parse(mContactsModel.get(i).getmImage()));
                          }
                          break;
                      }
                  }
              }
          }
            mSelectedPosition.remove(messageBean.getId());
            ((HomeActivity) context).selectedItemIds(mSelectedPosition, this);
        } else {
            holder.getBinding().contactImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.checked));
            holder.getBinding().rowItem.setBackgroundColor(context.getResources().getColor(R.color.row_background_selected));
            mSelectedPosition.add(messageBeanList.get(position).getId());
            ((HomeActivity) context).selectedItemIds(mSelectedPosition, this);
        }
        if (messageBean.getTimeInMillis() != null) {
            holder.getBinding().time.setText(Utils.converDatewithSec(messageBean.getTimeInMillis()));
        }
        if(messageBean.getmType().equalsIgnoreCase(BITATTACH_MESSAGE)){
            String hashTxId = Base64.toBase64String(Utils.hashGenerate(messageBean.getTxdID()));
            holder.getBinding().message.setText(context.getResources().getString(R.string.a2a_inbox_hint) + hashTxId.substring(hashTxId.length() - 5));
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
                break;
            case BITVAULT_MESSAGE:
                holder.getBinding().bitAttachment.setVisibility(View.GONE);
                holder.getBinding().attachment.setVisibility(View.GONE);
                break;
            case BITVAULT_MESSAGE_WITH_ATTACHMENT:
                holder.getBinding().bitAttachment.setVisibility(View.GONE);
                holder.getBinding().attachment.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return messageBeanList.size();
    }

    /**
     * Method used to get updated list
     *
     * @param messageBeanHelpers
     */
    public void setUpdateList(ArrayList<MessageBeanHelper> messageBeanHelpers) {
        this.messageBeanList = messageBeanHelpers;
        notifyDataSetChanged();
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
        mInboxFragment.updateUI();
    }

    @Override
    public void selectedPositions() {
        int messageBeanSize = messageBeanList.size();
        if (messageBeanList != null && messageBeanSize > 0) {
            for (int i = 0; i < messageBeanSize; i++) {
                messageBeanList.get(i).setSelected(false);
            }

            notifyDataSetChanged();
        }
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private SingleMessageLayoutInboxBinding binding;

        public MyViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mSelectedPosition != null && mSelectedPosition.size() > 0) {
                        updateRow(getAdapterPosition());
                    } else {

                        if (messageBeanList.get(getAdapterPosition()).getmType().equals(BITVAULT_MESSAGE)
                                || messageBeanList.get(getAdapterPosition()).getmType().equals(BITVAULT_MESSAGE_WITH_ATTACHMENT)) {
                            Intent transactionIntent = new Intent(context, InboxActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable(MESSAGE_OBJECT, messageBeanList.get(getAdapterPosition()));
                            transactionIntent.putExtras(mBundle);
                            context.startActivity(transactionIntent);
                        } else  if(messageBeanList.get(getAdapterPosition()).getmType().equals(BITATTACH_MESSAGE)){
                            Intent transactionIntent = new Intent(context, BitAttachInboxActivity.class);

                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable(MESSAGE_OBJECT, messageBeanList.get(getAdapterPosition()));
                            transactionIntent.putExtras(mBundle);

                            context.startActivity(transactionIntent);
                        }
                        else{

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


        public SingleMessageLayoutInboxBinding getBinding() {
            return binding;
        }
    }

}
