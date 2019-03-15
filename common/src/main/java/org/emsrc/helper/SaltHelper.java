package org.emsrc.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.security.SecureRandom;

/**
 * COPIED FROM PHONESTUDY
 * the salt (a random string concatenated with data values before hashing) is managed here
 * It is generated in the beginning of the study on the device individually (is different of
 * every user)
 * It is backuped through the Google Auto Backup. So if the user reinstalls the app, the salt stays the same
 */
public class SaltHelper {
    private static final String TAG = "SaltHelper";
    private static String salt = "";


    public static String getSalt(Context context){
        SharedPreferences pref = context.getSharedPreferences("salt", 0); // 0 - for private mode
        salt = pref.getString("salt", null);
        return salt;
    }

    public String generateSalt(){
        SecureRandom random = new SecureRandom();
        byte seed[] = random.generateSeed(15);

        for(int i = 0; i < seed.length; i++){
            salt += seed[i];
        }

        salt = removeSymbols(salt);
        salt = take15Numbers(salt);
        Log.i(TAG, "Salt wurde generiert" +
                " = " + salt);
        return salt;
    }

    private static String removeSymbols(String input){
        char inputChar[] = input.toCharArray();
        String outputString = "";

        for(int i=0; i<input.length(); i++){
            if(inputChar[i] == '-'){
                outputString += (int)(Math.random()*10);
            }else{
                outputString += inputChar[i];
            }
        }
        return outputString;
    }

    private static String take15Numbers(String input) {
        char inputChar[] = input.toCharArray();
        String outputString = "";

        for (int i = 0; i < 15; i++) {
            outputString += inputChar[((int) (Math.random() * 10) % 15)];
        }
        return outputString;
    }

    //Check if Salt in shared Pref already exist - if not create one
    public static void checkIfSaltIsAlreadyCreated(Context context){
        SharedPreferences saltPref = context.getSharedPreferences("salt", Context.MODE_PRIVATE);
        String salt = saltPref.getString("salt", "missing");
        if("missing".equals(salt)){
            //Generate Salt
            Log.i(TAG, "Salt war nicht vorhanden und wurde deshalb erstellt");
            SaltHelper saltHelper = new SaltHelper();
            String saltNew = saltHelper.generateSalt();

            SharedPreferences pref = context.getSharedPreferences("salt", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();

            Log.i(TAG, "Salt wurde in SharedPref gespeichert, Salt =  " + saltNew);
            editor.putString("salt", saltNew);

            editor.commit(); // commit changes
            editor.apply();
        } else {
            Log.i(TAG, "Salt war schon vorhanden");
        }
    }

}
