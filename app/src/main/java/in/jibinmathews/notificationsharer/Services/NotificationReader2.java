package in.jibinmathews.notificationsharer.Services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import in.jibinmathews.notificationsharer.Constants;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jibin on 25/2/17.
 */

public class NotificationReader2 extends NotificationListenerService {
    Context context;
    PackageManager packageManager;
    SharedPreferences sharedPreferences;
    
    private final String TAG = "NotificationReader2";

    Socket socket;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e(TAG, "Notification Listener connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.e(TAG, "Notification listener disconnected");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");

        context = getApplicationContext();
        packageManager = context.getPackageManager();
        sharedPreferences = getSharedPreferences(Constants.Preferences.Application.PREF, Context.MODE_PRIVATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if(!sharedPreferences.contains(Constants.Preferences.Application.CHROME_UNIQUE_ID) || sbn.isOngoing()){
            Log.e(TAG, "Chrome Id not available or notification is ongoing");
            return;
        }

        Log.e(TAG, "Notification received");


        try {
            socket = IO.socket(Constants.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String ticker = "";

        String appName = getAppNameFromPackage(sbn.getPackageName());
        if (sbn.getNotification().tickerText != null) {
            ticker = sbn.getNotification().tickerText.toString();
        }

        Bundle extras = sbn.getNotification().extras;

        String title = extras.getString("android.title");
        String text = "";

        CharSequence content = extras.getCharSequence("android.text");
        if (content != null) {
            text = content.toString();
        }

        if (appName.equals("Android system") || appName.equals("Security") || (appName.equals("Download Manager") && text.isEmpty())) {
            return;
        }

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("AppName", appName);
            jsonObject.put("ticker", ticker);
            jsonObject.put("title", title);
            jsonObject.put("id", sbn.getId());
            jsonObject.put("text", text);
            jsonObject.put("chromeId", sharedPreferences.getString(Constants.Preferences.Application.CHROME_UNIQUE_ID, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Sending event" + jsonObject.toString());

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket connected");
                socket.emit(Constants.Socket.EVENT_NOTIFICATION, jsonObject);
                socket.disconnect();
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket disconnected: "+args[0].toString());
            }
        });

        socket.connect();

    }

    private String getAppNameFromPackage(String packageName) {
        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "OnDestroy");
        socket.disconnect();
    }
}


