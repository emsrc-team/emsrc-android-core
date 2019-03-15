package org.emsrc.core.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.emsrc.core.R;
import org.emsrc.helper.SaltHelper;

public class ApplicationController extends Application {

    private static final String TAG = "ApplicationController";
    public static final String NOTIFICATION_CHANNEL_ID = "EMSRC_NOTIFICATION_CHANNEL";


    @Override
    public void onCreate() {
        super.onCreate();

        // required in order to prompt notifications
        createNotificationChannel(
                getApplicationContext(),
                NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
        );

        // start all logging stuff
        LoggingService.startService(this); // TODO also call this on boot (-> BootReceiver)

        // generate salt, if not done yet
        SaltHelper.checkIfSaltIsAlreadyCreated(this);

        Log.i(TAG,"started.");
    }

    /**
     *
     * @param context
     * @param notificationChannelId as specified when sending a notification later
     * @param importance one of {@see NotificationChannel.IMPORTANCE_...}
     */
    private  void createNotificationChannel(Context context, String notificationChannelId, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library

            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);

            NotificationChannel channel = new NotificationChannel(notificationChannelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }
    }

}
