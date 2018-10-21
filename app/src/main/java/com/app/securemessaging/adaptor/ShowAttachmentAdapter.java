package com.app.securemessaging.adaptor;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ShowImageBean;
import com.app.securemessaging.databinding.SingleGridLayoutDetailBinding;
import com.app.securemessaging.util.Utils;

import java.io.File;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/**
 * this class is used to set the adaptor of show image.
 */

public class ShowAttachmentAdapter extends RecyclerView.Adapter<ShowAttachmentAdapter.ShowImageViewHolder> implements IAppConstants {
    private Context context;

    private List<ShowImageBean> showImageBeanList;

    public ShowAttachmentAdapter(List<ShowImageBean> showImageBeanList, Context context) {
        this.context = context;
        this.showImageBeanList = showImageBeanList;
    }

    @Override
    public ShowImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_grid_layout_detail, null);
        ShowImageViewHolder viewHolder = new ShowImageViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ShowImageViewHolder holder, int position) {

        if (showImageBeanList != null && showImageBeanList.size() > 0) {

            holder.getBinding().closeButton.setVisibility(View.GONE);

            final ShowImageBean bean = showImageBeanList.get(position);
            if (bean.getImage() != null && !bean.getImage().equals("")) {
                if (new File(bean.getImage()).exists()) {

                    String extension = bean.getType();

                    if (Utils.getMediaType(extension).equalsIgnoreCase(VIDEO)) {
                        holder.getBinding().videoIcon.setVisibility(View.VISIBLE);
                        holder.getBinding().gridImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(bean.getImage(), MediaStore.Images.Thumbnails.MINI_KIND));
                    } else if (Utils.getMediaType(extension).equalsIgnoreCase(IMAGE)) {
                        holder.getBinding().videoIcon.setVisibility(View.GONE);
                        holder.getBinding().gridImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(bean.getImage())));
                    } else if (Utils.getMediaType(extension).equalsIgnoreCase(AUDIO)) {
                        holder.getBinding().gridImage.setImageResource(R.drawable.audio_1);
                        holder.getBinding().videoIcon.setVisibility(View.GONE);
                    }
                } else {
                    Utils.showSnakbar(holder.getBinding().rlMain, context.getResources().getString(R.string.media_deleted), Snackbar.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return showImageBeanList.size();
    }

    public void setUpdatedData(List<ShowImageBean> showImageBeanList) {
        this.showImageBeanList = showImageBeanList;
    }

    class ShowImageViewHolder extends RecyclerView.ViewHolder {
        private SingleGridLayoutDetailBinding binding;

        public ShowImageViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

            binding.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performClick(Utils.getMediaType(showImageBeanList.get(getAdapterPosition()).getType()), showImageBeanList.get(getAdapterPosition()).getImage());
                }
            });
        }

        public SingleGridLayoutDetailBinding getBinding() {
            return binding;
        }


        private void performClick(String clickAction, String mPath) {

            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(mPath);

            switch (clickAction) {

                case VIDEO:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        intent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + TAG_PROVIDER, file), TAG_VIDEO_URI);
                        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), TAG_VIDEO_URI);
                    }
                    context.startActivity(intent);
                    break;

                case AUDIO:

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        intent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + TAG_PROVIDER, file), TAG_AUDIO_URI);
                        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), TAG_AUDIO_URI);
                    }
                    context.startActivity(intent);
                    break;

                case IMAGE:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        intent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + TAG_PROVIDER, file), TAG_IMAGE_URI);
                        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), TAG_IMAGE_URI);
                    }

                    context.startActivity(intent);
                    break;
            }
        }

    }


}
