package com.app.securemessaging.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.securemessaging.R;
import com.app.securemessaging.databinding.ActivityNfcauthenticationBinding;


public class NFCAuthenticationActivity extends AppCompatActivity {

    ActivityNfcauthenticationBinding activityNfcauthenticationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityNfcauthenticationBinding = DataBindingUtil.setContentView(this, R.layout.activity_nfcauthentication);


        activityNfcauthenticationBinding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NFCAuthenticationActivity.this, SplashActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
