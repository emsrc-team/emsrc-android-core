package org.emsrc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.emsrc.common.BuildConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * COPIED FROM PHONESTUDY
 * Class implementing methods to encrypt a given String (for sensitive data).
 */
public class HashFactory {
    public static String TAG = "HashFactory";
    private static int NO_OPTIONS = 0;
    private String SHAHash;
    private Boolean HASH_BOOL;
    private SharedPreferences sharedPreferences;
    private static final String HASH = "HASH";
    private Context contextInside;

    /**
     * Constructor. Checks in SharedPreferences if Hashing is enabled or disabled.
     * @param context the application context
     */
    public HashFactory(Context context){

        this.sharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);

        if (BuildConfig.HASHING) {
            sharedPreferences.edit().putBoolean(HASH, true).commit();
        }
        else {
            sharedPreferences.edit().putBoolean(HASH, false).commit();
        }

        this.HASH_BOOL = this.sharedPreferences.getBoolean(HASH, true);

        contextInside = context;
    }

    /**
     * Method to get the hashed representation of a given String. The actual Hash computing is done in another method.
     * @param password the String to be hashed
     * @return the hashed representation of the input
     */
    public String getHash(String password){
        String hashString;
        if(password != null) {
            if (HASH_BOOL) {
                if (!password.equals("")) {
                    hashString = computeSHAHash(password);
                    return hashString;
                } else {
                    return password;
                }
            } else {
                return password;
            }
        }else{
            Log.v(TAG, "String is null in getHash()");
            return "";
        }

    }

    /**
     * Method to compute a SHA-1 hash representation of a given String.
     * @param password the String to be hashed
     * @return the hashed representation of the input
     */
    public String computeSHAHash(String password) {
        Log.d(TAG, "computeSHAHash()");
        if (SaltHelper.getSalt(contextInside) != null) {
            password += SaltHelper.getSalt(contextInside);
        }
        if (password != null){
            MessageDigest mdSha1 = null;
            try {
                mdSha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e1) {
                Log.e(TAG, "Error initializing SHA1 message digest");
            }
            try {
                mdSha1.update(password.getBytes("ASCII"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            byte[] data = mdSha1.digest();
            try {
                SHAHash = convertToHex(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SHAHash;
        }
        else {
            return "null";
        }
    }

    /**
     * Method to compute a MH5 representation of a given String
     * @param password the String to be hashed
     * @return the hashed representation of the input
     */
    public String computeMD5Hash(String password) {
        Log.d(TAG, "computeMD5Hash()");
        if (SaltHelper.getSalt(contextInside) != null) {
            password += SaltHelper.getSalt(contextInside);
        }
        if (password != null){
            try {
                // Create MD5 Hash
                MessageDigest digest = MessageDigest
                        .getInstance("MD5");
                digest.update(password.getBytes());
                byte messageDigest[] = digest.digest();

                StringBuffer MD5Hash = new StringBuffer();
                for (int i = 0; i < messageDigest.length; i++) {
                    String h = Integer.toHexString(0xFF & messageDigest[i]);
                    while (h.length() < 2)
                        h = "0" + h;
                    MD5Hash.append(h);
                }
                return MD5Hash.toString();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.e(TAG, "an Error occured!");
                return "Error!!!";
            }
        }
        else{
            return "null";
        }
    }

    /**
     * Method to encode data using Base64-encoding.
     * @param data the data that is to be encoded
     * @return the encoded String
     * @throws IOException
     */
    private static String convertToHex(byte[] data) throws IOException {
        Log.d(TAG, "convertToHex()");
        StringBuffer sb = new StringBuffer();
        String hex;
        hex = Base64.encodeToString(data, 0, data.length, NO_OPTIONS);
        sb.append(hex);
        return sb.toString();
    }

}


