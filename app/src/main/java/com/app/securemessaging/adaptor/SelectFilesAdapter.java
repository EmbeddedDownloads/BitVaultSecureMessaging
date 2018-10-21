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
import com.app.securemessaging.bean.SelectFilesBean;
import com.app.securemessaging.databinding.PdfBinding;
import com.app.securemessaging.util.Utils;
import com.app.securemessaging.view.activity.SelectFiles;

import java.util.List;

/**
 * this class is used to set the adaptor of inbox message.
 */
public class SelectFilesAdapter extends RecyclerView.Adapter<SelectFilesAdapter.MyViewHolder> implements IAppConstants {
    private List<SelectFilesBean> selectFilesAdapters;
    private Context context;

    public SelectFilesAdapter(Context context, List<SelectFilesBean> selectFilesAdapters) {
        this.context = context;
        this.selectFilesAdapters = selectFilesAdapters;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final SelectFilesBean selectFileBean = selectFilesAdapters.get(position);

        holder.getBinding().setVariable(BR.selectFilesBean, selectFileBean);
        holder.getBinding().executePendingBindings();

        if (selectFileBean.getSize() != null) {
            holder.getBinding().mediaFileSize.setText(Utils.convertKBIntoHigherUnit(Long.parseLong(selectFileBean.getSize())));
        }
        
        if (selectFileBean.isSelected())
            holder.getBinding().selectFile.setVisibility(View.VISIBLE);
        else
            holder.getBinding().selectFile.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return selectFilesAdapters.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private PdfBinding listBinding = null;

        public MyViewHolder(View v) {

            super(v);
            listBinding = DataBindingUtil.bind(v);

            listBinding.parentRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    selectFilesAdapters.get(getAdapterPosition()).setSelected(!selectFilesAdapters.get(getAdapterPosition()).isSelected());
                    notifyDataSetChanged();

                    int count = 0;

                    for (int i = 0; i < selectFilesAdapters.size(); i++) {
                        if (selectFilesAdapters.get(i).isSelected())
                            count++;
                    }

                    ((SelectFiles) context).updateSendFilesCount(count);
                }
            });
        }


        public PdfBinding getBinding() {
            return listBinding;
        }
    }
}

