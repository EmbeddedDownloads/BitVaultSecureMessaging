package com.app.securemessaging.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;

import com.app.securemessaging.appinterface.IAppConstants;
import com.app.securemessaging.bean.ContactsModel;
import com.app.securemessaging.bean.MobileModel;
import com.app.securemessaging.bean.PublicKeyModel;

import org.apache.commons.codec.binary.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by linchpin on 8/5/17.
 */

public class Utils {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    final public static String SYMALGORITHM 	= "AES/CBC/PKCS5Padding";

    // IV - 16 bytes static
    final private static byte[] ivBytes = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
            (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03,
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
            (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
    };



    public static AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);

    /**
     * Method is used to allocate run time permission
     *
     * @param mContext         -- context
     * @param permission       -- permission reuired
     * @param callbackConstant -- int constant for permission
     */
    public static void allocateRunTimePermissions(Activity mContext, String[] permission, int[] callbackConstant) {

        for (int i = 0; i < permission.length; i++) {
            if (ContextCompat.checkSelfPermission(mContext,
                    permission[i])
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(mContext,
                        new String[]{permission[i]},
                        callbackConstant[i]);
            }
        }
    }


    /**
     * Method used to getAllPermission which are already given
     *
     * @param mContext          -- context
     * @param grantedPermission -- list of permissions
     * @return -- list of granted permissions
     */
    public static List<String> getAllPermisiions(Context mContext, List<String> grantedPermission) {
        try {
            PackageInfo mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < mPackageInfo.requestedPermissions.length; i++) {
                if ((mPackageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    grantedPermission.add(mPackageInfo.requestedPermissions[i]);
                }
            }
        } catch (Exception e) {
        }

        return grantedPermission;
    }


    /**
     * Method used to convert file in KB into KB,MB,GB,TB
     *
     * @param sizeInBytes - size in KB
     * @return - required format in KB,MB,GB,TB
     */
    public static String convertKBIntoHigherUnit(long sizeInBytes) {

        String hrSize = "";

        double kb = sizeInBytes / 1024;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (kb == 0) {
            hrSize = sizeInBytes + " bytes";
        }

        if (kb >= 1) {

            hrSize = dec.format(kb).concat(" KB");

        }
        if (mb >= 1) {

            hrSize = dec.format(mb).concat(" MB");
        }
        if (gb >= 1) {

            hrSize = dec.format(gb).concat(" GB");
        }
        if (tb >= 1) {

            hrSize = dec.format(tb).concat(" TB");
        }

        return hrSize;

    }

    /**
     * Method used to convert file in KB into KB,MB,GB,TB
     *
     * @param sizeInBytes - size in KB
     * @return - required format in KB,MB,GB,TB
     */
    public static String convertKBIntoHigherUnitDouble(double sizeInBytes) {

        String hrSize = "";

        double kb = sizeInBytes / 1024;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (kb == 0) {
            hrSize = sizeInBytes + " bytes";
        }

        if (kb > 0) {

            hrSize = dec.format(kb).concat(" KB");

        }
        if (mb >= 1) {

            hrSize = dec.format(mb).concat(" MB");
        }
        if (gb >= 1) {

            hrSize = dec.format(gb).concat(" GB");
        }
        if (tb >= 1) {

            hrSize = dec.format(tb).concat(" TB");
        }

        return hrSize;

    }

    /**
     * Method used to convert file in KB into KB,MB,GB,TB
     *
     * @param sizeInBytes - size in KB
     * @return - required format in KB,MB,GB,TB
     */
    public static String convertKBInto(long sizeInBytes) {

        String hrSize = "";

        double kb = sizeInBytes / 1024;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (kb == 0) {
            hrSize = sizeInBytes + "";
        }

        if (kb >= 1) {

            hrSize = dec.format(kb);

        }
        if (mb >= 1) {

            hrSize = dec.format(mb);
        }
        if (gb >= 1) {

            hrSize = dec.format(gb);
        }
        if (tb >= 1) {

            hrSize = dec.format(tb);
        }

        return hrSize;

    }

    /**
     * Method to animate a view from top to bottom
     *
     * @param view - view which needs to be animate
     */
    public static void slideToBottom(View view, int height) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    /**
     * Method to animate view slide out from bottom to top
     *
     * @param view - view which needs to be animate
     */
    public static void slideToTop(final View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }


    /**
     * Method to snackbar in the application
     *
     * @param mView    -- parent layout
     * @param message  -- message to be displayed
     * @param duration -- durattion of the snackbar
     */
    public static void showSnakbar(View mView, String message, int duration) {
        try {
            if (mView != null) {
                Snackbar.make(mView, message, duration).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to check whether device is connected with internet or not
     *
     * @param mContext -- context of the application
     * @return -- either network is connected or not
     */
    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Method is used to prepare data from the arraylist of the file
     *
     * @param mFileList -- list of the file selected by the user
     * @return -- base64 + extension of data seperated by | & *
     */
    public static String prepareDataForFiles(ArrayList<File> mFileList) {

        String fileData = "";

        for (int i = 0; i < mFileList.size(); i++) {

            fileData += genBase64OfMediaFile(mFileList.get(i)) + "|"

                    + getFileExtension(mFileList.get(i)) + "*";

        }

        return fileData;
    }

    /**
     * Method used to get Base64 of Media file
     *
     * @param file -- Path of the image
     * @return -- Base64 value of the image
     */
    public static String genBase64OfMediaFile(File file) {
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedfile;
    }


    /**
     * Method to get the extension of the file
     *
     * @param mFile -- file selected by the user
     * @return -- extension of the file
     */
    public static String getFileExtension(File mFile) {
        String extension = "";

        try {
            if (mFile != null && !mFile.getAbsolutePath().equals("")) {
                File mSelectedFile = mFile;
                extension = mFile.getAbsoluteFile().toString().substring(mFile.getAbsoluteFile().toString().lastIndexOf("."), mFile.getAbsoluteFile().toString().length());
                return extension;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extension;
    }

    /**
     * Method used to get Base64 of any string value
     *
     * @param value -- text value of the message
     * @return -- base64 of the text
     */
    public static String getBase64(String value) {

        String base64Data = "";
        byte[] bytesEncoded = Base64.encodeBase64(value.getBytes());
        return (new String(bytesEncoded));
    }


    /**
     * Method used to take time difference
     *
     * @param millis
     * @return
     */
    public static String converDatewithSec(String millis) {

        try {

            long current = System.currentTimeMillis();
            long actual = Long.parseLong(millis);

            long difference = (current - actual) / 1000;

            String returnDate = "";

            if (difference < 60) {
                returnDate = difference + " seconds ago";
            } else if (difference >= 60 && difference < 60 * 60) {
                returnDate = difference / 60 + " minutes ago";
            } else if (difference >= 60 * 60 && difference < 24 * 60 * 60) {
                returnDate = difference / (60 * 60) + " hours ago";
            } else {
                Date date = new Date(Long.parseLong(millis));
                SimpleDateFormat requiredFormatwithtime = new SimpleDateFormat("EEEE, dd MMM - hh:mm a");
                returnDate = requiredFormatwithtime.format(date);
            }

            return returnDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Method used to get decoded base64 value
     *
     * @param mBase64Data
     * @return
     */
    public static String getDecodedBase64(String mBase64Data) {

        String base64 = "";

        try {
            byte[] base64Data = org.spongycastle.util.encoders.Base64.decode(mBase64Data);
            return new String(base64Data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;

    }

    /**
     * Method used to get decoded base64 value into media
     *
     * @param mBase64Data
     * @return
     */
    public static String getDecodedBase64Media(String mBase64Data, String extension) {

        String base64 = "";
        File mFile = null;
        FileOutputStream out = null;

        try {

            if (Utils.getMediaType(extension).equalsIgnoreCase(IAppConstants.VIDEO)) {

                byte[] decodedString = org.spongycastle.util.encoders.Base64.decode(mBase64Data);

                try {

                    mFile = new File(IAppConstants.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis() + extension);
                    out = new FileOutputStream(mFile);
                    out.write(decodedString);
                    out.close();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Error", e.toString());
                }
            } else if (Utils.getMediaType(extension).equalsIgnoreCase(IAppConstants.IMAGE)) {
                byte[] decodedString = org.spongycastle.util.encoders.Base64.decode(mBase64Data);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                try {

                    mFile = new File(IAppConstants.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis() + extension);

                    out = new FileOutputStream(mFile);

                    // bmp is your Bitmap instance PNG is a lossless format, the compression factor (100) is ignored
                    decodedByte.compress(Bitmap.CompressFormat.JPEG, 90, out);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (Utils.getMediaType(extension).equalsIgnoreCase(IAppConstants.AUDIO)) {

                byte[] decodedString = org.spongycastle.util.encoders.Base64.decode(mBase64Data);

                try {

                    mFile = new File(IAppConstants.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis() + extension);
                    out = new FileOutputStream(mFile);
                    out.write(decodedString);
                    out.close();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Error", e.toString());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (mFile.getAbsolutePath());

    }

    /**
     * Method used to get Cursor from arraylist
     *
     * @param mContext -- context
     * @return -- cursor
     */
    public static Cursor getContactsList(Activity mContext) {

        Cursor mContactsCursor = null;

        allocateRunTimePermissions(mContext, new String[]{Manifest.permission.READ_CONTACTS}, new int[]{100});

        try {
            mContactsCursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mContactsCursor;
    }


    /**
     * Method used to get arraylist of contacts
     *
     * @param mContactsCursor
     * @param mContext
     * @return
     */
    public static ArrayList<ContactsModel> createContactModelFromCursor(Cursor mContactsCursor, Context mContext) {

        ArrayList<ContactsModel> mContactList = new ArrayList<>();

        try {

                if (mContactsCursor != null && mContactsCursor.getCount() > 0) {
                    while (mContactsCursor.moveToNext()) {

                        String id = mContactsCursor.getString(mContactsCursor.getColumnIndex(ContactsContract.Contacts._ID));

                        String contactName = mContactsCursor.getString(mContactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        String image_uri = mContactsCursor.getString(mContactsCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));



                        ArrayList<PublicKeyModel> publicKeyModelArrayList = getKeyList(mContext,id,contactName);
                        if(publicKeyModelArrayList != null && publicKeyModelArrayList.size() > 0) {
                            for (int i = 0; i < publicKeyModelArrayList.size() ; i++) {
                                ContactsModel mContact = new ContactsModel();
                                mContact.setmName(contactName);
                                mContact.setmImage(image_uri);
                                mContact.setmReceiverAddress(publicKeyModelArrayList.get(i).getPublicKey());
                                mContactList.add(mContact);
                            }

                        }


                    }
                    if (mContactsCursor != null)
                        mContactsCursor.close();
                }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return mContactList;
    }

    /**
     * Method used to get decimal place value
     *
     * @param value
     * @return
     */
    public static String convertDecimalFormatPattern(Double value) {
        try {
            DecimalFormat df = new DecimalFormat("#0.########");
            return df.format(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value.toString();
    }

    /**
     * Method used to get media type
     *
     * @param extension
     * @return
     */
    public static String getMediaType(String extension) {
        if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".3gp") || extension.equalsIgnoreCase(".mkv")) {
            return IAppConstants.VIDEO;
        } else if (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".m4a") || extension.equalsIgnoreCase(".wav")) {
            return IAppConstants.AUDIO;
        } else if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".png")) {
            return IAppConstants.IMAGE;
        } else {
            return IAppConstants.IMAGE;
        }
    }


    /***
     * This method is used to get keyList for contacts
     *
     * @param context
     * @param contactId
     * @return
     */
    public static ArrayList<PublicKeyModel> getKeyList(Context context, String contactId, String name) {
        ArrayList<PublicKeyModel> keyList = new ArrayList<>();
        Cursor postal = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.SipAddress.CONTACT_ID + " = "
                        + contactId, null, null);

        if (postal != null) {
            List<MobileModel> list=getMobileList(context,contactId);
            while (postal.moveToNext()) {
                int keyType = postal.getInt(postal.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                String publicKey = postal.getString(postal.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                switch (keyType) {
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME:
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK:
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER:
                        break;
                    default:
                        if (publicKey != null && publicKey.length() > 0) {
                            publicKey=publicKey.trim();
                            if (!publicKey.equalsIgnoreCase(name.trim())&&!name.contains(publicKey)
                                    && !publicKey.equals("")) {
                                boolean isExist = false;
                                for(int i=0;i<list.size();i++){
                                    if(list.get(i).getNumber().equalsIgnoreCase(publicKey)){
                                        isExist=true;
                                        break;
                                    }
                                }
                                if(!isExist) {
                                    keyList.add(new PublicKeyModel(publicKey));
                                }
                            }
                        }
                        break;
                }
            }
            postal.close();
        }
        return keyList;
    }

    /***
     * This method is used to get MobileLIst
     *
     * @param context
     * @param contactId
     * @return
     */

    public static ArrayList<MobileModel> getMobileList(Context context, String contactId) {

        Cursor pCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId}, null);
        ArrayList<MobileModel> mobileModelList = new ArrayList<>();

        assert pCursor != null;
        while (pCursor.moveToNext()) {
            int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
            phoneNo = phoneNo.replaceAll(" ", "");
            phoneNo = phoneNo.replaceAll("-", "");
            switch (phoneType) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    if (!isNumberExist(mobileModelList, phoneNo)) {
                        mobileModelList.add(new MobileModel(phoneNo, IAppConstants.MOBILE));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    if (!isNumberExist(mobileModelList, phoneNo)) {
                        mobileModelList.add(new MobileModel(phoneNo, IAppConstants.HOME));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    if (!isNumberExist(mobileModelList, phoneNo)) {
                        mobileModelList.add(new MobileModel(phoneNo, IAppConstants.WORK));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                    if (!isNumberExist(mobileModelList, phoneNo)) {
                        mobileModelList.add(new MobileModel(phoneNo, IAppConstants.MAIN));
                    }
                    break;
                default:
                    if (!isNumberExist(mobileModelList, phoneNo)) {
                        mobileModelList.add(new MobileModel(phoneNo, IAppConstants.CUSTOM));
                    }
                    break;
            }
        }
        pCursor.close();

        return mobileModelList;

    }
    /***
     * This method is used to write text message into file
     *
     * @param list
     * @param phone
     * @return
     */
    private static boolean isNumberExist(List<MobileModel> list, String phone) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().equalsIgnoreCase(phone) || list.get(i).getNumber().endsWith(phone)) {
                return true;
            }        }
        return false;
    }
    /***
     * This method is used to write text message into file
     *
     * @param text
     * @param location
     * @return
     */
    public static boolean writeToFile(byte[] text, String location, int bufSize) {

        try {
            File root = new File(location);
            if (!root.exists()) {
                root.mkdirs();
            }
            File mFile = new File(location);

            if (mFile.exists()) {
                mFile.delete();
            }
            mFile.createNewFile();
            byte[] bWrite = text;
            // OutputStream os = new FileOutputStream(mFile, false);
            FileWriter writer = new FileWriter(mFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);
            for (int x = 0; x < bWrite.length; x++) {
                bufferedWriter.write(bWrite[x]);   // writes the bytes
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Hashes generation of String type of data
     *
     * @param txId
     * @return
     */
    public static byte[] hashGenerate(String txId) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] TxIDHash = digest.digest(txId.getBytes(StandardCharsets.UTF_8));
        return TxIDHash;
    }

    /***
     * This method is used to write text message into file
     *
     * @param location
     * @return
     */
    public static String readDataFromTxt(String location){
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(location));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            return  null;
        }
    }

    /*
      Method to Decrypt session key(desktop) by token  - AES
      */
    public static String getDecryptedSessionKey(String encSkey,String deviceToken) throws Exception {
        UUID uuidOfToken = null;
        try {
            uuidOfToken = UUID.fromString(deviceToken.trim());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        long hi = uuidOfToken.getMostSignificantBits();
        long lo = uuidOfToken.getLeastSignificantBits();
        byte[] uuidInBytes = ByteBuffer.allocate(16).putLong(hi).putLong(lo).array();
        SecretKey secKey = null;
        try {
            secKey = getAESKey(Hex.toHexString(uuidInBytes).trim());
        } catch (Exception e) {
            throw e;

        }
        Cipher ecipher2 = null;
        try {
            ecipher2 = Cipher.getInstance(SYMALGORITHM, "SC");
            ecipher2.init(Cipher.DECRYPT_MODE, secKey, ivSpec);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw e;
        }
        byte[][] bWrite2 = null;
        bWrite2 = symDecryption(org.spongycastle.util.encoders.Base64.decode(encSkey), org.spongycastle.util.encoders.Base64.decode(encSkey).length, ecipher2);

        byte[] decSessKey = new byte[new BigInteger(bWrite2[1]).intValue()];
        System.arraycopy(bWrite2[0], 0, decSessKey, 0, new BigInteger(bWrite2[1]).intValue());
        return Hex.toHexString(decSessKey);
    }

    /*
    Symmetric Decryption - AES
    */
    private static byte[][] symDecryption ( byte[] cipherText, int length, Cipher dcipher ) throws Exception {

        byte[] plainText = new byte[dcipher.getOutputSize(length)];
        int ptLength = dcipher.update( cipherText, 0, length, plainText, 0);
        ptLength += dcipher.doFinal(plainText, ptLength);

        byte[][] result = new byte[2][];
        result[0] = plainText;
        result[1] = BigInteger.valueOf(ptLength).toByteArray();
        return result;
    }

    /**
     * Gets AES Key
     * */
    public static SecretKey getAESKey(String key) throws Exception {
        SecretKey symKey = null;
        byte[] input = Hex.decode(key);
        symKey = new SecretKeySpec(input, "AES");
        return symKey;
    }


}
