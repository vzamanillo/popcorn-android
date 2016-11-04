package butter.droid.base.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class Locales extends Compatibility{

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getLocale(Context context){

        if (hasApi(Build.VERSION_CODES.N)) {
            return context.getResources().getConfiguration().getLocales().get(0);
        }
        return context.getResources().getConfiguration().locale;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        if (upToApi(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}