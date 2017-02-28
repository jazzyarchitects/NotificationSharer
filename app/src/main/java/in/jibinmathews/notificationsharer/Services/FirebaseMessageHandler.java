package in.jibinmathews.notificationsharer.Services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import in.jibinmathews.notificationsharer.Constants;
import in.jibinmathews.notificationsharer.TestNotification;

/**
 * Created by jibin on 26/2/17.
 */

public class FirebaseMessageHandler extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.Preferences.Application.PREF, MODE_PRIVATE);

        Map<String, String> msg = remoteMessage.getData();

        Log.e("FirebaseMessageHandler", msg.toString());

        if(msg.containsKey("pairing")){
            if(msg.get("pairing").equals("deletePairing")){
                sharedPreferences.edit().clear().apply();
            }
        }

        if(msg.containsKey("startupTime")){
            java.util.Date d = new java.util.Date(Long.parseLong(msg.get("startupTime")));
            String ids = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(d);
            TestNotification.notify(getApplicationContext(), "Linux Startup", "Laptop turned on at: "+ ids, 74165);
        }

        if (remoteMessage.getData().containsKey("NotificationEnabled")) {
            sharedPreferences.edit()
                    .putBoolean(Constants.Preferences.Application.NOTIFICATION_SHARING, Boolean.parseBoolean(remoteMessage.getData().get("SharingEnabled")))
                    .apply();
        }
    }
}
