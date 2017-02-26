package in.jibinmathews.notificationsharer.BroadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jibin on 25/2/17.
 */

public class NotificationReader extends NotificationListenerService {
    Context context;
    PackageManager packageManager;

    Socket socket;
    boolean connected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initalize();
    }

    private void initalize() {
        Log.e("NotificationReader", "onCreate");

        context = getApplicationContext();

        packageManager = context.getPackageManager();

        try {
            socket = IO.socket("http://jibinmathews.in");
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("NotificationReader", "Socket connected");
                    connected = true;
                    // First connection
                }
            });
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("NotificationReader", "Error Connecting to socket: " + args[0].toString());
                }
            });
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("NotificationReader", "Disconnected: "+args[0].toString());
                    connected = false;
                }
            });
            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (!connected) {
            initalize();
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
            jsonObject.put("text", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("NotificationReader", "Sending event" + jsonObject.toString());
        socket.emit("notification", jsonObject);

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
        Log.e("NotificationReader", "OnDestroy");
        socket.disconnect();
    }
}


