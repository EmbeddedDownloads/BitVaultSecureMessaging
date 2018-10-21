package com.app.securemessaging.adaptor;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.BR;
import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.bean.MessageBeanOuterHelper;
import com.app.securemessaging.databinding.SingleMessageBinding;
import com.app.securemessaging.view.fragment.InboxFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to set the adaptor of inbox message.
 */
public class InboxMessageOuterAdapter extends RecyclerView.Adapter<InboxMessageOuterAdapter.MyViewHolder> implements IAppConstants {
    private List<MessageBeanOuterHelper> messageBeanList;
    private Context context;
    private boolean isNewExists = false;
    private InboxMessageAdapter mInboxMesssageAdapter;
    private InboxFragment mInboxFragment ;
    private ArrayList<ContactsModel> mContactsModel;

    public InboxMessageOuterAdapter(Context context, List<MessageBeanOuterHelper> messageBeanList,
                                    boolean isNewExists,InboxFragment mInboxFragment,ArrayList<ContactsModel> contactsModel) {
        this.context = context;
        this.messageBeanList = messageBeanList;
        this.isNewExists = isNewExists;
        this.mInboxFragment = mInboxFragment ;
        this.mContactsModel = contactsModel;
    }

    /**
     * Method to inflate updated list
     *
     * @param messageBeanList
     */
    public void setUpdatedData(List<MessageBeanOuterHelper> messageBeanList) {
        this.messageBeanList = messageBeanList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final MessageBeanOuterHelper messageBean = messageBeanList.get(position);

        holder.getBinding().setVariable(BR.messageBeanhelper, messageBean);
        holder.getBinding().executePendingBindings();

        holder.getBinding().text.setText(messageBean.getTitle());

        mInboxMesssageAdapter = new InboxMessageAdapter(context, messageBean.getmMessageBeanHelper(), messageBean.getTitle(),mInboxFragment,mContactsModel);
        holder.getBinding().recycler.setAdapter(mInboxMesssageAdapter);
    }


    @Override
    public int getItemCount() {
        return messageBeanList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private SingleMessageBinding binding;

        public MyViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }


        /**
         * Method to get binding
         *
         * @return -- binding
         */
        public SingleMessageBinding getBinding() {
            return binding;
        }

    }

}
