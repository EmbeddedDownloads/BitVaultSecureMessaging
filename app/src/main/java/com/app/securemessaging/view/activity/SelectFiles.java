package com.app.securemessaging.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.app.securemessaging.R;
import com.app.securemessaging.adaptor.SelectFilesAdapter;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.SelectFilesBean;
import com.app.securemessaging.databinding.PdfRecyclerBinding;
import com.app.securemessaging.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * View Pager class to show inbox messages
 */
public class SelectFiles extends AppCompatActivity implements IAppConstants, View.OnClickListener {

    private final int READ_EXTERNAL_STORAGE_PERMISSION = 200;
    private PdfRecyclerBinding binding;
    private ArrayList<SelectFilesBean> mSelectedFiles;
    private List<String> mGrantedPermissions;
    private boolean isPermissionGranted = false;
    private boolean isFirstAttempt = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.pdf_recycler);

        intiData();

        setLayoutManager();

        setListner();

        Utils.allocateRunTimePermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new int[]{READ_EXTERNAL_STORAGE_PERMISSION});

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utils.getAllPermisiions(this, mGrantedPermissions).contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            isPermissionGranted = true;

            binding.progress.setVisibility(View.VISIBLE);

            getListFiles(Environment.getExternalStorageDirectory());

            if (mSelectedFiles != null && mSelectedFiles.size() > 0) {
                binding.emptyResult.emptyText.setVisibility(View.GONE);
                binding.recyclerView.setAdapter(new SelectFilesAdapter(this, mSelectedFiles));
            } else {
                binding.emptyResult.emptyText.setVisibility(View.VISIBLE);
            }
            binding.progress.setVisibility(View.GONE);

        } else if (!isPermissionGranted) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && !isFirstAttempt) {
                showPermissionsSettingsPage();
            } else {
                isFirstAttempt = false;
                Utils.allocateRunTimePermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new int[]{READ_EXTERNAL_STORAGE_PERMISSION});
            }
        }
    }


    /**
     * Method used to set listner
     */
    private void setListner() {
        binding.send.setOnClickListener(this);
        binding.toolbar.walletInfoBack.setOnClickListener(this);
    }

    /**
     * Method is used to initialize variables
     */
    private void intiData() {
        mSelectedFiles = new ArrayList<>();
        mGrantedPermissions = new ArrayList<>();
        binding.toolbar.title.setText(getResources().getString(R.string.select_files));
    }

    /**
     * Method used to set the LayoutManager to the recyclerview
     */

    private void setLayoutManager() {

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);

    }

    /**
     * Getting the list of information of all inbox data.
     *
     * @return -- list of files
     */

    private List<SelectFilesBean> getListFiles(File parentDir) {
        ArrayList<SelectFilesBean> inFiles = new ArrayList();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            SelectFilesBean selectFilesBean = new SelectFilesBean();
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if (file.getName().endsWith(".pdf") || file.getName().endsWith(".mp3")) {
                    selectFilesBean.setName(file.getName());
                    selectFilesBean.setSize(String.valueOf(file.length()));
                    selectFilesBean.setSelected(false);
                    inFiles.add(selectFilesBean);
                    mSelectedFiles.add(selectFilesBean);
                }
            }
        }
        return inFiles;
    }

    /**
     * Method will update the count of the files selected
     *
     * @param filesCount -- count of the file
     */
    public void updateSendFilesCount(int filesCount) {

        binding.send.setText("Send (" + filesCount + ")");

    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {

            case R.id.send:
                Intent sendAttachment = new Intent(this, CreateMessageWithBitAttach.class);
                sendAttachment.putExtra(FROM, PHONE_TO_DESKTOP);
                startActivity(sendAttachment);
                break;

            case R.id.wallet_info_back:
                onBackPressed();
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mGrantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                return;
            }
        }
    }

    /**
     * Method used to show snackbar to navigate to settings page.
     */
    private void showPermissionsSettingsPage() {

        Snackbar.make(binding.parentLayout, getResources().getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .show();
    }
}
