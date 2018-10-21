package com.app.securemessaging.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Message Bean Helper class to Set message,name and time.
 */
public class MessageBeanOuterHelper implements Serializable {

    /**
     * STATUS = "status";  // Pending,Success,fail
     * TYPE = "type";  // Inbox,Sent,Draft
     */

    private String title = "";
    private ArrayList<MessageBeanHelper> mMessageBeanHelper;

    /**
     * parameterize constructors for message helper bean
     */

    public MessageBeanOuterHelper(String title, ArrayList<MessageBeanHelper> mMessageBeanHelper) {
        this.title = title;
        this.mMessageBeanHelper = mMessageBeanHelper;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<MessageBeanHelper> getmMessageBeanHelper() {
        return mMessageBeanHelper;
    }

    /**
     * Setting the message bean helper
     *
     * @param mMessageBeanHelper
     */
    public void setmMessageBeanHelper(ArrayList<MessageBeanHelper> mMessageBeanHelper) {
        this.mMessageBeanHelper = mMessageBeanHelper;
    }
}
