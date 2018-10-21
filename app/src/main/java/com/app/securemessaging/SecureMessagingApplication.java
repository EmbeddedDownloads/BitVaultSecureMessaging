package com.app.securemessaging;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import controller.SDKControl;


/**
 * Class is used for Application level initializatoins
 */

// Used to send crash report to the particular mail id
@ReportsCrashes(
                mailTo = "vinod.singh@vvdntech.in",
                formKey = "", // This is required for backward compatibility but not used
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.toast_crash
        )
public class SecureMessagingApplication extends SDKControl {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
