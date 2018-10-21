package com.app.securemessaging.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.securemessaging.R;
import com.app.securemessaging.databinding.ActivityIrisauthenticationBinding;


public class IRISAuthenticationActivity extends AppCompatActivity {

    ActivityIrisauthenticationBinding activityIrisauthenticationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityIrisauthenticationBinding = DataBindingUtil.setContentView(this, R.layout.activity_irisauthentication);
        activityIrisauthenticationBinding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(IRISAuthenticationActivity.this, NFCAuthenticationActivity.class);
                startActivity(i);
                finish();
            }
        });


    }
}
