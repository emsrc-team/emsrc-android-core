package org.emsrc.common.loggingcomponent;

import android.content.Context;

public interface ILoggingComponent {

    /**
     * Is called when the app starts, by the LoggingService
     * @param context
     */
    void startLoggingComponent(Context context);


    /**
     * Is called when the logging should stop, e.g. on study end
     * @param context
     */
    void stopLoggingComponent(Context context);

}
