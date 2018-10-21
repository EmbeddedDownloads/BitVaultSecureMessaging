package com.app.securemessaging.adaptor;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.databinding.SingleGridLayoutBinding;
import com.app.securemessaging.view.activity.CreateMessage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * this class is used to set the adaptor of show image.
 */

public class ShowImageAdapter extends RecyclerView.Adapter<ShowImageAdapter.ShowImageViewHolder> implements IAppConstants {
    private Context context;

    private List<ShowImageBean> showImageBeanList;

    public ShowImageAdapter(List<ShowImageBean> showImageBeanList, Context context) {
        this.context = context;
        this.showImageBeanList = showImageBeanList;
    }

    @Override
    public ShowImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_grid_layout, null);
        ShowImageViewHolder viewHolder = new ShowImageViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ShowImageViewHolder holder, int position) {

        try {
            if (showImageBeanList != null && showImageBeanList.size() > 0) {
                final ShowImageBean bean = showImageBeanList.get(position);

                if (bean.getType().equalsIgnoreCase(MEDIA_TYPE_CAMERA_IMAGE) || bean.getType().equalsIgnoreCase(IMAGE)) {
                    Picasso.with(context).load(new File(bean.getImage())).into(holder.getBinding().gridImage);
                    holder.getBinding().videoIcon.setVisibility(View.GONE);
                } else if (bean.getType().equalsIgnoreCase(MEDIA_TYPE_CAMERA_VIDEO) || bean.getType().equalsIgnoreCase(VIDEO)) {
                    if (bean.getThumbnail() != null && !bean.getThumbnail().equals("")) {
                        Picasso.with(context).load(new File(bean.getThumbnail())).into(holder.getBinding().gridImage);
                        holder.getBinding().videoIcon.setVisibility(View.GONE);
                    } else {
                        holder.getBinding().videoIcon.setVisibility(View.VISIBLE);
                        holder.getBinding().gridImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(bean.getImage(), MediaStore.Images.Thumbnails.MICRO_KIND));
                    }
                } else if (bean.getType().equalsIgnoreCase(MEDIA_TYPE_GALLERY_IMAGE) || bean.getType().equalsIgnoreCase(IMAGE)) {
                    Picasso.with(context).load(new File(bean.getImage())).into(holder.getBinding().gridImage);
                    holder.getBinding().videoIcon.setVisibility(View.GONE);
                } else if (bean.getType().equalsIgnoreCase(MEDIA_TYPE_GALLERY_VIDEO) || bean.getType().equalsIgnoreCase(VIDEO)) {
                    if (String.valueOf(bean.getThumbnail()) != null && !String.valueOf(bean.getThumbnail()).equals("")) {
                        holder.getBinding().videoIcon.setVisibility(View.VISIBLE);
                        Picasso.with(context).load(new File(bean.getThumbnail())).into(holder.getBinding().gridImage);
                    } else {
                        holder.getBinding().gridImage.setImageResource(R.drawable.default_thumb);
                        holder.getBinding().videoIcon.setVisibility(View.GONE);
                    }
                } else if (bean.getType().equalsIgnoreCase(MEDIA_TYPE_AUDIO) || bean.getType().equalsIgnoreCase(AUDIO)) {
                    holder.getBinding().gridImage.setImageResource(R.drawable.audio_1);
                    holder.getBinding().videoIcon.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return showImageBeanList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setUpdatedData(List<ShowImageBean> showImageBeanList) {
        this.showImageBeanList = showImageBeanList;
        notifyDataSetChanged();
    }

    public void setUpdatedDataSingleItem(List<ShowImageBean> showImageBeanList) {
        this.showImageBeanList = showImageBeanList;
        notifyItemInserted(showImageBeanList.size() - 1);
    }

    public void setUpdatedDataSingleItemRemoved(List<ShowImageBean> showImageBeanList) {
        this.showImageBeanList = showImageBeanList;
        notifyItemInserted(showImageBeanList.size() - 1);
    }


    class ShowImageViewHolder extends RecyclerView.ViewHolder {
        private SingleGridLayoutBinding binding;

        public ShowImageViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

            binding.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (showImageBeanList != null && showImageBeanList.size() > 0) {
                            if(getAdapterPosition() >= 0 ) {
                                ((CreateMessage) context).removeItemFromList(showImageBeanList.get(getAdapterPosition()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public SingleGridLayoutBinding getBinding() {
            return binding;
        }
    }
}
