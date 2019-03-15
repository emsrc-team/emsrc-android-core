package org.emsrc.core.app;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.emsrc.common.loggingcomponent.ILoggingComponent;
import org.emsrc.core.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.emsrc.core.app.ApplicationController.NOTIFICATION_CHANNEL_ID;

public class LoggingService extends Service {

    private static final String TAG = "LoggingService";
    private static final Integer PERMANENT_NOTIFICATION_ID = 543768109; // some random number

    private List<ILoggingComponent> launchedLoggingComponents = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        // we don't need this, as LoggingService is a foreground service and not a bound service
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start the permanent notification, to bind the app as foreground service (avoids being killed by Android)
        startForeground(PERMANENT_NOTIFICATION_ID, buildPermanentNotification("Hello World!"));

        // start all logging components (receivers, services,  observers) running in the background
        findAndStartLoggingComponents();

        Log.i(TAG,"service started");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        stopLoggingComponents();

        super.onDestroy();
    }

    /**
     * finds all registered logging components, and starts them
     * A logging module has to register through its Manifest.
     * It must contain a meta-data entry like this:
     * <manifest ... >
     *     <application ... >
     *         ...
     *         <meta-data
     *             android:name="org.emsrc.logging.component.appusage"
     *             android:value="org.emsrc.logging.appusage.AppUsageLoggingComponent" />
     *     </application>
     * </manifest>
     * The name property MUST start with org.emsrc.logging.component. and end with an unique identifier,
     * the value property MUST be a class name of the components "main"-class
     * That class must have
     * - a default constructor
     * - implement ILoggingComponent
     */
    private void findAndStartLoggingComponents(){

        // ---- 1.: load application info (contains data from Manifest) ----
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplication().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,"could not look for available logging components",e);
        }

        // ---- 2.: from all Manifests (including those of indluded library projects),
        //          get all meta-data elements starting with org.emsrc.logging.component.
        //          Those are the logging component registrations
        List<String> loggingComponentsClassnames = new ArrayList<>();
        Bundle bundle = applicationInfo.metaData;
        for (String key: bundle.keySet()) {
            if (key.startsWith("org.emsrc.logging.component.")) {
                loggingComponentsClassnames.add(bundle.getString(key));
                Log.i(TAG, "Logging component found:"+key + " / "+bundle.getString(key));
            }
        }
        // ---- 3.: Then instantiate the component's main class (
        //          and in there happens the magic (e.g. registering a receiver)
        for (String loggingComponentsClassname : loggingComponentsClassnames) {
            try {
                // create an instance of that lass
                Class<?> clazz = Class.forName(loggingComponentsClassname);
                Constructor<?>  ctor = clazz.getConstructor();
                Object object = ctor.newInstance(new Object[]{});
                ILoggingComponent loggingComponentInstance = (ILoggingComponent) object;
                // call the startLoggingComponent method, to actually launch it
                loggingComponentInstance.startLoggingComponent(getApplicationContext());
                // keep reference for later
                launchedLoggingComponents.add(loggingComponentInstance);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "instantiating a logging component failed. Check if an existing " +
                        "classname is set in the component's Manifest. A meta-data element whose " +
                        "name starts with 'org.emsrc.logging.component.' must exist, the value must " +
                        "be a class implementing ILoggingComponent. " +
                        "Defined class was not found: "+loggingComponentsClassname,e);
            } catch (NoSuchMethodException e) {
                Log.e(TAG,"the class "+loggingComponentsClassname+
                        " must have a default constructor, but hasn't.",e);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                Log.e(TAG,"could not instantiate logging component, precisely class "+loggingComponentsClassname,e);
            }
        }

    }

    /**
     * stops all registered logging components
     */
    private void stopLoggingComponents(){
       for (ILoggingComponent launchedLoggingComponent : launchedLoggingComponents){
           if (launchedLoggingComponent != null) {
               launchedLoggingComponent.startLoggingComponent(getApplicationContext());
           }
       }
    }


    private Notification buildPermanentNotification(CharSequence message){
         return new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setContentText(message)
                //.setSmallIcon(R.drawable.notification_icon)  TODO we need an icon
                .setColor(getResources().getColor(R.color.colorPrimary))
                .build();
    }

    public static void startService(Context context) {
        if(!isServiceRunning(context,LoggingService.class)){
            Intent loggingIntent = new Intent(context, LoggingService.class);
            context.startService(loggingIntent);
        }else{
            Log.d(TAG, "Service already running");
        }
    }


    private static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
