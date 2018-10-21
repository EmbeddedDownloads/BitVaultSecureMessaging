package com.app.securemessaging.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.app.securemessaging.R;
import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.view.activity.FingerprintAuthenticationActivity;
import com.app.securemessaging.view.activity.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import bitmanagers.BitVaultWalletManager;
import utils.SDKUtils;

/**
 * Created by Vinod Singh on 28-07-2017.
 */
public class NotificationReceiver extends BroadcastReceiver implements  IAppConstants{
    private String TAG = NotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NOTIFICATION_FILTER)) {
            Bundle mBundle = intent.getBundleExtra(BUNDLE_DATA);
            String mMessage = mBundle.getString(MESSAGE);
            try {
                JSONObject mainObject = new JSONObject(mMessage);
                if(mainObject.has(IAppConstants.DATA_TAG) && mainObject.getString(IAppConstants.DATA_TAG) != null ) {
                    if(mainObject.getString(IAppConstants.DATA_TAG).equalsIgnoreCase(IAppConstants.D2D_NOTIFICATION) ||
                            mainObject.getString(IAppConstants.DATA_TAG).equalsIgnoreCase(IAppConstants.SECURE_MESSAGE)) {
                        if (mainObject.has(IAppConstants.TAG_NOTIFICATION_SENDER) && mainObject.has(IAppConstants.TAG_NOTIFICATION_DATA)) {
                            sendNotification(context, mainObject.getString(IAppConstants.TAG_NOTIFICATION_SENDER), mainObject);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private void sendNotification(Context context, String mSenderAddress, JSONObject mainObject) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        Intent intentBroadcast = new Intent("com.app.securemessaging.UpdateUI");
        Intent intent = null;
        if(BitVaultWalletManager.getInstance().getUserValid()){
            intent = new Intent(context, HomeActivity.class);
        }
        else{
            intent = new Intent(context, FingerprintAuthenticationActivity.class);
        }
        intent.putExtra(FROM_NOTIFICATION, true);
        try {
            if(mainObject.has(IAppConstants.DATA_TAG)){
                if(mainObject.getString(IAppConstants.DATA_TAG).equalsIgnoreCase(IAppConstants.D2D_NOTIFICATION)){
                    intent.putExtra(IAppConstants.DATA_TAG, IAppConstants.D2D_NOTIFICATION);
                    intentBroadcast.putExtra(IAppConstants.DATA_TAG, IAppConstants.D2D_NOTIFICATION);
                }
                else {
                    intent.putExtra(IAppConstants.DATA_TAG, mainObject.getString(IAppConstants.DATA_TAG));
                    intentBroadcast.putExtra(IAppConstants.DATA_TAG, mainObject.getString(IAppConstants.DATA_TAG));
                }

            }
            if(mainObject.has(IAppConstants.TAG_NOTIFICATION_RECEIVER)){
                intent.putExtra(IAppConstants.TAG_NOTIFICATION_RECEIVER, mainObject.getString(IAppConstants.TAG_NOTIFICATION_RECEIVER));
                intentBroadcast.putExtra(IAppConstants.TAG_NOTIFICATION_RECEIVER, mainObject.getString(IAppConstants.TAG_NOTIFICATION_RECEIVER));
            }
            if(mainObject.has(IAppConstants.TAG_NOTIFICATION_DATA)){
                intent.putExtra(IAppConstants.TAG_NOTIFICATION_DATA, mainObject.getString(IAppConstants.TAG_NOTIFICATION_DATA));
                intentBroadcast.putExtra(IAppConstants.TAG_NOTIFICATION_DATA, mainObject.getString(IAppConstants.TAG_NOTIFICATION_DATA));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pendingIntent = PendingIntent.getActivity(context, m, intent, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(context.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(IAppConstants.TAG_NOTIFICATION_TEXT  + mSenderAddress))
                .setContentText(IAppConstants.TAG_NOTIFICATION_TEXT + mSenderAddress).setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setColor(Color.parseColor("#ED6708"));

        }
        notificationManager.notify(m, mBuilder.build());

        // send broadcast to start receiving if inbox screen is in FOREGROUND
        intentBroadcast.putExtra(IAppConstants.FROM, FROM_NOTIFICATION);
        intentBroadcast.putExtra(NOTIFICATION_ID,m);
        context.sendBroadcast(intentBroadcast);
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.app_icon_lolipop : R.mipmap.app_icon;
    }
}